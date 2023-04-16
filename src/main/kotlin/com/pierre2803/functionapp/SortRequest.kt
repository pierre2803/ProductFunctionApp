package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpRequestMessage
import com.pierre2803.functionapp.Validations.supportedValue

private const val defaultSortByField = "name"
private val supportedValues = listOf("name")

abstract class SortRequest(request: HttpRequestMessage<String>, private val invalidSortError: com.pierre2803.functionapp.ApplicationError) {

    private val sortBy: String? = request.queryParameters["sort"]

    internal fun validateAndTransform(operation: Operation): QuerySort {
        val errors = mutableListOf<com.pierre2803.functionapp.ApplicationError>()
        supportedValue(sortBy, supportedValues, operation, invalidSortError)?.let { errors.add(it) }
        if (errors.isNotEmpty()) throw ValidationException(*errors.toTypedArray())
        return QuerySort(sortBy = if (sortBy.isNullOrBlank()) defaultSortByField else sortBy)
    }
}
