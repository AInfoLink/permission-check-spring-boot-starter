package com.module.multitenantbookingservice.commons.contract

interface ValidationRequired {
    fun validate(): MutableSet<Exception>
}
