package com.app.security.service

import com.app.core.config.AdminProperties
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class SystemInitService(
    private val adminProperties: AdminProperties,
) {

    private val logger = LoggerFactory.getLogger(SystemInitService::class.java)

    @PostConstruct
    @Transactional
    fun init() {
        // Initialize the system, e.g., create default admin user if it doesn't exist
        this.initSystemUser()
    }
    /**
     * Initializes the system user with admin privileges.
     * This method is called during application startup to ensure that
     * there is always an admin user available.
     */
    fun initSystemUser() {
        logger.info("Initializing system user with admin privileges...")
    }
}