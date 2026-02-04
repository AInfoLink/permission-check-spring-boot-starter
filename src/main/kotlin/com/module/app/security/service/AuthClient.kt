package com.module.app.security.service
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

data class LogoutResponse(
    val keys: List<String>,
    val domain: String
)

@FeignClient(name = "auth-client", url = "\${auth.client.url}")
interface AuthClient {
    @GetMapping("/api/public/auth/logout-info")
    fun logout(): LogoutResponse

    @GetMapping("/api/public/applications/login-endpoint/{name}")
    fun getLoginEndpoint(@PathVariable name: String): String
}