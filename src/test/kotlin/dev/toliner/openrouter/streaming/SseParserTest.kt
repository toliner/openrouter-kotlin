package dev.toliner.openrouter.streaming

import dev.toliner.openrouter.error.OpenRouterException
import dev.toliner.openrouter.l1.chat.ChatCompletionResponse
import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

class SseParserTest : FunSpec({
    test("normal chunk is parsed") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"Hello"}}]}"""),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].id shouldBe "gen-123"
        chunks[0].choices[0].delta.content shouldBe "Hello"
    }

    test("comment line is skipped") {
        val events = flowOf(
            ServerSentEvent(data = ": OPENROUTER PROCESSING"),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks shouldBe emptyList()
    }

    test("done marker completes flow") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"A"}}]}"""),
            ServerSentEvent(data = "[DONE]"),
            ServerSentEvent(data = """{"id":"gen-999","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"SHOULD_NOT_EMIT"}}]}""")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].choices[0].delta.content shouldBe "A"
    }

    test("usage-only chunk is emitted") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[],"usage":{"prompt_tokens":10,"completion_tokens":20,"total_tokens":30}}"""),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].choices.size shouldBe 0
        chunks[0].usage?.totalTokens shouldBe 30
    }

    test("debug chunk with empty choices is skipped") {
        val events = flowOf(
            ServerSentEvent(data = """{"choices":[],"debug":{"echo_upstream_body":true}}"""),
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"Hello"}}]}"""),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].choices[0].delta.content shouldBe "Hello"
    }

    test("unknown debug field with non-empty choices still emits chunk") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"Hello"}}],"debug":{"foo":"bar"}}"""),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].choices[0].delta.content shouldBe "Hello"
    }

    test("empty data line is skipped") {
        val events = flowOf(
            ServerSentEvent(data = ""),
            ServerSentEvent(data = " "),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks shouldBe emptyList()
    }

    test("raw sse data prefix is normalized") {
        val events = flowOf(
            ServerSentEvent(data = "data: {\"id\":\"gen-123\",\"model\":\"openai/gpt-4o\",\"object\":\"chat.completion.chunk\",\"created\":1,\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"}}]}"),
            ServerSentEvent(data = "data: [DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 1
        chunks[0].choices[0].delta.content shouldBe "Hello"
    }

    test("raw empty data prefix line is skipped") {
        val events = flowOf(
            ServerSentEvent(data = "data: "),
            ServerSentEvent(data = "data: [DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks shouldBe emptyList()
    }

    test("mid-stream error throws StreamError") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{},"finish_reason":"error","error":{"message":"upstream failed","code":500}}]}""")
        )

        val error = shouldThrow<OpenRouterException.StreamError> {
            events.toChatCompletionChunks(OpenRouterJson).toList()
        }

        error.body.message shouldBe "upstream failed"
        error.body.code shouldBe 500
    }

    test("multiple chunks are emitted in order") {
        val events = flowOf(
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"He"}}]}"""),
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":2,"choices":[{"index":0,"delta":{"content":"ll"}}]}"""),
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":3,"choices":[{"index":0,"delta":{"content":"o"}}]}"""),
            ServerSentEvent(data = """{"id":"gen-123","model":"openai/gpt-4o","object":"chat.completion.chunk","created":4,"choices":[],"usage":{"prompt_tokens":10,"completion_tokens":2,"total_tokens":12}}"""),
            ServerSentEvent(data = "[DONE]")
        )

        val chunks = events.toChatCompletionChunks(OpenRouterJson).toList()

        chunks.size shouldBe 4
        chunks[0].choices[0].delta.content shouldBe "He"
        chunks[1].choices[0].delta.content shouldBe "ll"
        chunks[2].choices[0].delta.content shouldBe "o"
        chunks[3].usage?.totalTokens shouldBe 12
    }

    test("in-band error in completion response throws InBandError") {
        val response = OpenRouterJson.decodeFromString<ChatCompletionResponse>(
            """
            {
              "id": "gen-123",
              "model": "openai/gpt-4o",
              "object": "chat.completion",
              "created": 1,
              "choices": [
                {
                  "index": 0,
                  "message": {"role": "assistant", "content": ""},
                  "finish_reason": "error",
                  "error": {"message": "in-band failure", "code": 400}
                }
              ]
            }
            """.trimIndent()
        )

        val error = shouldThrow<OpenRouterException.InBandError> {
            response.checkInBandError()
        }

        error.body.message shouldBe "in-band failure"
        error.body.code shouldBe 400
    }
})
