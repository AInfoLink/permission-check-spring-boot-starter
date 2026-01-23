package com.module.multitenantbookingservice.security.service

import io.netty.handler.codec.http.cookie.CookieHeaderNames
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface AuthService {
    fun logout(response: HttpServletResponse)
    fun getLoginEndpoint(): String

}

@Service
class DefaultAuthService(
    @Value("\${spring.application.name}")
    val appName: String,
    val authClient: AuthClient
) : AuthService {
    private val logger = LoggerFactory.getLogger(DefaultAuthService::class.java)
    
    override fun logout(response: HttpServletResponse) {
        logger.info("Processing user logout request")
        val logoutInfo = authClient.logout()
        logger.debug("Retrieved logout info with ${logoutInfo.keys.size} cookies to clear")
        
        logoutInfo.keys.forEach { cookieName ->
            val cookie = Cookie(cookieName, null)
            cookie.path = "/"
            cookie.isHttpOnly = true
            cookie.secure = true
            cookie.maxAge = 0
            cookie.domain = logoutInfo.domain
            cookie.setAttribute(CookieHeaderNames.SAMESITE, CookieHeaderNames.SameSite.None.toString())
            response.addCookie(cookie)
            logger.debug("Cleared cookie: $cookieName")
        }
        
        logger.info("User logout completed successfully")
    }

    override fun getLoginEndpoint(): String {
        logger.debug("Retrieving login endpoint for app: $appName")
        val endpoint = authClient.getLoginEndpoint(appName)
        logger.debug("Login endpoint retrieved: $endpoint")
        return endpoint
    }
}