package dev.toliner.openrouter.testutil

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.HttpRequestData
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.client.engine.mock.respond

fun mockEngineWithResponse(
    responseBody: String,
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    headers: Headers = headersOf("Content-Type", "application/json"),
    requestValidator: ((HttpRequestData) -> Unit)? = null
): MockEngine = MockEngine { requestData ->
    requestValidator?.invoke(requestData)
    respond(content = responseBody, status = statusCode, headers = headers)
}
