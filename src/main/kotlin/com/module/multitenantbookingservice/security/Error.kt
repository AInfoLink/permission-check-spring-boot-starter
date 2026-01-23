package com.module.multitenantbookingservice.security

import org.springframework.http.HttpStatus

data class AppDomainException(
    val code: String,
    val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
): Exception(code)

// 404 Not Found - Resource not found
val UserNotFound = AppDomainException("UserNotFound", statusCode = HttpStatus.NOT_FOUND)
val UserProfileNotCreated = AppDomainException("UserProfileNotCreated", statusCode = HttpStatus.NOT_FOUND)

// 409 Conflict - Resource already exists
val UserProfileAlreadyExists = AppDomainException("UserProfileAlreadyExists", statusCode = HttpStatus.CONFLICT)