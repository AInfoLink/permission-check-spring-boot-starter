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
val TenantRoleNotFound = AppDomainException("TenantRoleNotFound", statusCode = HttpStatus.NOT_FOUND)

// 409 Conflict - Resource already exists
val UserProfileAlreadyExists = AppDomainException("UserProfileAlreadyExists", statusCode = HttpStatus.CONFLICT)
val VenueAlreadyExists = AppDomainException("VenueAlreadyExists", statusCode = HttpStatus.CONFLICT)
val VenueGroupAlreadyExists = AppDomainException("VenueGroupAlreadyExists", statusCode = HttpStatus.CONFLICT)
val ItemCategoryAlreadyExists = AppDomainException("ItemCategoryAlreadyExists", statusCode = HttpStatus.CONFLICT)

// 403 Forbidden - Operation not allowed
val SystemManagedCategoryModificationDenied = AppDomainException(
    "SystemManagedCategoryModificationDenied",
    statusCode = HttpStatus.FORBIDDEN
)
val SystemManagedCategoryDeletionDenied = AppDomainException(
    "SystemManagedCategoryDeletionDenied",
    statusCode = HttpStatus.FORBIDDEN
)

// 404 Not Found - ItemCategory resources
val ItemCategoryNotFound = AppDomainException("ItemCategoryNotFound", statusCode = HttpStatus.NOT_FOUND)

// 404 Not Found - Order resources
val OrderNotFound = AppDomainException("OrderNotFound", statusCode = HttpStatus.NOT_FOUND)
val OrderItemNotFound = AppDomainException("OrderItemNotFound", statusCode = HttpStatus.NOT_FOUND)
val OrderIdentityNotFound = AppDomainException("OrderIdentityNotFound", statusCode = HttpStatus.NOT_FOUND)

// 409 Conflict - Order resources already exist
val OrderIdentityAlreadyExists = AppDomainException("OrderIdentityAlreadyExists", statusCode = HttpStatus.CONFLICT)

// 400 Bad Request - OrderItem business rule validation
val InvalidAmountForCategory = AppDomainException("InvalidAmountForCategory", statusCode = HttpStatus.BAD_REQUEST)
val OrderAlreadyPaidModificationDenied = AppDomainException("OrderAlreadyPaidModificationDenied", statusCode = HttpStatus.BAD_REQUEST)

// 422 Unprocessable Entity - OrderItem bulk operation failures
val BulkUpdatePartialFailure = AppDomainException("BulkUpdatePartialFailure", statusCode = HttpStatus.UNPROCESSABLE_ENTITY)