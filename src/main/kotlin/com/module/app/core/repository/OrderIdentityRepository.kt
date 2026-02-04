package com.module.app.core.repository

import com.module.app.core.models.IdentityType
import com.module.app.core.models.OrderIdentity
import com.module.app.security.model.User
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderIdentityRepository : ListCrudRepository<OrderIdentity, UUID> {
    fun findByEmail(email: String): Optional<OrderIdentity>
    fun findByUser(user: User): List<OrderIdentity>
    fun findByType(type: IdentityType): List<OrderIdentity>
    fun findByEmailAndType(email: String, type: IdentityType): Optional<OrderIdentity>
}