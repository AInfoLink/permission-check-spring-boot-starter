package com.module.app.system.tenancy.interceptor

import com.module.app.system.tenancy.context.TenantContextHolder
import com.module.app.system.tenancy.resolver.HttpHeaderTenantResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView


@Component
class TenantInterceptor(
    val tenantResolver: HttpHeaderTenantResolver
): HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(TenantInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestUri = request.requestURI
        val method = request.method

        val tenantId = tenantResolver.resolveTenantIdentifier(request)
        if (tenantId.isNullOrBlank()) {
            logger.warn("No tenant ID resolved for request: $method $requestUri - using default context")
            TenantContextHolder.setTenantId(null)
        } else {
            logger.debug("Tenant context set for request: $method $requestUri - tenant: $tenantId")
            TenantContextHolder.setTenantId(tenantId)
        }
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        clear()
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        clear()
    }

    private fun clear() {
        val currentTenantId = TenantContextHolder.getTenantId()
        if (currentTenantId != null) {
            logger.debug("Clearing tenant context for tenant: $currentTenantId")
        }
        TenantContextHolder.clear()
    }


}