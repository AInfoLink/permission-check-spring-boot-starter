variable "tenant" {
  type = string
  description = "The ID of the tenant"
}

// Define the schema, "tenant" here is a placeholder for the final
// schema name that will be defined at runtime.
schema "tenant" {
  // Reference to the input variable.
  name = var.tenant
}

enum "order_item_reference_type" {
  schema = schema.tenant
  values = [
    "VENUE_BOOKING_REQUEST",
    "MEMBERSHIP_UPGRADE",
    "BOOKING_ITEM_DETAIL",
    "QUANTITY_BOOKING_REQUEST",
    "QUARTERLY_BOOKING",
    "WALLET_RECHARGE",
    "WALLET_TRANSACTION",
    "WALLET_ADJUSTMENT",
    "DISCOUNT_COUPON",
    "PROMOTIONAL_DISCOUNT",
    "MEMBERSHIP_DISCOUNT",
    "BULK_BOOKING_DISCOUNT",
    "MERCHANDISE_ITEM",
    "SERVICE_ADDON",
    "EQUIPMENT_RENTAL",
    "REFUND_REQUEST",
    "PARTIAL_REFUND",
    "ADMIN_ADJUSTMENT",
    "PENALTY_FEE",
    "LATE_CANCELLATION_FEE",
    "GIFT_CARD",
    "LOYALTY_POINTS_REDEMPTION",
    "PACKAGE_DEAL",
    "TAX_ADJUSTMENT",
    "SYSTEM_CORRECTION"
  ]
}



table "order_identities" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "email" {
    null = false
    type = character_varying(255)
  }
  column "name" {
    null = false
    type = character_varying(255)
  }
  column "type" {
    null = false
    type = character_varying(50)
  }
  column "user_id" {
    null = true
    type = uuid
  }
  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_order_identities_user_id" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  check "order_identities_type_check" {
    expr = "((type)::text = ANY ((ARRAY['USER'::character varying, 'GUEST'::character varying, 'SYSTEM'::character varying])::text[]))"
  }
}

table "order_items" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "amount" {
    null = false
    type = integer
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "description" {
    null = false
    type = character_varying(500)
  }
  column "updated_at" {
    null = false
    type = timestamptz
  }


  column "order_id" {
    null = false
    type = uuid
  }
  column "reference_type" {
    null = false
    type = enum.order_item_reference_type
  }
  column "reference_id" {
    null = false
    type = uuid
  }
  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_order_items_order_id" {
    columns     = [column.order_id]
    ref_columns = [table.orders.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  index "idx_order_items_reference_type" {
    columns = [column.reference_type]
  }
  index "idx_order_items_reference_id" {
    columns = [column.reference_id]
  }
}

table "orders" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "amount" {
    null = false
    type = integer
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "description" {
    null = false
    type = character_varying(1000)
  }
  column "payment_status" {
    null = false
    type = character_varying(50)
  }
  column "updated_at" {
    null = false
    type = timestamptz
  }
  column "order_identity_id" {
    null = false
    type = uuid
  }
  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_orders_order_identity_id" {
    columns     = [column.order_identity_id]
    ref_columns = [table.order_identities.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  check "orders_payment_status_check" {
    expr = "((payment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'PAID'::character varying, 'FAILED'::character varying])::text[]))"
  }
}

table "venue_groups" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "annotations" {
    null = false
    type = jsonb
  }
  column "description" {
    null = false
    type = character_varying(1000)
  }
  column "is_default" {
    null = false
    type = boolean
  }
  column "name" {
    null = false
    type = character_varying(255)
  }
  primary_key {
    columns = [column.id]
  }
}

table "venues" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "annotations" {
    null = false
    type = jsonb
  }
  column "description" {
    null = false
    type = character_varying(1000)
  }
  column "location" {
    null = false
    type = character_varying(500)
  }
  column "name" {
    null = false
    type = character_varying(255)
  }
  column "booking_slot_type" {
    null = false
    type = character_varying(50)
  }
  column "is_schedule_active" {
    null = false
    type = boolean
  }
  column "venue_group_id" {
    null = false
    type = uuid
  }
  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_venues_venue_group_id" {
    columns     = [column.venue_group_id]
    ref_columns = [table.venue_groups.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  check "venues_booking_slot_type_check" {
    expr = "((booking_slot_type)::text = ANY ((ARRAY['HALF_HOUR'::character varying, 'ONE_HOUR'::character varying])::text[]))"
  }
}

table "memberships" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "name" {
    null = false
    type = character_varying(100)
  }
  column "description" {
    null = true
    type = character_varying(500)
  }

  primary_key {
    columns = [column.id]
  }
}

