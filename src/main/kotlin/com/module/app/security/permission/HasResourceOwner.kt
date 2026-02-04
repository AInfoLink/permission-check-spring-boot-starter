package com.module.app.security.permission

interface HasResourceOwner {
    fun getResourceOwnerId(): String
}