package com.module.multitenantbookingservice.core.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID


@Entity
@Table(name = "memberships")
class Membership(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 100)
    val name: String,


    @Column(name = "description", length = 500)
    val description: String? = null,

    @OneToMany(mappedBy = "membership")
    val profiles: MutableSet<UserProfile> = mutableSetOf(),

    @OneToOne(mappedBy = "membership")
    val pricing: MembershipPricing,
)


@Entity
@Table(name = "membership_pricings")
class MembershipPricing(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne
    @JoinColumn(name = "membership_id", nullable = false)
    val membership: Membership,

    @Column(name = "discount_percentage", nullable = false)
    val discountPercentage: Double,
)