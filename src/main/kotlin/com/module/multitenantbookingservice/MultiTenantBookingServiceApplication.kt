package com.module.multitenantbookingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MultiTenantBookingServiceApplication

fun main(args: Array<String>) {
    runApplication<MultiTenantBookingServiceApplication>(*args)
}
