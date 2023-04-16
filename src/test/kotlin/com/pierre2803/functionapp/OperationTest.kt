package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpMethod.*
import com.microsoft.azure.functions.HttpMethod.DELETE
import com.pierre2803.functionapp.Operation.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

internal class OperationTest {

    @Test
    fun `get CREATE when the http method is POST`() {
        assertThat(Operation.toOperation(POST)).isEqualTo(CREATE)
    }

    @Test
    fun `get UPDATE when the http method is PUT`() {
        assertThat(Operation.toOperation(PUT)).isEqualTo(UPDATE)
    }

    @Test
    fun `get READ when the http method is GET`() {
        assertThat(Operation.toOperation(GET)).isEqualTo(READ)
    }

    @Test
    fun `get DELETE when the http method is DELETE`() {
        assertThat(Operation.toOperation(DELETE)).isEqualTo(Operation.DELETE)
    }

    @ParameterizedTest
    @ArgumentsSource(UnsupportedHttpMethodProvider::class)
    fun `expect exception when the http method is not supported`(httpMethod: HttpMethod) {
        assertThatThrownBy { Operation.toOperation(httpMethod) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("No supported operation matches the HTTP method $httpMethod.")
    }

    class UnsupportedHttpMethodProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return HttpMethod.values()
                    .filter { it != GET }
                    .filter { it != POST }
                    .filter { it != PUT }
                    .filter { it != DELETE }
                    .map { Arguments.arguments(it) }.stream()
        }
    }
}