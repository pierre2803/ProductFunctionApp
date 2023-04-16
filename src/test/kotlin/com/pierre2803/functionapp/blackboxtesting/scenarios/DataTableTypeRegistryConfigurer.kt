package com.pierre2803.functionapp.blackboxtesting.scenarios

import io.cucumber.java.DefaultDataTableEntryTransformer
import java.lang.reflect.Type
import java.util.*

@Suppress("unused")
class DataTableTypeRegistryConfigurer {

    @DefaultDataTableEntryTransformer
    fun defaultTransformer(valueMap: Map<String, String>, toValueType: Type): Any {
        return when (toValueType) {

            else -> error("The type $toValueType cannot be automatically transformed. Add the case to ${DataTableTypeRegistryConfigurer::class.java.canonicalName}")
        }
    }

}