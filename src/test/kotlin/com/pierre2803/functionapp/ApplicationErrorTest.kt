package com.pierre2803.functionapp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

internal class ApplicationErrorTest {

    @Test
    fun `expect no duplicate error code on companion object`() {
        assertNoDuplicateCode(ApplicationError.Companion::class)
        assertErrorCodeInRange(ApplicationError.Companion::class, minErrorCodeValue = 1, maxErrorCodeValue = 9999)
    }

    @Test
    fun `expect no duplicate error code on Product object`() {
        assertNoDuplicateCode(ApplicationError.Product::class)
        assertErrorCodeInRange(ApplicationError.Product::class, minErrorCodeValue = 10000)
    }

    private inline fun <reified T: Any> assertNoDuplicateCode(objectKClass: KClass<T>) {
        val applicationErrors = objectKClass.memberProperties
                .map { it.get(objectKClass.objectInstance!!) }
                .map { it as com.pierre2803.functionapp.ApplicationError }
        val duplicateApplicationErrorCodes = applicationErrors
                .groupBy { it.getCode() }
                .mapValues { it.value.size }
                .filter { it.value > 1 }
                .keys
        assertThat(duplicateApplicationErrorCodes).`as`("Duplicate error codes found ($duplicateApplicationErrorCodes)").isEmpty()
    }

    private inline fun <reified T: Any> assertErrorCodeInRange(objectKClass: KClass<T>, minErrorCodeValue: Int, maxErrorCodeValue: Int = Int.MAX_VALUE) {
        val outOfRangeCodeValues = objectKClass.memberProperties
                .asSequence()
                .map { it.get(objectKClass.objectInstance!!) }
                .map { (it as com.pierre2803.functionapp.ApplicationError).getCode() }
                .map { it.substring(startIndex = it.lastIndexOf('_') + 1) }
                .map { it.toInt() }
                .filter { it < minErrorCodeValue || it > maxErrorCodeValue }
                .toList()
        assertThat(outOfRangeCodeValues).`as`("Numeric error code(s) ($outOfRangeCodeValues) are out of range [$minErrorCodeValue,$maxErrorCodeValue]").isEmpty()
    }
}