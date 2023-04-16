package com.pierre2803.functionapp.status

import com.microsoft.azure.functions.HttpMethod
import com.pierre2803.functionapp.MockHttpRequestMessage
import com.pierre2803.functionapp.TestData.flywayInfo
import com.pierre2803.functionapp.db.FlywayRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
internal class StatusFunctionsTest {

    @MockK private lateinit var flywayRepository: FlywayRepository
    private lateinit var function: StatusFunctions
    private lateinit var mockStatusRequest: MockHttpRequestMessage<String>

    @BeforeEach
    internal fun setUp() {
        function = StatusFunctions(flywayRepository)
        mockStatusRequest = MockHttpRequestMessage()
        mockStatusRequest.requestHttpMethod = HttpMethod.GET
    }

    @Test
    fun `expect no error when calling getStatus`() {
        // Given
        every { flywayRepository.getFlywayInfo() } returns flywayInfo
        // When
        val response = function.getStatus(mockStatusRequest)
        // Then
        assertThat(response.statusCode).isEqualTo(200)
        val actualBody: String = response.body.toString()

        assertNotNull(actualBody)
        assertThat(actualBody).contains("gitBranch")
        assertThat(actualBody).contains("gitCommitIdAbbrev")

        assertThat(actualBody).contains("dbVersion=" + flywayInfo.version)
        assertThat(actualBody).contains("dbVersionDescription=" + flywayInfo.description)
        assertThat(actualBody).contains("dbVersionInstalledOn=" + flywayInfo.installedOn)
    }

}