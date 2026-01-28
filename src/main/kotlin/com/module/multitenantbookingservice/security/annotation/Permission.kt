package com.module.multitenantbookingservice.security.annotation

/**
 * System permission enumeration for unified management of all permission definitions
 * Permission format: <domain>:<action>[:<scope>]
 */
enum class Permission(private val value: String) {
    // User Management (Users)
    USERS_READ("users:read"),
    USERS_CREATE("users:create"),
    USERS_UPDATE("users:update"),
    USERS_DELETE("users:delete"),
    USERS_ALL("users:*"),

    // Role Management (Roles)
    ROLES_READ("roles:read"),
    ROLES_CREATE("roles:create"),
    ROLES_UPDATE("roles:update"),
    ROLES_DELETE("roles:delete"),
    ROLES_ASSIGN("roles:assign"),
    ROLES_UNASSIGN("roles:unassign"),
    ROLES_ALL("roles:*"),

    // Order Management (Orders)
    ORDERS_READ("orders:read"),
    ORDERS_CREATE("orders:create"),
    ORDERS_UPDATE("orders:update"),
    ORDERS_DELETE("orders:delete"),
    ORDERS_REFUND("orders:refund"),
    ORDERS_ALL("orders:*"),

    // Order Item Management (OrderItems)
    ORDER_ITEMS_READ("orderitems:read"),
    ORDER_ITEMS_CREATE("orderitems:create"),
    ORDER_ITEMS_UPDATE("orderitems:update"),
    ORDER_ITEMS_DELETE("orderitems:delete"),
    ORDER_ITEMS_ALL("orderitems:*"),

    // Venue Management (Venues)
    VENUES_READ("venues:read"),
    VENUES_CREATE("venues:create"),
    VENUES_UPDATE("venues:update"),
    VENUES_DELETE("venues:delete"),
    VENUES_ALL("venues:*"),

    // Venue Group Management (VenueGroups)
    VENUE_GROUPS_READ("venuegroups:read"),
    VENUE_GROUPS_CREATE("venuegroups:create"),
    VENUE_GROUPS_UPDATE("venuegroups:update"),
    VENUE_GROUPS_DELETE("venuegroups:delete"),
    VENUE_GROUPS_ALL("venuegroups:*"),

    // Booking Management (Bookings)
    BOOKINGS_READ("bookings:read"),
    BOOKINGS_CREATE("bookings:create"),
    BOOKINGS_UPDATE("bookings:update"),
    BOOKINGS_CANCEL("bookings:cancel"),
    BOOKINGS_ALL("bookings:*"),

    // Wallet Management (Wallets)
    WALLETS_READ("wallets:read"),
    WALLETS_RECHARGE("wallets:recharge"),
    WALLETS_ADJUST("wallets:adjust"),
    WALLETS_ALL("wallets:*"),

    // Category Management (Categories)
    CATEGORIES_READ("categories:read"),
    CATEGORIES_CREATE("categories:create"),
    CATEGORIES_UPDATE("categories:update"),
    CATEGORIES_DELETE("categories:delete"),
    CATEGORIES_ALL("categories:*"),

    // System-wide Permissions
    SYSTEM_ALL("*:*");

    override fun toString(): String = value
}