.PHONY: help init plan apply destroy validate fmt lint clean

# Default target
help: ## Show this help message
	@echo "Multi-Tenant Booking Service - Schema Management"
	@echo ""
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

init: ## Initialize Terraform
	terraform init

plan: ## Show Terraform execution plan
	terraform plan

apply: ## Apply Terraform configuration
	terraform apply

destroy: ## Destroy all managed infrastructure
	@echo "⚠️  WARNING: This will destroy all managed schemas and data!"
	@echo "Are you sure? Type 'yes' to confirm:"
	@read confirm && [ "$$confirm" = "yes" ] && terraform destroy || echo "Cancelled"

validate: ## Validate Terraform configuration
	terraform validate

fmt: ## Format Terraform files
	terraform fmt -recursive

lint: fmt validate ## Run linting checks

clean: ## Clean Terraform cache and state backup files
	rm -rf .terraform/
	rm -f .terraform.lock.hcl
	rm -f terraform.tfstate.backup

setup: ## Setup initial configuration (copy example vars)
	@if [ ! -f terraform.tfvars ]; then \
		cp terraform.tfvars.example terraform.tfvars && \
		echo "✅ Created terraform.tfvars from example. Please edit it with your configuration."; \
	else \
		echo "⚠️  terraform.tfvars already exists. Skipping."; \
	fi

check-vars: ## Check if required variables are set
	@if [ ! -f terraform.tfvars ]; then \
		echo "❌ terraform.tfvars not found. Run 'make setup' first."; \
		exit 1; \
	else \
		echo "✅ terraform.tfvars found."; \
	fi

dev-setup: setup ## Setup development environment
	@echo "Setting up development environment..."
	@echo "1. Ensure you have Atlas CLI installed: https://atlasgo.io/cli"
	@echo "2. Ensure you have access to both production and development databases"
	@echo "3. Update terraform.tfvars with your database configurations"
	@echo "4. Run 'make init' to initialize Terraform"

refresh: ## Refresh Terraform state
	terraform refresh

show: ## Show current Terraform state
	terraform show

output: ## Show Terraform outputs
	terraform output

# Tenant management
add-tenant: ## Add a new tenant schema (usage: make add-tenant TENANT=tenant_name)
	@if [ -z "$(TENANT)" ]; then \
		echo "❌ Please specify TENANT name: make add-tenant TENANT=tenant_name"; \
		exit 1; \
	fi
	@echo "Adding tenant: $(TENANT)"
	@echo "Please manually add '$(TENANT)' to tenant_schemas list in terraform.tfvars"
	@echo "Then run: make plan && make apply"