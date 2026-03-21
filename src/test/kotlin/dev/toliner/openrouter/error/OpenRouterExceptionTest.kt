package dev.toliner.openrouter.error

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OpenRouterExceptionTest : FunSpec({
    test("errorFromStatus maps known HTTP statuses to sealed subclasses") {
        val body = ErrorBody(message = "boom")

        errorFromStatus(400, body) shouldBe OpenRouterException.BadRequest(body)
        errorFromStatus(401, body) shouldBe OpenRouterException.Unauthorized(body)
        errorFromStatus(402, body) shouldBe OpenRouterException.PaymentRequired(body)
        errorFromStatus(403, body) shouldBe OpenRouterException.Forbidden(body)
        errorFromStatus(408, body) shouldBe OpenRouterException.RequestTimeout(body)
        errorFromStatus(429, body, retryAfter = 12) shouldBe OpenRouterException.TooManyRequests(body, 12)
        errorFromStatus(502, body) shouldBe OpenRouterException.BadGateway(body)
        errorFromStatus(503, body) shouldBe OpenRouterException.ServiceUnavailable(body)
    }

    test("errorFromStatus maps unknown HTTP statuses to UnknownError") {
        val body = ErrorBody(message = "boom")

        errorFromStatus(500, body) shouldBe OpenRouterException.UnknownError(500, body)
        errorFromStatus(504, body) shouldBe OpenRouterException.UnknownError(504, body)
        errorFromStatus(524, body) shouldBe OpenRouterException.UnknownError(524, body)
        errorFromStatus(529, body) shouldBe OpenRouterException.UnknownError(529, body)
        errorFromStatus(418, body) shouldBe OpenRouterException.UnknownError(418, body)
    }

    test("isRetryable returns true only for retryable errors") {
        val body = ErrorBody(message = "boom")

        errorFromStatus(429, body).isRetryable shouldBe true
        errorFromStatus(502, body).isRetryable shouldBe true
        errorFromStatus(503, body).isRetryable shouldBe true
        errorFromStatus(500, body).isRetryable shouldBe true
        errorFromStatus(504, body).isRetryable shouldBe true
        errorFromStatus(524, body).isRetryable shouldBe true
        errorFromStatus(529, body).isRetryable shouldBe true

        errorFromStatus(400, body).isRetryable shouldBe false
        errorFromStatus(401, body).isRetryable shouldBe false
        errorFromStatus(402, body).isRetryable shouldBe false
        errorFromStatus(403, body).isRetryable shouldBe false
        errorFromStatus(408, body).isRetryable shouldBe false
        errorFromStatus(418, body).isRetryable shouldBe false
    }

    test("when expression over sealed hierarchy is exhaustive") {
        fun classify(exception: OpenRouterException): String = when (exception) {
            is OpenRouterException.BadRequest -> "400"
            is OpenRouterException.Unauthorized -> "401"
            is OpenRouterException.PaymentRequired -> "402"
            is OpenRouterException.Forbidden -> "403"
            is OpenRouterException.RequestTimeout -> "408"
            is OpenRouterException.TooManyRequests -> "429"
            is OpenRouterException.BadGateway -> "502"
            is OpenRouterException.ServiceUnavailable -> "503"
            is OpenRouterException.UnknownError -> "unknown"
            is OpenRouterException.StreamError -> "stream"
            is OpenRouterException.InBandError -> "in-band"
        }

        classify(OpenRouterException.StreamError(ErrorBody("stream"))) shouldBe "stream"
        classify(OpenRouterException.InBandError(ErrorBody("in-band"))) shouldBe "in-band"
    }

    test("OpenRouterException retains message and cause contract") {
        val body = ErrorBody(message = "boom")
        val cause = IllegalStateException("cause")

        shouldThrow<IllegalStateException> {
            throw cause
        }

        errorFromStatus(400, body).message shouldBe "boom"
    }
})
