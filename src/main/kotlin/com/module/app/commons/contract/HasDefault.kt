package com.module.app.commons.contract

interface HasDefault<T> {
    fun withDefault(): T
}