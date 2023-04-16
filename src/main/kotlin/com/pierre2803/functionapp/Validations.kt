package com.pierre2803.functionapp

import org.apache.commons.validator.routines.EmailValidator
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor
import java.util.*

object Validations {

    private val alphaNumDashUnderscoreRegex = "[a-zA-Z0-9-_]+\$".toRegex()
    private val alphaAccentNumDashUnderscoreSpaceRegex = "(?i)^(?:(?![×Þß÷þø])[-_ 0-9a-zÀ-ÿ])+\$".toRegex()
    private val alphaNumDashUnderscoreSharpDotRegex = "[a-zA-Z0-9-_#.]+\$".toRegex()
    private val alphaAccentNumDashUnderscoreSpaceSharpDotRegex = "(?i)^(?:(?![×Þß÷þø])[-_#.', 0-9a-zÀ-ÿ])+\$".toRegex()
    private val alphaAccentNumDashUnderscoreSpaceSharpDotParenthesisRegex = "(?i)^(?:(?![×Þß÷þø])[-)_(#./ 0-9a-zÀ-ÿ])+\$".toRegex()
    private val domainNamePatternRegex = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}\$".toRegex()
    private val numDotRegex = "[0-9.]+\$".toRegex()

    fun required(value: String?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value.isNullOrBlank()) noValueError.forOperation(operation).args(value ?: "") else null
    }

    fun required(value: Int?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: Any?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: Long?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: Double?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: List<Any>?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value.isNullOrEmpty()) noValueError.forOperation(operation) else null
    }

    fun required(value: Boolean?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value == null) noValueError.forOperation(operation) else null
    }

    fun required(value: String?, supportedValues: List<String>, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError, unsupportedEnumValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrBlank()) return noValueError.forOperation(operation)
        if (!supportedValues.contains(value)) {
            val supportedValuesAsString = supportedValues.joinToString(separator = ", ")
            return unsupportedEnumValueError.forOperation(operation).args(value, supportedValuesAsString)
        }
        return null
    }

    fun requiredNull(value: Int?, operation: Operation, noValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (value != null) noValueError.forOperation(operation) else null
    }

    fun validUUID(value: String?, resourceType: String, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError.GeneralApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            UUID.fromString(value)
            null
        } catch (_: Throwable) {
            invalidValueError.resource(resourceType).forOperation(operation).args(value)
        }
    }

    fun isValidUUID(value: String): Boolean {
        return try {
            UUID.fromString(value)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun validateUUIDS(list: List<String>?, resourceType: String, operation: Operation, applicationError: com.pierre2803.functionapp.ApplicationError.GeneralApplicationError): List<com.pierre2803.functionapp.ApplicationError>? {
        return list?.mapNotNull { validateUUID(it, resourceType, operation, applicationError) }
    }

    private fun validateUUID(value: String?, resourceType: String, operation: Operation, error: com.pierre2803.functionapp.ApplicationError.GeneralApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrEmpty()) return error.resource(resourceType).forOperation(operation).args("null")
        return try {
            UUID.fromString(value)
            null
        } catch (ex: IllegalArgumentException) {
            return error.resource(resourceType).forOperation(operation).args(value)
        }
    }

    fun maxLength(value: String?, maxLength: Int, operation: Operation, valueTooLongError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && value.trim().length > maxLength) valueTooLongError.forOperation(operation).args(Pair("max_length", maxLength)) else null
    }

    fun positiveDouble(value: Double?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value == null) return null
        return if (value.isNaN() || value <= 0) invalidValueError.forOperation(operation).args(value.toString()) else null
    }

    fun positiveNumber(value: Long?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value == null) return null
        return if (value <= 0) invalidValueError.forOperation(operation).args(value) else null
    }

    fun validatePlannedDuration(value: Boolean?, duration: Long?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (duration == null) return null
        return if (value != true) invalidValueError.forOperation(operation).args(duration) else null
    }

    fun <T : Comparable<T>> greaterOrEqualsTo(value: T?, minValue: T, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value == null) return null
        return if (value < minValue) invalidValueError.forOperation(operation).args(value, minValue) else null
    }

    fun <T : Comparable<T>> greaterThan(value: T?, minValue: T, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value == null) return null
        return if (value <= minValue) invalidValueError.forOperation(operation).args(value, minValue) else null
    }

    fun <T : Comparable<T>> lowerOrEqualsTo(value: T?, maxValue: T, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value == null) return null
        return if (value > maxValue) invalidValueError.forOperation(operation).args(value, maxValue) else null
    }

    fun validCharacters(value: String?, regex: Regex, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(regex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaNumDashUnderscore(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaNumDashUnderscoreRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaAccentNumDashUnderscoreSpace(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaAccentNumDashUnderscoreSpaceRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaNumDashUnderscoreSharpDotRegex(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaNumDashUnderscoreSharpDotRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaAccentNumDashUnderscoreSpaceSharpDotRegex(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaAccentNumDashUnderscoreSpaceSharpDotRegex)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun alphaAccentNumDashUnderscoreSpaceSharpDotParenthesisRegex(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(alphaAccentNumDashUnderscoreSpaceSharpDotParenthesisRegex)) invalidValueError.forOperation(operation).args(value) else null
    }
    fun isAlphaAccentNumDashUnderscoreSpaceSharpDotRegex(value: String?): Boolean = value?.trim()?.matches(alphaAccentNumDashUnderscoreSpaceSharpDotRegex) ?: false

    fun isNumDotRegex(value: String?): Boolean = value?.trim()?.matches(numDotRegex) ?: false

    fun isEmailValid(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if(!value.isNullOrBlank() && !EmailValidator.getInstance().isValid(value)) invalidValueError.forOperation(operation).args(value) else null
    }

    fun isAlphaNumDashUnderscoreSharpDotRegex(value: String?): Boolean = value?.trim()?.matches(alphaNumDashUnderscoreSharpDotRegex) ?: false

    fun noBlankElement(list: List<String>?, resourceType: String, operation: Operation, blankListElementError: com.pierre2803.functionapp.ApplicationError.GeneralApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (list.isNullOrEmpty()) return null
        val blankCount = list.count { it.isBlank() }
        return if (blankCount > 0) blankListElementError.resource(resourceType).forOperation(operation).args(blankCount) else null
    }

    fun supportedValue(value: String?, supportedValues: List<String>, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrBlank()) return null
        if (!supportedValues.contains(value.trim())) {
            return invalidValueError.forOperation(operation).args(value, supportedValues.joinToString())
        }
        return null
    }

    fun validDate(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            ISO_OFFSET_DATE_TIME.parse(value.trim())
            null
        } catch (_: Throwable) {
            invalidValueError.forOperation(operation).args(value)
        }
    }

    fun validTime(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        if (value.isNullOrBlank()) return null
        return try {
            ISO_LOCAL_TIME.parse(value.trim())
            null
        } catch (_: Throwable) {
            invalidValueError.forOperation(operation).args(value)
        }
    }

    fun dateTimeBeforeThan(dateTime: String?, beforeThan: String?, operation: Operation, errorToReturn: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        val start = tryToParseDateTime(dateTime?.trim())
        val end = tryToParseDateTime(beforeThan?.trim())
        if (start != null && end != null && isAfterOrEqualDateTime(start, end)) {
            val arg1 = beforeThan ?: end
            val arg2 = dateTime ?: start
            return errorToReturn.forOperation(operation).args(arg1, arg2)
        }
        return null
    }

    fun dateBeforeThan(date: String?, beforeThan: String?, operation: Operation, errorToReturn: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        val start = tryToParseDate(date?.trim())
        val end = tryToParseDate(beforeThan?.trim())
        if (start != null && end != null && isAfterOrEqualDate(start, end)) {
            return errorToReturn.forOperation(operation)
        }
        return null
    }

    fun isValidDate(date: String?, operation: Operation, errorToReturn: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        try {
            if (!date.isNullOrBlank()) {
                LocalDate.parse(date)
                return null
            }
            return null
        } catch (ex: DateTimeParseException) {
            return errorToReturn.forOperation(operation)
        }
    }

    private fun isAfterOrEqualDateTime(start: Instant, end: Instant?) = start.isAfter(end) || start == end

    private fun isAfterOrEqualDate(start: LocalDate, end: LocalDate) = start.isAfter(end) || start == end

    fun validTimezone(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !TimeZone.getAvailableIDs().contains(value.trim())) invalidValueError.forOperation(operation).args(value) else null
    }

    private fun tryToParseDateTime(v: String?): Instant? {
        return try {
            ISO_OFFSET_DATE_TIME.parse(v, ZonedDateTime::from).toInstant()
        } catch (ex: Throwable) {
            null
        }
    }

    private fun tryToParseDate(v: String?): LocalDate? {
        return try {
            LocalDate.parse(v)
        } catch (ex: Throwable) {
            null
        }
    }

    fun parseValidDate(value: String): TemporalAccessor = ISO_OFFSET_DATE_TIME.parse(value.trim())

    fun validDomainName(value: String?, operation: Operation, invalidValueError: com.pierre2803.functionapp.ApplicationError): com.pierre2803.functionapp.ApplicationError? {
        return if (!value.isNullOrBlank() && !value.trim().matches(domainNamePatternRegex)) invalidValueError.forOperation(operation).args(value) else null
    }
}
