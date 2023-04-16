package com.pierre2803.functionapp

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
        @JsonProperty("errors") val errors: List<ValidationError>)

data class ValidationError(
        @JsonProperty("code") val errorCode: String,
        @JsonProperty("message") val errorMessage: String)
