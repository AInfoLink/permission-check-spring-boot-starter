package com.module.multitenantbookingservice.utils

import org.slf4j.Logger


class Safe {
    companion object {
        fun <T> call(logger: Logger,action: () -> T): T?{
            try {
                return action()
            } catch (exception: Exception) {
                // Log the exception or handle it as needed
                logger.error("Exception caught in Safe.callSafely: ${exception.message}")
                return null
            }
        }
    }
}