package com.module.multitenantbookingservice.security.permission

interface HasResourceOwner {
    fun getResourceOwnerId(): String
}