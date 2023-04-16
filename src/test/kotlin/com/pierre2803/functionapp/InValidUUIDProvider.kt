package com.pierre2803.functionapp

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class InValidUUIDProvider : ArgumentsProvider {
    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return listOf("", " ", "   ", "invalid-uuid")
            .map { Arguments.arguments(it) }
            .stream()
    }
}