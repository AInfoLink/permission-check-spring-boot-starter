package com.module.multitenantbookingservice.system.tenancy.resolver

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class HttpHeaderTenantResolver(
    @Value("\${tenancy.http-header-name:X-Tenant-ID}")
    val headerName: String
): TenantResolver<HttpServletRequest> {
    override fun resolveTenantIdentifier(request: HttpServletRequest): String? {
         return request.getHeader(headerName)
    }
}
