package com.pierre2803.functionapp

import com.microsoft.azure.functions.ExecutionContext
import java.util.logging.Level
import java.util.logging.Logger

object ErrorHandler {

    internal fun handle(t: Throwable, executionContext: ExecutionContext) {
        handleUnmappedException(t, executionContext.logger)
    }

    private fun handleUnmappedException(ex: Throwable, logger: Logger) {
        logger.log(Level.SEVERE, "Unhandled exception", ex)
    }
}