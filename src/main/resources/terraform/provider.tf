terraform {
  required_version = ">= 1.0"
  required_providers {
    postgresql = {
      source = "cyrilgdn/postgresql"
      version = "1.26.0"
    }
    null = {
      source = "hashicorp/null"
      version = "3.2.1"
    }
  }
}

provider "postgresql" {
  host            = var.db_host
  port            = var.db_port
  username        = var.db_user
  password        = var.db_password
  sslmode         = "require"
  connect_timeout = 15
}