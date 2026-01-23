package com.module.multitenantbookingservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<MultiTenantBookingServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
