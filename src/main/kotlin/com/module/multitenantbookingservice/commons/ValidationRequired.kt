package com.module.multitenantbookingservice.commons

interface ValidationRequired {
    fun validate(): MutableSet<Exception>
}
