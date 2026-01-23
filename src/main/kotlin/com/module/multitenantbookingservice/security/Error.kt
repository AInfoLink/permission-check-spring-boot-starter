package com.app.security

import com.app.core.AppDomainException
import org.springframework.http.HttpStatus

// 404 Not Found - Resource not found
val UserNotFound = AppDomainException("UserNotFound", statusCode = HttpStatus.NOT_FOUND)
val OrganizationNotFound = AppDomainException("OrganizationNotFound", statusCode = HttpStatus.NOT_FOUND)

// 409 Conflict - Business logic conflicts
val OrganizationAlreadyExists = AppDomainException("OrganizationAlreadyExists", statusCode = HttpStatus.CONFLICT)