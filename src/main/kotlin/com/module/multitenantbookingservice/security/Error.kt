package com.module.multitenantbookingservice.security

import org.springframework.http.HttpStatus

data class AppDomainException(
    val code: String,
    val statusCode: HttpStatus = HttpStatus.BAD_REQUEST,
    val details: String? = null
): Exception(code) {
    fun withDetails(details: String): AppDomainException {
        return AppDomainException(
            code = this.code,
            statusCode = this.statusCode,
            details = details
        )
    }
}

// 404 Not Found - Resource not found
val UserNotFound = AppDomainException("UserNotFound", statusCode = HttpStatus.NOT_FOUND)
val UserProfileNotCreated = AppDomainException("UserProfileNotCreated", statusCode = HttpStatus.NOT_FOUND)
val VenueNotFound = AppDomainException("VenueNotFound", statusCode = HttpStatus.NOT_FOUND)
val VenueGroupNotFound = AppDomainException("VenueGroupNotFound", statusCode = HttpStatus.NOT_FOUND)

// 409 Conflict - Resource already exists
val UserProfileAlreadyExists = AppDomainException("UserProfileAlreadyExists", statusCode = HttpStatus.CONFLICT)
val VenueAlreadyExists = AppDomainException("VenueAlreadyExists", statusCode = HttpStatus.CONFLICT)
val VenueGroupAlreadyExists = AppDomainException("VenueGroupAlreadyExists", statusCode = HttpStatus.CONFLICT)