table "membership_pricings" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "membership_id" {
    null = false
    type = uuid
  }
  column "discount_percentage" {
    null = false
    type = double_precision
  }

  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_membership_pricings_membership_id" {
    columns     = [column.membership_id]
    ref_columns = [table.memberships.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
  unique "uq_membership_pricings_membership_id" {
    columns = [column.membership_id]
  }
  index "idx_membership_pricings_membership_id" {
    columns = [column.membership_id]
  }
}

table "user_profiles" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "is_active" {
    null = false
    type = boolean
  }
  column "joined_at" {
    null = false
    type = timestamp
  }
  column "wallet_balance" {
    null = false
    type = integer
  }
  column "user_id" {
    null = false
    type = uuid
  }
  column "membership_id" {
    null = true
    type = uuid
  }

  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_user_profiles_user_id" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  foreign_key "fk_user_profiles_membership_id" {
    columns     = [column.membership_id]
    ref_columns = [table.memberships.column.id]
    on_update   = NO_ACTION
    on_delete   = SET_NULL
  }
  index "idx_user_profiles_active" {
    columns = [column.is_active]
  }
  index "idx_user_profiles_user_id" {
    columns = [column.user_id]
  }
  index "idx_user_profiles_membership_id" {
    columns = [column.membership_id]
  }
}

table "tenant_roles" {
  schema = schema.tenant

  column "role_id" {
    null = false
    type = uuid
  }
  column "role_name" {
    null = false
    type = character_varying(100)
  }
  column "role_description" {
    null = true
    type = character_varying(500)
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "updated_at" {
    null = false
    type = timestamptz
  }
  column "permissions" {
    null = true
    type = text
  }
  primary_key {
    columns = [column.role_id]
  }
  index "idx_tenant_roles_name" {
    columns = [column.role_name]
  }
}

table "user_profile_tenant_roles" {
  schema = schema.tenant

  column "user_profile_id" {
    null = false
    type = uuid
  }
  column "role_id" {
    null = false
    type = uuid
  }
  primary_key {
    columns = [column.user_profile_id, column.role_id]
  }
  foreign_key "fk_user_profile_tenant_roles_user_profile_id" {
    columns     = [column.user_profile_id]
    ref_columns = [table.user_profiles.column.id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
  foreign_key "fk_user_profile_tenant_roles_role_id" {
    columns     = [column.role_id]
    ref_columns = [table.tenant_roles.column.role_id]
    on_update   = NO_ACTION
    on_delete   = CASCADE
  }
}

table "venue_booking_requests" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "venue_id" {
    null = false
    type = uuid
  }
  column "date" {
    null = false
    type = date
  }
  column "hour" {
    null = false
    type = integer
  }
  column "duration" {
    null = false
    type = character_varying(50)
  }
  column "status" {
    null = false
    type = character_varying(50)
  }
  column "order_identity_id" {
    null = false
    type = uuid
  }
  column "notes" {
    null = true
    type = character_varying(1000)
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "updated_at" {
    null = false
    type = timestamptz
  }

  primary_key {
    columns = [column.id]
  }

  foreign_key "fk_booking_requests_venue_id" {
    columns     = [column.venue_id]
    ref_columns = [table.venues.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  foreign_key "fk_booking_requests_order_identity_id" {
    columns     = [column.order_identity_id]
    ref_columns = [table.order_identities.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }

  unique "uq_booking_requests_venue_date_hour_duration" {
    columns = [column.venue_id, column.date, column.hour, column.duration]
  }

  index "idx_booking_requests_venue_id" {
    columns = [column.venue_id]
  }
  index "idx_booking_requests_date" {
    columns = [column.date]
  }
  index "idx_booking_requests_status" {
    columns = [column.status]
  }
  index "idx_booking_requests_order_identity_id" {
    columns = [column.order_identity_id]
  }

  check "booking_requests_hour_check" {
    expr = "((hour >= 0) AND (hour <= 23))"
  }
  check "booking_requests_duration_check" {
    expr = "((duration)::text = ANY ((ARRAY['FIRST_HALF_HOUR'::character varying, 'SECOND_HALF_HOUR'::character varying, 'FULL_HOUR'::character varying])::text[]))"
  }
  check "booking_requests_status_check" {
    expr = "((status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[]))"
  }
}

