package com.pierre2803.functionapp

class ValidationException(private vararg val applicationError: com.pierre2803.functionapp.ApplicationError) : RuntimeException() {
    fun getApplicationErrors() = applicationError.asList()
}
