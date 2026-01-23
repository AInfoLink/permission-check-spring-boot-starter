package com.module.multitenantbookingservice.security.service

import com.module.multitenantbookingservice.security.repository.UserRepository
import com.app.security.repository.model.User
import com.module.multitenantbookingservice.security.UserNotFound
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

    @Transactional
    override fun register(user: User): User {
        val optionalUser = userRepository.findById(user.id).getOrNull()
        if (optionalUser != null) {
            optionalUser.assumeRoles(user.systemRoles)
            return userRepository.save(optionalUser)
        }
        user.verify()
        val savedUser = userRepository.save(user)
        return savedUser
    }

    override fun getAllUsers(pageable: Pageable): Iterable<User> {
        return userRepository.findAll(pageable)
    }

    override fun getByUsernameContaining(keyword: String, pageable: Pageable): Iterable<User> {
        return userRepository.findByUsernameContaining(keyword, pageable)
    }

    override fun getUserById(userId: UUID): User {
        return userRepository.findById(userId).getOrNull() ?: throw UserNotFound
    }
}