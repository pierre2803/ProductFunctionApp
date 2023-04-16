package com.pierre2803.functionapp

import com.microsoft.azure.functions.HttpStatus
import com.microsoft.azure.functions.HttpStatus.BAD_REQUEST
import com.pierre2803.functionapp.ApplicationError.Companion.DEFAULT_ERROR
import com.pierre2803.functionapp.ApplicationError.Companion.INVALID_UUID
import com.pierre2803.functionapp.ApplicationError.GeneralApplicationError
import com.pierre2803.functionapp.Operation.CREATE
import com.pierre2803.functionapp.Operation.READ
import com.pierre2803.functionapp.Validations.alphaAccentNumDashUnderscoreSpace
import com.pierre2803.functionapp.Validations.alphaNumDashUnderscore
import com.pierre2803.functionapp.Validations.maxLength
import com.pierre2803.functionapp.Validations.noBlankElement
import com.pierre2803.functionapp.Validations.positiveNumber
import com.pierre2803.functionapp.Validations.supportedValue
import com.pierre2803.functionapp.Validations.validDate
import com.pierre2803.functionapp.Validations.validTime
import com.pierre2803.functionapp.Validations.validTimezone
import com.pierre2803.functionapp.Validations.validUUID
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertNotNull

private val supportedValues = listOf("a", "b", "c")

@Suppress("PrivatePropertyName")
internal class ValidationsTest {

