-- Tenant Roles Initialization Script for Multi-Tenant Booking Service
-- This script creates standard roles with appropriate permissions for booking service tenants
-- Compatible with the existing permission system using "resource:action" format

-- ============================================================================
-- TENANT ADMIN ROLES
-- ============================================================================

-- Super Admin - Full access to everything
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'TENANT_SUPER_ADMIN',
    '超級管理員 - 擁有所有權限，可管理租戶內所有功能',
    '*:*',
    NOW(),
    NOW()
);

-- Tenant Admin - Can manage users and basic tenant settings
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'TENANT_ADMIN',
    '租戶管理員 - 可管理用戶、角色和基本租戶設定',
    'users:*, tenant_roles:*, user_profiles:*, reports:read',
    NOW(),
    NOW()
);

-- ============================================================================
-- VENUE MANAGEMENT ROLES
-- ============================================================================

-- Venue Manager - Full venue management
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'VENUE_MANAGER',
    '場地管理員 - 可管理場地、場地群組和相關設定',
    'venues:*, venue_groups:*, order_item_categories:*',
    NOW(),
    NOW()
);

-- Venue Admin - Can manage venues but not groups or categories
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'VENUE_ADMIN',
    '場地管理者 - 可管理場地信息和基本設置',
    'venues:read, venues:update, venue_groups:read, order_item_categories:read',
    NOW(),
    NOW()
);

-- ============================================================================
-- ORDER & BOOKING MANAGEMENT ROLES
-- ============================================================================

-- Booking Manager - Full order and booking management
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'BOOKING_MANAGER',
    '預訂管理員 - 可管理所有預訂、訂單和相關項目',
    'orders:*, order_items:*, order_identities:*, venues:read, users:read',
    NOW(),
    NOW()
);

-- Booking Assistant - Can create and view orders, limited modification
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'BOOKING_ASSISTANT',
    '預訂助理 - 可創建和查看預訂，有限的修改權限',
    'orders:create, orders:read, orders:update:own, order_items:create, order_items:read, venues:read',
    NOW(),
    NOW()
);

-- Booking Clerk - Basic booking operations for own bookings
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'BOOKING_CLERK',
    '預訂職員 - 可處理基本預訂操作',
    'orders:create, orders:read:own, orders:update:own, order_items:read, venues:read',
    NOW(),
    NOW()
);

-- ============================================================================
-- CUSTOMER & USER ROLES
-- ============================================================================

-- Customer Manager - User management and customer service
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'CUSTOMER_MANAGER',
    '客戶管理員 - 可管理客戶資料和提供客戶服務',
    'user_profiles:*, orders:read, orders:update, venues:read',
    NOW(),
    NOW()
);

-- Customer Service - Support and assistance
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'CUSTOMER_SERVICE',
    '客戶服務 - 可查看客戶資料和協助處理問題',
    'user_profiles:read, orders:read, venues:read',
    NOW(),
    NOW()
);

-- Member - Regular customer with booking capabilities
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'MEMBER',
    '會員 - 一般客戶，可進行預訂和管理自己的帳戶',
    'orders:create, orders:read:own, orders:update:own, user_profiles:read:own, user_profiles:update:own, venues:read',
    NOW(),
    NOW()
);

-- Guest - Limited access for non-members
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'GUEST',
    '訪客 - 有限存取權限，僅可查看公開資訊',
    'venues:read',
    NOW(),
    NOW()
);

-- ============================================================================
-- FINANCIAL & REPORTING ROLES
-- ============================================================================

-- Finance Manager - Financial operations and reporting
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'FINANCE_MANAGER',
    '財務管理員 - 可管理財務相關功能和查看報表',
    'orders:read, user_profiles:read, reports:*, wallets:*',
    NOW(),
    NOW()
);

-- Accountant - Financial reporting and analysis
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'ACCOUNTANT',
    '會計 - 可查看財務報表和進行分析',
    'orders:read, reports:read, wallets:read',
    NOW(),
    NOW()
);

-- ============================================================================
-- READ-ONLY ROLES
-- ============================================================================

-- Auditor - Read-only access for auditing
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'AUDITOR',
    '稽核員 - 唯讀權限，用於稽核和合規檢查',
    '*:read',
    NOW(),
    NOW()
);

-- Viewer - Basic read-only access
INSERT INTO tenant_roles (role_id, role_name, role_description, permissions, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'VIEWER',
    '查看者 - 基本唯讀權限',
    'venues:read, orders:read, user_profiles:read',
    NOW(),
    NOW()
);