package com.app.security.repository

import com.app.security.repository.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: CrudRepository<User, UUID> {
    fun findAll(pageable: Pageable): Iterable<User>
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun findByUsernameContaining(keyword: String, pageable: Pageable): Page<User>
}