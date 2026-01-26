# Terraform configuration for multi-tenant booking service schema management
data "postgresql_schemas" "tenant_schemas" {
  database = "booking"
}



locals {
  # if tenant == "", use all schemas except public, else use the specified tenant
  tenant_schemas = var.tenant == "" ? [for s in data.postgresql_schemas.tenant_schemas.schemas : s if s != "public" ] : [var.tenant]

  file_mappings = {
    for schema in local.tenant_schemas : schema => "schema_diff_${schema}.sql"
  }
  system_tables = file("${path.module}/hcl/system-tables.hcl")
  tenant_tables = file("${path.module}/hcl/tenant-tables.hcl")
  tables_sha = sha256("${local.system_tables}${local.tenant_tables}")
}



resource "null_resource" "diff_tenant_schemas" {
  count = length(local.tenant_schemas)

  triggers = {
    sha = local.tables_sha
  }

  provisioner "local-exec" {
    command = <<EOT
      mkdir -p ${path.module}/diffs
      atlas schema diff \
      --from "${local.database_url}" \
      --to file://./hcl \
      --dev-url "docker://postgres/17/dev" \
      --var tenant=${local.tenant_schemas[count.index]} \
      --schema ${local.tenant_schemas[count.index]},public > ${path.module}/diffs/${local.file_mappings[local.tenant_schemas[count.index]]}
    EOT
  }
}

resource "null_resource" "single_transaction_file" {
  depends_on = [null_resource.diff_tenant_schemas]

  triggers = {
    sha = local.tables_sha
  }

  provisioner "local-exec" {
    command = <<EOT
      cd ${path.module}

      # Create the consolidated transaction file
      echo "BEGIN;" > diffs/consolidated_schema_changes.sql
      echo "" >> diffs/consolidated_schema_changes.sql
      echo "-- Multi-tenant schema changes generated at $(date)" >> diffs/consolidated_schema_changes.sql
      echo "-- Applied to all tenant schemas" >> diffs/consolidated_schema_changes.sql
      echo "" >> diffs/consolidated_schema_changes.sql

      # Concatenate all individual diff files
      for schema in ${join(" ", local.tenant_schemas)}; do
        if [ -f "diffs/schema_diff_$${schema}.sql" ] && [ -s "diffs/schema_diff_$${schema}.sql" ]; then
          # Check if the diff file contains the "no changes" message
          if grep -q "Schemas are synced, no changes to be made." "diffs/schema_diff_$${schema}.sql"; then
            echo "No changes for schema: $${schema}, skipping."
            rm "diffs/schema_diff_$${schema}.sql"
            continue
          fi
          echo "-- ===========================================" >> diffs/consolidated_schema_changes.sql
          echo "-- Changes for schema: $${schema}" >> diffs/consolidated_schema_changes.sql
          echo "-- ===========================================" >> diffs/consolidated_schema_changes.sql
          echo "" >> diffs/consolidated_schema_changes.sql
          cat "diffs/schema_diff_$${schema}.sql" >> diffs/consolidated_schema_changes.sql
          echo "" >> diffs/consolidated_schema_changes.sql
          echo "-- End of changes for schema: $${schema}" >> diffs/consolidated_schema_changes.sql
          echo "Removing individual diff file for schema: $${schema}"
          rm "diffs/schema_diff_$${schema}.sql"
        fi
      done

      echo "COMMIT;" >> diffs/consolidated_schema_changes.sql

      echo "Created consolidated schema changes file: diffs/consolidated_schema_changes.sql"
    EOT
  }
}