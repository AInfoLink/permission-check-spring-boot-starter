package com.module.app.system.tenancy.resolver

import org.jetbrains.annotations.NotNull


interface TenantResolver<T> {
    fun resolveTenantIdentifier(@NotNull request: T): String?
}