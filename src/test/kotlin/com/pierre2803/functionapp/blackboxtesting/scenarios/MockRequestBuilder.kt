package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpMethod.*
import com.microsoft.azure.functions.HttpRequestMessage
import com.pierre2803.functionapp.MockHttpRequestMessage
import com.pierre2803.functionapp.TestData.defaultProductId
import java.net.URI
import java.util.*

object MockRequestBuilder {

    fun <T> mockHttpGetRequest(path: String, productId: UUID = defaultProductId, ) = mockHttpRequest(method = GET, path = path, productId = productId, queryParams = mutableMapOf(), body = null as T?)

    fun <T> mockHttpPostRequest(path: String, body: T?) = mockHttpRequest(method = POST, path = path, body = body)

    fun <T> mockHttpPutRequest(path: String, productId: UUID = defaultProductId, body: T?) = mockHttpRequest(method = PUT, path = path, productId = productId, body = body)

    fun <T> mockHttpDeleteRequest(path: String, productId: UUID = defaultProductId) = mockHttpRequest(method = DELETE, path = path, body = null as T?, productId = productId)

    private fun <T> mockHttpRequest(method: HttpMethod, path: String, productId: UUID = defaultProductId, body: T?, queryParams: MutableMap<String, String>? = null): HttpRequestMessage<T> {
        val mockRequest = MockHttpRequestMessage<T>()
        mockRequest.requestHttpMethod = method
        mockRequest.headers["X-ProductId"] = productId.toString()
        queryParams?.let { mockRequest.queryParameters.putAll(it) }
        mockRequest.requestBody = body
        mockRequest.requestUri = URI.create("http://functionapp.pjmbusnel.io/api/$path")
        return mockRequest
    }
}