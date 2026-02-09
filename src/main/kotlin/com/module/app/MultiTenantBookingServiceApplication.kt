package com.module.app

import io.github.common.permission.autoconfigure.EnablePermissionCheck
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableFeignClients
@EnableAspectJAutoProxy
@EnablePermissionCheck
class MultiTenantBookingServiceApplication

fun main(args: Array<String>) {
    runApplication<MultiTenantBookingServiceApplication>(*args)
}
