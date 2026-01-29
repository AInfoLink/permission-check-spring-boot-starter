# Outputs for schema management


output "managed_tenant_schemas" {
  description = "List of managed tenant schema names"
  value       = data.postgresql_schemas.tenant_schemas.schemas
}

output "schema_management_summary" {
  description = "Summary of schema management configuration"
  value = {
    system_schema_managed = true
    tenant_schemas_count  = length(local.tenant_schemas)
    tenant_schemas_list   = local.tenant_schemas
    concurrent_index_enabled = true
    safety_checks_enabled = true
  }
}