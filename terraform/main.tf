# Terraform configuration for multi-tenant booking service schema management
data "postgresql_schemas" "tenant_schemas" {
  database = "booking"
}

locals {
  tenant_schemas = [for s in data.postgresql_schemas.tenant_schemas.schemas : s if s != "public" ]
}


# Data source to read system tables schema
data "atlas_schema" "system_schema" {
  src     = file("${path.module}/hcl/system-tables.hcl")
}

locals {
  # Create a map of tenant -> combined HCL string (system + tenant schema)
  tenant_schemas_hcl = {
    for tenant in local.tenant_schemas : tenant => join("\n", [
      data.atlas_schema.system_schema.hcl,
      templatefile("${path.module}/hcl/tenant-tables.hcl", {
        tenant = tenant
      })
    ])
  }
}

# Generate tenant schemas for each tenant
resource "atlas_schema" "all_schemas" {
  for_each = toset(local.tenant_schemas)

  hcl     = local.tenant_schemas_hcl[each.key]
  url     = var.database_url

  # Include only the specific tenant schema
  include = ["public.*","${each.key}.*"]

  # Set variables for tenant-specific schema
  variables = jsonencode({
    tenant = each.key
  })

  # Lint configuration
  lint {
    review = "WARNING"
  }

  # Diff configuration
  diff {
    concurrent_index {
      create = true
      drop   = true
    }

    skip {
      drop_table  = true
      drop_column = true
      drop_schema = false
    }
  }

}