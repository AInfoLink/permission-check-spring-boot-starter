package com.module.multitenantbookingservice.security.web.controller
import com.module.multitenantbookingservice.monitoring.otel.TraceUtils
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val timestamp: Long,
    val status: HttpStatus,
    val error: String?,
    val path: String,
    val traceId: String
)


@RestControllerAdvice
class ControllerAdvise {
    private val logger = LoggerFactory.getLogger(ControllerAdvise::class.java)

    val skippedExceptions = setOf(
        AuthenticationException::class,
        AccessDeniedException::class
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidExceptionHandler(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = exception.bindingResult.allErrors.joinToString(", ") { it.defaultMessage ?: "" }
        logger.warn("Validation error on ${request.method} ${request.requestURI} - Errors: $errors, TraceId: ${TraceUtils.getTraceId()}")
        
        val errorResponse = ErrorResponse(
            System.currentTimeMillis(),
            HttpStatus.BAD_REQUEST,
            errors,
            request.requestURI,
            TraceUtils.getTraceId()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @Order(Int.MAX_VALUE)
    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val traceId = TraceUtils.getTraceId()

        if (skippedExceptions.any { it.isInstance(exception) }) {
            logger.debug("Skipping logging for ${exception::class.simpleName} on ${request.method} ${request.requestURI} - TraceId: $traceId")
            throw exception
        }

        logger.error("Unhandled exception on ${request.method} ${request.requestURI} - Error: ${exception.message}, TraceId: $traceId", exception)

        val errorResponse = ErrorResponse(
            System.currentTimeMillis(),
            HttpStatus.BAD_REQUEST,
            exception.message,
            request.requestURI,
            traceId
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

}