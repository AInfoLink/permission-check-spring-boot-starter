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


table "item_categories" {
  schema = schema.tenant

  column "id" {
    null = false
    type = uuid
  }
  column "code" {
    null = false
    type = character_varying(50)
  }
  column "created_at" {
    null = false
    type = timestamptz
  }
  column "description" {
    null = true
    type = character_varying(500)
  }
  column "is_active" {
    null = false
    type = boolean
  }
  column "name" {
    null = false
    type = character_varying(100)
  }
  column "operation_type" {
    null = false
    type = character_varying(20)
  }
  column "type" {
    null = false
    type = character_varying(20)
  }
  column "updated_at" {
    null = false
    type = timestamptz
  }
  primary_key {
    columns = [column.id]
  }
  index "idx_item_categories_operation_type" {
    columns = [column.operation_type]
  }
  index "idx_item_categories_type" {
    columns = [column.type]
  }
  check "item_categories_operation_type_check" {
    expr = "((operation_type)::text = ANY ((ARRAY['CHARGE'::character varying, 'REFUND'::character varying, 'NEUTRAL'::character varying])::text[]))"
  }
  check "item_categories_type_check" {
    expr = "((type)::text = ANY ((ARRAY['SYSTEM_MANAGED'::character varying, 'USER_MANAGED'::character varying])::text[]))"
  }
  unique "idx_item_categories_code" {
    columns = [column.code]
  }
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
  column "category_id" {
    null = false
    type = uuid
  }
  column "order_id" {
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
  foreign_key "fk_order_items_category_id" {
    columns     = [column.category_id]
    ref_columns = [table.item_categories.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
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
  primary_key {
    columns = [column.id]
  }
  foreign_key "fk_user_profiles_user_id" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
  index "idx_user_profiles_active" {
    columns = [column.is_active]
  }
  index "idx_user_profiles_user_id" {
    columns = [column.user_id]
  }
}

table "user_profile_tenant_roles" {
  schema = schema.tenant

  column "user_profile_id" {
    null = false
    type = uuid
  }
  column "role" {
    null = true
    type = character_varying(255)
  }
  foreign_key "fk_user_profile_tenant_roles_user_profile_id" {
    columns     = [column.user_profile_id]
    ref_columns = [table.user_profiles.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
}

