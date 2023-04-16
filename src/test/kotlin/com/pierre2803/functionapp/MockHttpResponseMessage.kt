package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatusType

class MockHttpResponseMessage(
        private val responseStatus: HttpStatusType?,
        private val requestHeaders: MutableMap<String, String>,
        private val responseBody: Any?) : HttpResponseMessage {

    override fun getStatus() = responseStatus

    override fun getHeader(key: String?) = requestHeaders[key]

    override fun getBody() = responseBody
}

class MockHttpResponseMessageBuilder : HttpResponseMessage.Builder {

    var responseStatus: HttpStatusType? = null
    var responseBody: Any? = null
    val responseHeaders = mutableMapOf<String, String>()

    override fun status(status: HttpStatusType?): HttpResponseMessage.Builder {
        responseStatus = status
        return this
    }

    override fun header(key: String?, value: String?): HttpResponseMessage.Builder {
        if (!key.isNullOrBlank() && !value.isNullOrBlank())
            responseHeaders[key] = value
        return this
    }

    override fun body(body: Any?): HttpResponseMessage.Builder {
        body?.let { responseBody = it }
        return this
    }

    override fun build(): HttpResponseMessage {
        return MockHttpResponseMessage(responseStatus, responseHeaders, responseBody)
    }
}
