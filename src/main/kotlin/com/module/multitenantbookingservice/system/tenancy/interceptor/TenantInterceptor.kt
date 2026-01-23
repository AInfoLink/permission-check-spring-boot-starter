package com.module.multitenantbookingservice.system.tenancy.interceptor

import com.module.multitenantbookingservice.system.tenancy.context.TenantContextHolder
import com.module.multitenantbookingservice.system.tenancy.resolver.HttpHeaderTenantResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView


@Component
class TenantInterceptor(
    val tenantResolver: HttpHeaderTenantResolver
): HandlerInterceptor {


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val tenantId = tenantResolver.resolveTenantIdentifier(request)
        TenantContextHolder.setTenantId(tenantId)
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
        TenantContextHolder.clear()
    }


}