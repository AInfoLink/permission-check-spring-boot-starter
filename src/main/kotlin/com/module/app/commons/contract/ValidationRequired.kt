package com.module.app.commons.contract

interface ValidationRequired {
    fun validate(): MutableSet<Exception>
}
