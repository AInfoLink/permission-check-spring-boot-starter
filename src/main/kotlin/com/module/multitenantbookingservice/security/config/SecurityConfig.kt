package com.module.multitenantbookingservice.security.config

import com.app.security.repository.model.Role
import com.module.multitenantbookingservice.security.web.filter.UserJwtAuthenticationFilter
import com.module.multitenantbookingservice.security.service.UserJwtService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@ConfigurationProperties(prefix = "web")
class SecurityProperties(
    /**
     * List of routes that do not require authentication.
     * These routes are accessible without any security checks.
     */
    @Value("\${unprotected-routes}") val unprotectedRoutes: List<String>,
    @Value("\${jwt-secret}") val jwtSecret: String
)


@Configuration
class SpringSecurityConfig(
    private val securityProperties: SecurityProperties,
    private val userJwtService: UserJwtService,
    @Qualifier("DefaultAccessDeniedHandler") val accessDeniedHandler: AccessDeniedHandler,
) {

    private val unprotectedRoutes = securityProperties.unprotectedRoutes

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {
        http
            .exceptionHandling { it.accessDeniedHandler(accessDeniedHandler) }
            .sessionManagement { sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(userJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .cors { cors -> cors.configurationSource(withDefaultCorsConfigurationSource()) }
            .authorizeHttpRequests(withDefaultChain())

            .csrf { it.disable() }
        return http.build()
    }



    @Bean
    fun httpFirewall(): HttpFirewall {
        val defaultHttpFirewall = StrictHttpFirewall()
        defaultHttpFirewall.setAllowUrlEncodedSlash(true)
        defaultHttpFirewall.setAllowBackSlash((true))
        defaultHttpFirewall.setAllowSemicolon(true)
        return defaultHttpFirewall
    }

    fun withDefaultChain(): Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

        return Customizer { auth ->
            auth
                .requestMatchers(*unprotectedRoutes.toTypedArray()).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
        }
    }

    fun userJwtAuthenticationFilter(): UserJwtAuthenticationFilter {
        return UserJwtAuthenticationFilter(userJwtService, unprotectedRoutes)
    }

    fun withDefaultCorsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("*")
        config.allowedMethods = listOf("*")
        config.allowCredentials = true;
        config.allowedHeaders = listOf("*")
        config.maxAge = 3600L;
        val source = UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    fun defaultPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy {
        val hierarchyChain = Role.entries.joinToString(" > ", transform = { it.value })
        return RoleHierarchyImpl.fromHierarchy(hierarchyChain)
    }

    // and, if using pre-post method security also add
    @Bean
    fun methodSecurityExpressionHandler(roleHierarchy: RoleHierarchy): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setRoleHierarchy(roleHierarchy)
        return expressionHandler
    }


}