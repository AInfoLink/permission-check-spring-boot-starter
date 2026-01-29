table "users" {
  schema = schema.public
  column "id" {
    null = false
    type = uuid
  }
  column "email" {
    null = false
    type = character_varying(255)
  }
  column "is_email_verified" {
    null = false
    type = boolean
  }
  column "is_phone_verified" {
    null = false
    type = boolean
  }
  column "phone_number" {
    null = true
    type = character_varying(255)
  }
  column "provider" {
    null = false
    type = character_varying(255)
  }
  column "username" {
    null = false
    type = character_varying(255)
  }
  primary_key {
    columns = [column.id]
  }
  unique "uq_users_username" {
    columns = [column.username]
  }
}

table "system_roles" {
  schema = schema.public
  column "user_id" {
    null = false
    type = uuid
  }
  column "system_roles" {
    null = true
    type = character_varying(255)
  }
  foreign_key "fk_system_roles_user_id" {
    columns     = [column.user_id]
    ref_columns = [table.users.column.id]
    on_update   = NO_ACTION
    on_delete   = NO_ACTION
  }
}


table "dynamic_configs" {
  schema = schema.public

  column "id" {
    null = false
    type = uuid
  }
  column "key" {
    null = false
    type = character_varying(255)
  }
  column "body" {
    null = false
    type = jsonb
  }

  column "tenant_id" {
    null = false
    type = uuid
  }

  primary_key {
    columns = [column.id]
  }
  index "idx_dynamic_configs_key" {
    columns = [column.key]
  }
  unique "idx_dynamic_configs_key_unique" {
    columns = [column.key]
  }

  unique "idx_dynamic_configs_tenant_key_unique" {
    columns = [column.tenant_id, column.key]
  }
}

schema "public" {
  comment = "standard public schema"
}