    @Test
    fun `no error when a required string is non-null non-blank`() {
        // When
        val error = Validations.required("valid", CREATE, TEST_NO_NAME)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `error when a required string is not provided`(value: String?) {
        // When
        val error = Validations.required(value, CREATE, TEST_NO_NAME)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_200")
        assertThat(error.getMessage()).isEqualTo("No value provided.")
    }

    @Test
    fun `no error when a required long is non-null`() {
        // When
        val error = Validations.required(0L, CREATE, DEFAULT_ERROR)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when a required long is not provided`() {
        // Given
        val value: Long? = null
        // When
        val error = Validations.required(value, CREATE, TEST_NO_NAME)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_200")
        assertThat(error.getMessage()).isEqualTo("No value provided.")
    }

    @Test
    fun `no error when a required list is non-null non-empty`() {
        // When
        val error = Validations.required(listOf("a", "b"), CREATE, DEFAULT_ERROR)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(NoListValueProvider::class)
    fun `error when a required list is empty`(value: List<Any>?) {
        // When
        val error = Validations.required(value, CREATE, TEST_NO_NAME)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_200")
        assertThat(error.getMessage()).isEqualTo("No value provided.")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no value is provided for a check against a required and supported list of values`(value: String?) {
        // When
        val error = Validations.required(value, supportedValues, CREATE, TEST_NO_NAME, TEST_UNSUPPORTED_VALUE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_200")
        assertThat(error.getMessage()).isEqualTo("No value provided.")
    }

    @ParameterizedTest
    @ArgumentsSource(SupportedValueProvider::class)
    fun `no error when a non-null non-blank and supported value is provided for a check against a required and supported list of values`(value: String) {
        // When
        val error = Validations.required(value, supportedValues, CREATE, TEST_NO_NAME, TEST_UNSUPPORTED_VALUE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when a non-null non-blank and unsupported value is provided for a check against a required and supported list of values`() {
        // When
        val error = Validations.required("AAAAA", supportedValues, CREATE, TEST_NO_NAME, TEST_UNSUPPORTED_VALUE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_100")
        assertThat(error.getMessage()).isEqualTo("Value AAAAA is not supported. Supported values are: a, b, c")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no value is provided against a check for max length`(value: String?) {
        // When
        val error = maxLength(value, 10, CREATE, TEST_VALUE_TOO_LONG)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when value with a length exactly at the limit is provided against a check for max length`() {
        // Given
        val value = randomAlphabetic(10)
        // When
        val error = maxLength(value, 10, CREATE, TEST_VALUE_TOO_LONG)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when value too long is provided against a check for max length`() {
        // Given
        val value = randomAlphabetic(11)
        // When
        val error = maxLength(value, 10, CREATE, TEST_VALUE_TOO_LONG)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_600")
        assertThat(error.getMessage()).isEqualTo("Value too long. Max is 10.")
    }

    @Test
    fun `no error when a null long value is provided against a check for positive number`() {
        // When
        val error = positiveNumber(null, CREATE, TEST_NOT_POSITIVE_LONG)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when a long value of 0 is provided against a check for positive number`() {
        // When
        val error = positiveNumber(0, CREATE, TEST_NOT_POSITIVE_LONG)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_700")
        assertThat(error.getMessage()).isEqualTo("Value 0 is not a valid and positive long.")
    }

    @Test
    fun `no error when a long value greater than 0 is provided against a check for positive number`() {
        // When
        val error = positiveNumber(1, CREATE, TEST_NOT_POSITIVE_LONG)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when negative long value is provided against a check for positive number`() {
        // When
        val error = positiveNumber(-1, CREATE, TEST_NOT_POSITIVE_LONG)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_700")
        assertThat(error.getMessage()).isEqualTo("Value -1 is not a valid and positive long.")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no string is provided for a check against alphaNumDashUnderscore`(value: String?) {
        // When
        val error = alphaNumDashUnderscore(value, CREATE, TEST_INVALID_STRING)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a string with valid char is provided for a check against alphaNumDashUnderscore`() {
        // When
        val error = alphaNumDashUnderscore("abcdefghijklmnopqrstuvwxyz0123456789-_", CREATE, TEST_INVALID_STRING)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a string with invalid char is provided for a check against alphaNumDashUnderscore`() {
        // When
        val error = alphaNumDashUnderscore("abcdefghijklmnopqrstuvwxyz0123456789-_<>", CREATE, TEST_INVALID_STRING)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_800")
        assertThat(error.getMessage()).isEqualTo("Value abcdefghijklmnopqrstuvwxyz0123456789-_<> contains invalid char.")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no string is provided for a check against alphaAccentNumDashUnderscoreSpace`(value: String?) {
        // When
        val error = alphaAccentNumDashUnderscoreSpace(value, CREATE, TEST_INVALID_STRING)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a string with valid char is provided for a check against alphaAccentNumDashUnderscoreSpace`() {
        // When
        val error = alphaAccentNumDashUnderscoreSpace("abcdefghijklmnopqrstuvwxyzéèÊùö 0123456789-_", CREATE, TEST_INVALID_STRING)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a string with invalid char is provided for a check against alphaAccentNumDashUnderscoreSpace`() {
        // When
        val error = alphaAccentNumDashUnderscoreSpace("abcdefghijklmnopqrstuvwxyzéèÊùö 0123456789-_<>", CREATE, TEST_INVALID_STRING)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_800")
        assertThat(error.getMessage()).isEqualTo("Value abcdefghijklmnopqrstuvwxyzéèÊùö 0123456789-_<> contains invalid char.")
    }

    @ParameterizedTest
    @ArgumentsSource(NoListValueProvider::class)
    fun `no error whe an empty list is provided for a check against a list with no blank elements`(list: List<String>?) {
        // When
        val error = noBlankElement(list, "ZZZ", CREATE, TEST_LIST_WITH_BLANK)
        // then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a list with no blank element is provided for a check against a list with no blank elements`() {
        // Given
        val list = listOf("a", "2")
        // When
        val error = noBlankElement(list, "ZZZ", CREATE, TEST_LIST_WITH_BLANK)
        // then
        assertThat(error).isNull()
    }

    @Test
    fun `expect error when a list containing blank elements is provided for a check against a list with no blank elements`() {
        // Given
        val list = listOf("a", "", "   ", "2")
        // When
        val error = noBlankElement(list, "ZZZ", CREATE, TEST_LIST_WITH_BLANK)
        // then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_900")
        assertThat(error.getMessage()).isEqualTo("List contains 2 blank element(s).")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no value is provided for a check against a supported list of values`(value: String?) {
        // When
        val error = supportedValue(value, supportedValues, READ, DEFAULT_ERROR)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(SupportedValueProvider::class)
    fun `no error when a non-null non-blank and supported value is provided for a check against a supported list of values`(value: String) {
        // When
        val error = supportedValue(value, supportedValues, READ, DEFAULT_ERROR)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when a non-null non-blank and unsupported value is provided for a check against a supported list of values`() {
        // When
        val error = supportedValue("AAAAA", supportedValues, READ, TEST_UNSUPPORTED_VALUE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_GET_100")
        assertThat(error.getMessage()).isEqualTo("Value AAAAA is not supported. Supported values are: a, b, c")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no uuid is provided`(value: String?) {
        // When
        val error = validUUID(value, "TEST", READ, INVALID_UUID)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a valid uuid is provided`() {
        // When
        val error = validUUID(UUID.randomUUID().toString(), "TEST", READ, INVALID_UUID)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when an invalid uuid is provided`() {
        // When
        val error = validUUID("AAAAA", "TEST", READ, INVALID_UUID)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("TEST_GET_1010")
        assertThat(error.getMessage()).isEqualTo("Invalid UUID has been provided 'AAAAA'.")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when no date is provided`(value: String?) {
        // When
        val error = validDate(value, CREATE, TEST_INVALID_DATE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when a date is provided with milliseconds`() {
        // When
        val error = validDate("2020-10-01T12:34:00.123456Z", CREATE, TEST_INVALID_DATE_NANO)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when an invalid date is provided`() {
        // When
        val error = validDate("AAAAA", CREATE, TEST_INVALID_DATE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_300")
        assertThat(error.getMessage()).isEqualTo("Invalid date AAAAA")
    }

    @Test
    fun `no error when a valid date is provided`() {
        // When
        val error = validDate("2020-09-23T15:45:00-05:00", CREATE, TEST_INVALID_DATE)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when a no time is provided`(value: String?) {
        // When
        val error = validTime(value, CREATE, TEST_INVALID_TIME)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when a valid time is provided`() {
        // When
        val error = validTime("09:10", CREATE, TEST_INVALID_TIME)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["-1", "abc", "13", "13h", "1:5", "1:11", "13:4", "24:00"])
    fun `expect error when an invalid time is provided`(value: String) {
        // When
        val error = validTime(value, CREATE, TEST_INVALID_TIME)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_301")
        assertThat(error.getMessage()).isEqualTo("Invalid time $value")
    }

    @ParameterizedTest
    @ArgumentsSource(NoStringValueProvider::class)
    fun `no error when a no timezone is provided`(value: String?) {
        // When
        val error = validTimezone(value, CREATE, TEST_INVALID_TIMEZONE)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ArgumentsSource(ValidTimezoneProvider::class)
    fun `no error when a valid timezone is provided`(value: String) {
        // When
        val error = validTimezone(value, CREATE, TEST_INVALID_TIMEZONE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `expect error when an invalid timezone is provided`() {
        // When
        val error = validTimezone("invalid", CREATE, TEST_INVALID_TIMEZONE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_POST_302")
        assertThat(error.getMessage()).isEqualTo("Invalid timezone invalid")
    }







    @ParameterizedTest
    @ArgumentsSource(InvalidStartEndCombinationProvider::class)
    fun `error when start time is after end time`(testCase: StartEndCombination) {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = testCase.startTime, beforeThan = testCase.endTime, operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_GET_500")
        assertThat(error.getMessage()).isEqualTo("End (${testCase.endTime}) before or equal to start (${testCase.startTime})")
    }

    @ParameterizedTest
    @ArgumentsSource(ValidStartEndCombinationProvider::class)
    fun `no error when start time is before end time`(testCase: StartEndCombination) {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = testCase.startTime, beforeThan = testCase.endTime, operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when start time is null`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = null, beforeThan = "2020-10-01T20:00:00Z", operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when start time is not valid`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = "invalid", beforeThan = "2020-10-01T20:00:00Z", operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when end time is null`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = "2020-10-01T20:00:00Z", beforeThan = null, operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when end time is not valid`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = "2020-10-01T20:00:00Z", beforeThan = "invalid", operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when end time is has second greater than 0`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = "2020-10-01T20:00:00Z", beforeThan = "2020-10-01T20:00:01Z", operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when end time is has nanosecond greater than 0`() {
        // When
        val error = Validations.dateTimeBeforeThan(dateTime = "2020-10-01T20:00:00Z", beforeThan = "2020-10-01T20:00:00.000001Z", operation = READ, errorToReturn = TEST_INVALID_START_END_DATES)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ValueSource(doubles = [0.0, 5.0])
    fun `no error when double is greater or equals to 0`(value: Double) {
        // When
        val error = Validations.greaterOrEqualsTo(value = value, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when greater or equals has null value`() {
        // When
        val error = Validations.greaterOrEqualsTo(value = null, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when double is not greater or equals to 0`() {
        // When
        val error = Validations.greaterOrEqualsTo(value = -1.0, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_GET_1000")
        assertThat(error.getMessage()).isEqualTo("Invalid value -1.0")
    }

    @Test
    fun `no error when double is greater than 0`() {
        // When
        val error = Validations.greaterThan(value = 5.0, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when greater than has null value`() {
        // When
        val error = Validations.greaterThan(value = null, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @ParameterizedTest
    @ValueSource(doubles = [0.0, -1.0])
    fun `error when double is not greater than 0`(value: Double) {
        // When
        val error = Validations.greaterThan(value = value, minValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_GET_1000")
        assertThat(error.getMessage()).isEqualTo("Invalid value $value")
    }

    @ParameterizedTest
    @ValueSource(doubles = [0.0, -1.0])
    fun `no error when double is lower or equals to 0`(value: Double) {
        // When
        val error = Validations.lowerOrEqualsTo(value = value, maxValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `no error when lower or equals has null value`() {
        // When
        val error = Validations.lowerOrEqualsTo(value = null, maxValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertThat(error).isNull()
    }

    @Test
    fun `error when double is not lower or equals to 0`() {
        // When
        val error = Validations.lowerOrEqualsTo(value = 5.0, maxValue = 0.0, operation = READ, invalidValueError = TEST_INVALID_DOUBLE)
        // Then
        assertNotNull(error)
        assertThat(error.getCode()).isEqualTo("ZZZ_GET_1000")
        assertThat(error.getMessage()).isEqualTo("Invalid value 5.0")
    }

    class UnsupportedValueApplicationError(status: HttpStatus, code: Int, messageTemplate: String) : com.pierre2803.functionapp.ApplicationError(status = status, code = code, messageTemplate = messageTemplate, resourceType = resource, operation = READ) {
        companion object {
            const val resource = "ZZZ"
        }
    }

    private val TEST_UNSUPPORTED_VALUE = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 100, messageTemplate = "Value %s is not supported. Supported values are: %s")

    private val TEST_NO_NAME = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 200, messageTemplate = "No value provided.")

    private val TEST_INVALID_DATE = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 300, messageTemplate = "Invalid date %s")

    private val TEST_INVALID_TIME = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 301, messageTemplate = "Invalid time %s")

    private val TEST_INVALID_TIMEZONE = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 302, messageTemplate = "Invalid timezone %s")

    private val TEST_INVALID_DATE_NANO = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 300, messageTemplate = "Invalid date %s because of nano seconds")

    private val TEST_INVALID_START_END_DATES = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 500, messageTemplate = "End (%s) before or equal to start (%s)")

    private val TEST_VALUE_TOO_LONG = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 600, messageTemplate = "Value too long. Max is %d.")

    private val TEST_NOT_POSITIVE_LONG = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 700, messageTemplate = "Value %s is not a valid and positive long.")

    private val TEST_INVALID_STRING = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 800, messageTemplate = "Value %s contains invalid char.")

    private val TEST_LIST_WITH_BLANK = GeneralApplicationError(
        status = BAD_REQUEST,
        code = 900,
        messageTemplate = "List contains %d blank element(s)."
    )

    private val TEST_INVALID_DOUBLE = UnsupportedValueApplicationError(status = BAD_REQUEST, code = 1000, messageTemplate = "Invalid value %.1f")

    private class NoStringValueProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return listOf(null, "", "  ").map { Arguments.of(it) }.stream()
        }
    }

    private class NoListValueProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return listOf(null, emptyList<Any>()).map { Arguments.of(it) }.stream()
        }
    }

    private class ValidTimezoneProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return TimeZone.getAvailableIDs().map { Arguments.of(it) }.stream()
        }
    }

    private class SupportedValueProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return supportedValues.map { Arguments.of(it) }.stream()
        }
    }

    private class InvalidStartEndCombinationProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return listOf(
                    StartEndCombination(startTime = "2020-10-01T20:00:00Z", endTime = "2020-10-01T20:00:00Z"),              // start = end (same timezone)
                    StartEndCombination(startTime = "2020-10-01T20:00:00Z", endTime = "2020-10-01T15:00:00-05:00"),         // start = end (different timezones)
                    StartEndCombination(startTime = "2020-10-02T04:00:00+08:00", endTime = "2020-10-01T15:00:00-05:00"),    // start = end (different timezones)
                    StartEndCombination(startTime = "2020-10-01T20:00:00Z", endTime = "2020-10-01T19:59:00Z"),              // start > end (same timezone)
                    StartEndCombination(startTime = "2020-10-01T20:00:00Z", endTime = "2020-10-01T14:59:00-05:00"),         // start = end (different timezones)
                    StartEndCombination(startTime = "2020-10-02T04:00:00+08:00", endTime = "2020-10-01T14:59:00-05:00"))    // start = end (different timezones)
                    .map { Arguments.of(it) }.stream()
        }
    }

    private class ValidStartEndCombinationProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return listOf(
                    StartEndCombination(startTime = "2020-10-01T19:59:00Z", endTime = "2020-10-01T20:00:00Z"),              // start < end (same timezone)
                    StartEndCombination(startTime = "2020-10-01T14:59:00-05:00", endTime = "2020-10-01T20:00:00Z"),         // start < end (different timezones)
                    StartEndCombination(startTime = "2020-10-01T14:59:00-05:00", endTime = "2020-10-02T04:00:00+08:00"))    // start < end (different timezones)
                    .map { Arguments.of(it) }.stream()
        }
    }

    internal data class StartEndCombination(val startTime: String, val endTime: String)
}