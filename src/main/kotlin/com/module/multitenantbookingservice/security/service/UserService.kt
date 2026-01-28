package com.module.multitenantbookingservice.security.service

import com.module.multitenantbookingservice.security.repository.UserRepository
import com.module.multitenantbookingservice.security.model.User
import com.module.multitenantbookingservice.security.UserNotFound
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

interface UserService {
    fun register(user: User): User
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(pageable: Pageable): Iterable<User>
    @PreAuthorize("hasRole('ADMIN')")
    fun getByUsernameContaining(keyword: String, pageable: Pageable): Iterable<User>
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserById(userId: UUID): User
}

@Service
class DefaultUserService(
    private val userRepository: UserRepository,
) : UserService {

    private val logger = LoggerFactory.getLogger(DefaultUserService::class.java)

    @Transactional
    override fun register(user: User): User {
        logger.info("User registration initiated - ID: ${user.id}, username: ${user.username}")

        val optionalUser = userRepository.findById(user.id).getOrNull()
        if (optionalUser != null) {
            logger.info("Existing user found during registration - updating roles for user: ${user.id}")
            optionalUser.assumeRoles(user.systemRoles)
            val updatedUser = userRepository.save(optionalUser)
            logger.info("User roles updated successfully - user: ${user.id}")
            return updatedUser
        }

        val savedUser = userRepository.save(user)
        logger.info("New user registered successfully - ID: ${savedUser.id}, username: ${savedUser.username}")
        return savedUser
    }

    override fun getAllUsers(pageable: Pageable): Iterable<User> {
        logger.debug("Retrieving all users - page: ${pageable.pageNumber}, size: ${pageable.pageSize}")
        return userRepository.findAll(pageable)
    }

    override fun getByUsernameContaining(keyword: String, pageable: Pageable): Iterable<User> {
        logger.debug("Searching users by username containing: '$keyword'")
        return userRepository.findByUsernameContaining(keyword, pageable)
    }

    override fun getUserById(userId: UUID): User {
        logger.debug("Retrieving user by ID: $userId")
        return userRepository.findById(userId).getOrNull() ?: run {
            logger.warn("User not found by ID: $userId")
            throw UserNotFound
        }
    }
}