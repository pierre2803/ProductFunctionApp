package com.pierre2803.functionapp

import com.microsoft.azure.functions.*
import java.net.URI

class MockHttpRequestMessage<T> : HttpRequestMessage<T> {

    private val requestHeaders = mutableMapOf<String, String>()
    var requestHttpMethod: HttpMethod? = null
    private val requestQueryParam = mutableMapOf<String, String>()
    var requestUri: URI? = null
    var requestBody: T? = null

    override fun getHeaders() = requestHeaders

    override fun getHttpMethod() = requestHttpMethod

    override fun getQueryParameters() = requestQueryParam

    override fun getUri() = requestUri

    override fun getBody() = requestBody

    override fun createResponseBuilder(status: HttpStatus?): HttpResponseMessage.Builder {
        val builder = MockHttpResponseMessageBuilder()
        builder.responseStatus = status
        return builder
    }

    override fun createResponseBuilder(status: HttpStatusType?): HttpResponseMessage.Builder {
        val builder = MockHttpResponseMessageBuilder()
        builder.responseStatus = status
        return builder
    }
}
