package com.module.app.core.web.admin

import com.module.app.core.tenant.service.TenantInitializer
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
data class InitializeTenantRequest(
    val tenantId: String
)


@RestController
@RequestMapping("/api/admin/tenant-initialization")
class TenantInitializationController(
    val tenantInitializer: TenantInitializer
) {
    @PostMapping("/initialize-tenant")
    fun initializeTenantData(request: InitializeTenantRequest): String {
        tenantInitializer.initialize(request.tenantId)
        return "Tenant data initialization triggered."
    }
}