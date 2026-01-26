variable "db_user" {
    type        = string
    description = "The database user"
}

variable "db_password" {
    type        = string
    description = "The database password"
    sensitive   = true
}

variable "db_host" {
    type        = string
    description = "The database host"
}

variable "db_port" {
    type        = number
    description = "The database port"
    default     = 5432
}

variable "db_name" {
    type        = string
    description = "The database name"

}

locals {
    database_url = "postgres://${var.db_user}:${var.db_password}@${var.db_host}:${var.db_port}/${var.db_name}"
}