# RETAIL CLOTHES INVENTORY MANAGEMENT SYSTEM
## Technical Specification Document

**Version:** 1.0
**Date:** March 21, 2026
**Technology Stack:** Java Spring Boot + React.js + PostgreSQL

---

## TABLE OF CONTENTS

1. [System Architecture](#1-system-architecture)
2. [Technology Stack Details](#2-technology-stack-details)
3. [Database Schema Design](#3-database-schema-design)
4. [Backend API Specifications](#4-backend-api-specifications)
5. [Frontend Architecture](#5-frontend-architecture)
6. [Authentication & Authorization](#6-authentication--authorization)
7. [Batch Tracking Implementation](#7-batch-tracking-implementation)
8. [Data Migration Strategy](#8-data-migration-strategy)
9. [Development Environment Setup](#9-development-environment-setup)
10. [Deployment Architecture](#10-deployment-architecture)
11. [Security Considerations](#11-security-considerations)
12. [Performance Optimization](#12-performance-optimization)

---

## 1. SYSTEM ARCHITECTURE

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER (Browser)                    │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────────┐ │
│  │  React.js  │  │ TypeScript │  │  Ant Design / MUI      │ │
│  │  Frontend  │  │            │  │  Component Library     │ │
│  └─────┬──────┘  └────────────┘  └────────────────────────┘ │
└────────┼────────────────────────────────────────────────────┘
         │ HTTP/HTTPS (REST API + JSON)
         │
┌────────▼────────────────────────────────────────────────────┐
│                   APPLICATION LAYER (Server)                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Spring Boot Application                    │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │  Controllers (REST Endpoints)                     │  │ │
│  │  ├──────────────────────────────────────────────────┤  │ │
│  │  │  Services (Business Logic)                        │  │ │
│  │  ├──────────────────────────────────────────────────┤  │ │
│  │  │  Repositories (Data Access - JPA)                │  │ │
│  │  ├──────────────────────────────────────────────────┤  │ │
│  │  │  Security (Spring Security + JWT)                │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────┘ │
└────────┬────────────────────────────────────────────────────┘
         │ JDBC
┌────────▼────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              PostgreSQL 16 Database                     │ │
│  │  - ACID Compliant                                       │ │
│  │  - Transaction Management                               │ │
│  │  - Concurrent Access Support                            │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Deployment Model

**Single Server Deployment:**
- One Windows/Linux server runs Spring Boot application
- PostgreSQL runs on same server
- React frontend built and served as static files via Spring Boot
- Client computers access via browser: `http://server-ip:8080`

---

## 2. TECHNOLOGY STACK DETAILS

### 2.1 Backend Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.5.5 | Application framework |
| **Language** | Java | 21 LTS | Programming language |
| **Build Tool** | Maven / Gradle | Latest | Dependency management |
| **ORM** | Spring Data JPA (Hibernate) | 6.x | Database ORM |
| **Database** | PostgreSQL | 16 | Relational database |
| **Security** | Spring Security | 6.x | Authentication & Authorization |
| **JWT** | jjwt (io.jsonwebtoken) | 0.12.x | Token-based auth |
| **Validation** | Jakarta Validation | 3.x | Input validation |
| **PDF Generation** | iText 7 / JasperReports | Latest | Report generation |
| **Excel Export** | Apache POI | 5.x | Excel file generation |
| **Barcode** | Barcode4J / ZXing | Latest | Barcode generation |
| **Logging** | SLF4J + Logback | Latest | Application logging |

### 2.2 Frontend Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | React.js | 18.x | UI framework |
| **Language** | TypeScript | 5.x | Type-safe JavaScript |
| **Build Tool** | Vite / Create React App | Latest | Build tooling |
| **UI Library** | Ant Design / Material-UI | Latest | Component library |
| **State Management** | Redux Toolkit / Zustand | Latest | Global state |
| **Routing** | React Router | 6.x | Client-side routing |
| **HTTP Client** | Axios | 1.x | API calls |
| **Form Handling** | React Hook Form | Latest | Form management |
| **Validation** | Yup / Zod | Latest | Schema validation |
| **Charts** | Recharts / Chart.js | Latest | Data visualization |
| **Icons** | Ant Design Icons / MUI Icons | Latest | Icon library |
| **Date Handling** | date-fns / Day.js | Latest | Date utilities |

### 2.3 Development Tools

| Tool | Purpose |
|------|---------|
| **IntelliJ IDEA / VS Code** | IDE |
| **Postman / Insomnia** | API testing |
| **pgAdmin / DBeaver** | Database management |
| **Git** | Version control |
| **Docker** (Optional) | Containerization |

---

## 3. DATABASE SCHEMA DESIGN

### 3.1 Entity Relationship Diagram (ERD)

```
┌──────────────┐         ┌──────────────────┐
│    users     │────────<│  activity_logs   │
└──────────────┘         └──────────────────┘

┌──────────────┐         ┌──────────────────┐
│  categories  │────────<│    products      │
└──────────────┘         └────────┬─────────┘
                                  │
                         ┌────────▼─────────────┐
                         │  product_variants    │
                         └────────┬─────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
         ┌──────────▼─────┐  ┌───▼──────────┐  │
         │ inventory_batch│  │ sale_items   │  │
         └────────────────┘  └──────────────┘  │
                                               │
┌──────────────┐         ┌───────────────┐    │
│  suppliers   │────────<│purchase_orders│    │
└──────────────┘         └───────┬───────┘    │
                                 │            │
                         ┌───────▼────────┐   │
                         │purchase_items  │───┘
                         └────────────────┘

┌──────────────┐         ┌──────────────┐
│  customers   │────────<│    sales     │
└──────────────┘         └──────┬───────┘
                                │
                    ┌───────────┼───────────┐
                    │           │           │
         ┌──────────▼──────┐ ┌──▼────────┐ │
         │    payments     │ │sale_items │ │
         └─────────────────┘ └───────────┘ │
                                           │
┌──────────────┐                           │
│  employees   │                           │
└──────┬───────┘                           │
       │                                   │
┌──────▼─────────────┐                     │
│employee_salaries   │                     │
└────────────────────┘                     │
                                           │
┌──────────────────┐                       │
│expense_categories│                       │
└────────┬─────────┘                       │
         │                                 │
┌────────▼─────────┐                       │
│  daily_expenses  │                       │
└──────────────────┘                       │
```

### 3.2 Detailed Table Schemas

#### **3.2.1 User Management**

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- ADMIN, MANAGER, CASHIER, INVENTORY_MANAGER
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Activity logs
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL, -- SALES, INVENTORY, PURCHASE, etc.
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created ON activity_logs(created_at);
```

#### **3.2.2 Product & Inventory**

```sql
-- Categories table (hierarchical)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES categories(id),
    category_type VARCHAR(20), -- MALE, FEMALE, KIDS
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products master table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    brand VARCHAR(100),
    product_type VARCHAR(20) NOT NULL, -- STITCHED, UNSTITCHED
    description TEXT,
    images TEXT[], -- Array of image URLs/paths
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product variants (size + color combinations)
CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(100) UNIQUE,
    size VARCHAR(20), -- S, M, L, XL, XXL, or numeric for unstitched (meters)
    color VARCHAR(50),
    cost_price DECIMAL(12,2) NOT NULL,
    selling_price DECIMAL(12,2) NOT NULL,
    stock_quantity INTEGER DEFAULT 0,
    reorder_level INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory batches (batch-wise tracking)
CREATE TABLE inventory_batches (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT REFERENCES product_variants(id),
    batch_number VARCHAR(50) NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id),
    purchase_order_id BIGINT REFERENCES purchase_orders(id),
    quantity_received INTEGER NOT NULL,
    quantity_remaining INTEGER NOT NULL,
    cost_price DECIMAL(12,2) NOT NULL,
    manufacturing_date DATE,
    expiry_date DATE,
    received_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stock movements (audit trail)
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT REFERENCES product_variants(id),
    batch_id BIGINT REFERENCES inventory_batches(id),
    movement_type VARCHAR(20) NOT NULL, -- PURCHASE, SALE, ADJUSTMENT, RETURN, DAMAGE
    quantity INTEGER NOT NULL, -- Positive for IN, Negative for OUT
    reference_type VARCHAR(20), -- PURCHASE_ORDER, SALE, ADJUSTMENT
    reference_id BIGINT,
    remarks TEXT,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_barcode ON product_variants(barcode);
CREATE INDEX idx_batches_variant ON inventory_batches(product_variant_id);
CREATE INDEX idx_stock_movements_variant ON stock_movements(product_variant_id);
```

#### **3.2.3 Supplier & Purchase**

```sql
-- Suppliers table
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    city VARCHAR(50),
    payment_terms VARCHAR(100), -- Net 30, COD, etc.
    credit_limit DECIMAL(12,2),
    opening_balance DECIMAL(12,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase orders
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id),
    order_date DATE NOT NULL,
    expected_delivery_date DATE,
    received_date DATE,
    subtotal DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- DRAFT, SUBMITTED, RECEIVED, CANCELLED
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PARTIAL, PAID
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase order items
CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id),
    batch_number VARCHAR(50), -- Assigned during GRN
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    received_quantity INTEGER DEFAULT 0,
    manufacturing_date DATE,
    expiry_date DATE
);

-- Supplier payments
CREATE TABLE supplier_payments (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT REFERENCES suppliers(id),
    purchase_order_id BIGINT REFERENCES purchase_orders(id),
    payment_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- CASH, BANK_TRANSFER, CHEQUE
    reference_number VARCHAR(50),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_po_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_purchase_items_po ON purchase_items(purchase_order_id);
```

#### **3.2.4 Customer & Sales**

```sql
-- Customers table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100),
    address TEXT,
    city VARCHAR(50),
    credit_limit DECIMAL(12,2) DEFAULT 0,
    loyalty_points INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sales table
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT REFERENCES customers(id), -- NULL for walk-in customers
    customer_name VARCHAR(150), -- For walk-in customers
    customer_phone VARCHAR(20),
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    balance_due DECIMAL(12,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'PAID', -- PAID, PARTIAL, PENDING
    sale_type VARCHAR(20) DEFAULT 'REGULAR', -- REGULAR, CREDIT, RETURN
    cashier_id BIGINT REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sale items
CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT REFERENCES sales(id) ON DELETE CASCADE,
    product_variant_id BIGINT REFERENCES product_variants(id),
    batch_id BIGINT REFERENCES inventory_batches(id), -- Track which batch sold from
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_price DECIMAL(12,2) NOT NULL,
    cost_price DECIMAL(12,2), -- For profit calculation
    profit_amount DECIMAL(12,2) -- Calculated: (unit_price - cost_price) * quantity
);

-- Payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT REFERENCES sales(id),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- CASH, CARD, JAZZCASH, EASYPAISA, BANK_TRANSFER
    reference_number VARCHAR(50),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cash register (daily opening/closing)
CREATE TABLE cash_register (
    id BIGSERIAL PRIMARY KEY,
    cashier_id BIGINT REFERENCES users(id),
    register_date DATE NOT NULL,
    shift VARCHAR(20), -- MORNING, EVENING, NIGHT
    opening_balance DECIMAL(12,2) NOT NULL,
    closing_balance DECIMAL(12,2),
    total_sales DECIMAL(12,2),
    total_cash DECIMAL(12,2),
    total_card DECIMAL(12,2),
    total_digital DECIMAL(12,2),
    difference DECIMAL(12,2), -- Expected - Actual
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED
    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_sales_cashier ON sales(cashier_id);
CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX idx_payments_sale ON payments(sale_id);
```

#### **3.2.5 Employee Management**

```sql
-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    cnic VARCHAR(15) UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    designation VARCHAR(50), -- Tailor, Helper, Manager, Accountant, Security
    joining_date DATE NOT NULL,
    monthly_salary DECIMAL(12,2) NOT NULL,
    photo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employee salary payments
CREATE TABLE employee_salaries (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    payment_date DATE NOT NULL,
    salary_month VARCHAR(7) NOT NULL, -- Format: 2026-03 (YYYY-MM)
    amount DECIMAL(12,2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- SALARY, ADVANCE, BONUS, DEDUCTION
    payment_method VARCHAR(20) NOT NULL, -- CASH, BANK_TRANSFER, JAZZCASH, EASYPAISA
    reference_number VARCHAR(50),
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employee attendance (optional for future)
CREATE TABLE employee_attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) DEFAULT 'PRESENT', -- PRESENT, ABSENT, HALF_DAY, LEAVE
    notes TEXT,
    UNIQUE(employee_id, attendance_date)
);

CREATE INDEX idx_employee_salaries_emp ON employee_salaries(employee_id);
CREATE INDEX idx_employee_salaries_month ON employee_salaries(salary_month);
```

#### **3.2.6 Daily Expense Management**

```sql
-- Expense categories
CREATE TABLE expense_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Daily expenses
CREATE TABLE daily_expenses (
    id BIGSERIAL PRIMARY KEY,
    expense_date DATE NOT NULL,
    category_id BIGINT REFERENCES expense_categories(id),
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- CASH, BANK_TRANSFER, CARD
    vendor_name VARCHAR(150),
    description TEXT,
    receipt_url VARCHAR(255), -- Optional receipt image
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_date ON daily_expenses(expense_date);
CREATE INDEX idx_expenses_category ON daily_expenses(category_id);
```

### 3.3 Database Initialization Scripts

```sql
-- Insert default roles (handled in application, but for reference)
-- ADMIN, MANAGER, CASHIER, INVENTORY_MANAGER

-- Insert default admin user (password should be hashed)
INSERT INTO users (username, password_hash, full_name, role, is_active)
VALUES ('admin', '$2a$10$...hashedpassword...', 'System Administrator', 'ADMIN', true);

-- Insert default categories
INSERT INTO categories (name, category_type, display_order) VALUES
('Male Clothing', 'MALE', 1),
('Female Clothing', 'FEMALE', 2),
('Kids Clothing', 'KIDS', 3);

-- Insert sub-categories
INSERT INTO categories (name, parent_id, display_order) VALUES
('Shirts', (SELECT id FROM categories WHERE name='Male Clothing'), 1),
('Pants', (SELECT id FROM categories WHERE name='Male Clothing'), 2),
('Suits', (SELECT id FROM categories WHERE name='Male Clothing'), 3),
('Dresses', (SELECT id FROM categories WHERE name='Female Clothing'), 1),
('Shalwar Kameez', (SELECT id FROM categories WHERE name='Female Clothing'), 2),
('Shirts', (SELECT id FROM categories WHERE name='Kids Clothing'), 1);

-- Insert default expense categories
INSERT INTO expense_categories (name, description, display_order) VALUES
('Utilities', 'Electricity, water, gas, internet bills', 1),
('Rent', 'Shop/workspace rent', 2),
('Raw Materials', 'Thread, buttons, zippers, fabric', 3),
('Transportation', 'Fuel, vehicle maintenance, delivery costs', 4),
('Maintenance', 'Equipment repairs, shop maintenance', 5),
('Marketing', 'Advertising, promotional materials', 6),
('Office Supplies', 'Stationery, printing', 7),
('Miscellaneous', 'Other expenses', 8);
```

---

## 4. BACKEND API SPECIFICATIONS

### 4.1 API Architecture

**Base URL:** `http://server-ip:8080/api/v1`

**Authentication:** JWT Bearer Token (in Authorization header)

**Response Format:** JSON

**Standard Response Structure:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-03-21T10:30:00Z"
}
```

**Error Response Structure:**
```json
{
  "success": false,
  "message": "Error description",
  "errors": [
    {
      "field": "fieldName",
      "message": "Validation error message"
    }
  ],
  "timestamp": "2026-03-21T10:30:00Z"
}
```

### 4.2 API Endpoints

#### **4.2.1 Authentication APIs**

```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh-token
GET    /api/v1/auth/profile
PUT    /api/v1/auth/change-password
```

**Example: Login**
```
POST /api/v1/auth/login
Content-Type: application/json

Request:
{
  "username": "admin",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "admin",
      "fullName": "System Administrator",
      "role": "ADMIN"
    }
  }
}
```

#### **4.2.2 Product Management APIs**

```
GET    /api/v1/products                    // List all products (with pagination)
GET    /api/v1/products/{id}               // Get product details
POST   /api/v1/products                    // Create new product
PUT    /api/v1/products/{id}               // Update product
DELETE /api/v1/products/{id}               // Delete product
GET    /api/v1/products/search             // Search products (by name, SKU, barcode)
POST   /api/v1/products/{id}/images        // Upload product images

GET    /api/v1/categories                  // List categories (hierarchical)
POST   /api/v1/categories                  // Create category
PUT    /api/v1/categories/{id}             // Update category
DELETE /api/v1/categories/{id}             // Delete category
```

**Example: Create Product**
```
POST /api/v1/products
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "name": "Cotton Shirt",
  "categoryId": 4,
  "brand": "Gul Ahmed",
  "productType": "STITCHED",
  "description": "Premium cotton formal shirt",
  "variants": [
    {
      "sku": "GUL-SHIRT-001-M-BLUE",
      "size": "M",
      "color": "Blue",
      "costPrice": 1200.00,
      "sellingPrice": 1800.00,
      "reorderLevel": 5
    },
    {
      "sku": "GUL-SHIRT-001-L-BLUE",
      "size": "L",
      "color": "Blue",
      "costPrice": 1200.00,
      "sellingPrice": 1800.00,
      "reorderLevel": 5
    }
  ]
}

Response:
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Cotton Shirt",
    "variants": [...]
  }
}
```

#### **4.2.3 Inventory & Batch APIs**

```
GET    /api/v1/inventory/stock-summary          // Overall stock summary
GET    /api/v1/inventory/low-stock               // Low stock items
GET    /api/v1/inventory/batches                 // List all batches
GET    /api/v1/inventory/batches/{id}            // Batch details
POST   /api/v1/inventory/adjustment              // Stock adjustment (damage/loss)
GET    /api/v1/inventory/movements               // Stock movement history
GET    /api/v1/inventory/valuation               // Stock valuation report
GET    /api/v1/inventory/expiry-alerts           // Batches nearing expiry
```

**Example: Stock Summary**
```
GET /api/v1/inventory/stock-summary?categoryId=1
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "totalProducts": 150,
    "totalVariants": 450,
    "totalStockValue": 2500000.00,
    "lowStockCount": 12,
    "items": [
      {
        "productId": 1,
        "productName": "Cotton Shirt",
        "variantId": 1,
        "sku": "GUL-SHIRT-001-M-BLUE",
        "size": "M",
        "color": "Blue",
        "stockQuantity": 45,
        "reorderLevel": 5,
        "status": "IN_STOCK",
        "batches": [
          {
            "batchId": 1,
            "batchNumber": "BATCH-2026-001",
            "quantity": 25,
            "expiryDate": null
          },
          {
            "batchId": 2,
            "batchNumber": "BATCH-2026-015",
            "quantity": 20,
            "expiryDate": null
          }
        ]
      }
    ]
  }
}
```

#### **4.2.4 Purchase Management APIs**

```
GET    /api/v1/suppliers                   // List suppliers
POST   /api/v1/suppliers                   // Create supplier
PUT    /api/v1/suppliers/{id}              // Update supplier
GET    /api/v1/suppliers/{id}/ledger       // Supplier ledger

GET    /api/v1/purchase-orders             // List purchase orders
GET    /api/v1/purchase-orders/{id}        // PO details
POST   /api/v1/purchase-orders             // Create PO
PUT    /api/v1/purchase-orders/{id}        // Update PO
POST   /api/v1/purchase-orders/{id}/receive // Goods receiving (GRN)
POST   /api/v1/purchase-orders/{id}/payment // Record supplier payment
```

**Example: Goods Receiving (GRN)**
```
POST /api/v1/purchase-orders/5/receive
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "receivedDate": "2026-03-21",
  "items": [
    {
      "purchaseItemId": 10,
      "receivedQuantity": 50,
      "batchNumber": "BATCH-2026-025",
      "manufacturingDate": "2026-03-01",
      "expiryDate": null
    }
  ]
}

Response:
{
  "success": true,
  "message": "Goods received successfully. Inventory updated.",
  "data": {
    "purchaseOrderId": 5,
    "batchesCreated": [
      {
        "batchId": 25,
        "batchNumber": "BATCH-2026-025",
        "quantity": 50
      }
    ]
  }
}
```

#### **4.2.5 Sales & POS APIs**

```
GET    /api/v1/sales                       // List sales (with filters)
GET    /api/v1/sales/{id}                  // Sale details
POST   /api/v1/sales                       // Create sale (POS)
POST   /api/v1/sales/{id}/payment          // Add payment to sale
PUT    /api/v1/sales/{id}/status           // Update sale status
GET    /api/v1/sales/invoice/{id}          // Generate invoice PDF
POST   /api/v1/sales/{id}/return           // Process return

GET    /api/v1/pos/products/search         // Fast product search for POS
GET    /api/v1/pos/cart                    // Get current cart
POST   /api/v1/pos/cart/add                // Add item to cart
DELETE /api/v1/pos/cart/remove/{itemId}    // Remove from cart
POST   /api/v1/pos/checkout                // Checkout and create sale

GET    /api/v1/cash-register               // Get current cash register
POST   /api/v1/cash-register/open          // Open cash register
POST   /api/v1/cash-register/close         // Close cash register
```

**Example: POS Checkout**
```
POST /api/v1/pos/checkout
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "customerId": null,
  "customerName": "Walk-in Customer",
  "customerPhone": null,
  "items": [
    {
      "variantId": 5,
      "quantity": 2,
      "unitPrice": 1800.00,
      "discount": 0
    }
  ],
  "subtotal": 3600.00,
  "discountAmount": 200.00,
  "totalAmount": 3400.00,
  "payments": [
    {
      "paymentMethod": "CASH",
      "amount": 2000.00
    },
    {
      "paymentMethod": "CARD",
      "amount": 1400.00
    }
  ]
}

Response:
{
  "success": true,
  "message": "Sale completed successfully",
  "data": {
    "saleId": 123,
    "invoiceNumber": "INV-2026-00123",
    "totalAmount": 3400.00,
    "paidAmount": 3400.00,
    "balanceDue": 0,
    "invoiceUrl": "/api/v1/sales/invoice/123"
  }
}
```

#### **4.2.6 Customer APIs**

```
GET    /api/v1/customers                   // List customers
POST   /api/v1/customers                   // Create customer
PUT    /api/v1/customers/{id}              // Update customer
GET    /api/v1/customers/{id}/purchases    // Customer purchase history
GET    /api/v1/customers/{id}/payments     // Customer payment history
GET    /api/v1/customers/{id}/balance      // Outstanding balance
```

#### **4.2.7 Employee Management APIs**

```
GET    /api/v1/employees                   // List employees
POST   /api/v1/employees                   // Create employee
PUT    /api/v1/employees/{id}              // Update employee
DELETE /api/v1/employees/{id}              // Deactivate employee
GET    /api/v1/employees/{id}/salaries     // Salary history

POST   /api/v1/employee-salaries           // Record salary payment
GET    /api/v1/employee-salaries/payroll   // Monthly payroll report
```

**Example: Record Salary Payment**
```
POST /api/v1/employee-salaries
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "employeeId": 3,
  "paymentDate": "2026-03-31",
  "salaryMonth": "2026-03",
  "amount": 35000.00,
  "paymentType": "SALARY",
  "paymentMethod": "BANK_TRANSFER",
  "referenceNumber": "TXN123456",
  "notes": "March 2026 salary"
}

Response:
{
  "success": true,
  "message": "Salary payment recorded",
  "data": {
    "id": 15,
    "employeeId": 3,
    "amount": 35000.00
  }
}
```

#### **4.2.8 Expense Management APIs**

```
GET    /api/v1/expense-categories          // List expense categories
POST   /api/v1/expense-categories          // Create category
PUT    /api/v1/expense-categories/{id}     // Update category

GET    /api/v1/expenses                    // List expenses (with filters)
POST   /api/v1/expenses                    // Create expense
PUT    /api/v1/expenses/{id}               // Update expense
DELETE /api/v1/expenses/{id}               // Delete expense
GET    /api/v1/expenses/summary            // Expense summary (date range)
```

#### **4.2.9 Reports APIs**

```
GET    /api/v1/reports/sales-summary       // Sales summary report
GET    /api/v1/reports/sales-by-category   // Category-wise sales
GET    /api/v1/reports/sales-by-product    // Product-wise sales
GET    /api/v1/reports/top-products        // Top selling products
GET    /api/v1/reports/cashier-summary     // Cashier-wise sales

GET    /api/v1/reports/inventory-valuation // Stock valuation
GET    /api/v1/reports/low-stock           // Low stock report
GET    /api/v1/reports/fast-moving         // Fast moving items
GET    /api/v1/reports/slow-moving         // Slow moving items
GET    /api/v1/reports/batch-expiry        // Batches nearing expiry

GET    /api/v1/reports/purchase-summary    // Purchase summary
GET    /api/v1/reports/supplier-wise       // Supplier-wise purchases

GET    /api/v1/reports/profit-loss         // P&L statement
GET    /api/v1/reports/cash-flow           // Cash flow report
GET    /api/v1/reports/expense-breakdown   // Expense breakdown
```

**Example: Profit & Loss Report**
```
GET /api/v1/reports/profit-loss?startDate=2026-03-01&endDate=2026-03-31
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "period": {
      "startDate": "2026-03-01",
      "endDate": "2026-03-31"
    },
    "revenue": {
      "totalSales": 1250000.00,
      "returns": 25000.00,
      "netSales": 1225000.00
    },
    "costOfGoodsSold": 750000.00,
    "grossProfit": 475000.00,
    "grossMargin": 38.78,
    "expenses": {
      "employeeSalaries": 150000.00,
      "dailyExpenses": 75000.00,
      "totalExpenses": 225000.00
    },
    "netProfit": 250000.00,
    "netMargin": 20.41
  }
}
```

#### **4.2.10 User Management APIs**

```
GET    /api/v1/users                       // List users
POST   /api/v1/users                       // Create user
PUT    /api/v1/users/{id}                  // Update user
DELETE /api/v1/users/{id}                  // Deactivate user
GET    /api/v1/users/{id}/activity-logs    // User activity logs
```

### 4.3 Backend Project Structure

```
src/main/java/com/retail/inventory/
│
├── config/
│   ├── SecurityConfig.java              // Spring Security configuration
│   ├── JwtConfig.java                   // JWT settings
│   ├── DatabaseConfig.java              // Database configuration
│   └── CorsConfig.java                  // CORS settings
│
├── controller/
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── InventoryController.java
│   ├── PurchaseController.java
│   ├── SalesController.java
│   ├── POSController.java
│   ├── CustomerController.java
│   ├── EmployeeController.java
│   ├── ExpenseController.java
│   ├── ReportController.java
│   └── UserController.java
│
├── service/
│   ├── AuthService.java
│   ├── ProductService.java
│   ├── InventoryService.java
│   ├── BatchTrackingService.java
│   ├── PurchaseService.java
│   ├── SalesService.java
│   ├── POSService.java
│   ├── CustomerService.java
│   ├── EmployeeService.java
│   ├── ExpenseService.java
│   ├── ReportService.java
│   └── UserService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── ProductVariantRepository.java
│   ├── InventoryBatchRepository.java
│   ├── StockMovementRepository.java
│   ├── SupplierRepository.java
│   ├── PurchaseOrderRepository.java
│   ├── SaleRepository.java
│   ├── SaleItemRepository.java
│   ├── CustomerRepository.java
│   ├── EmployeeRepository.java
│   ├── EmployeeSalaryRepository.java
│   ├── ExpenseCategoryRepository.java
│   └── DailyExpenseRepository.java
│
├── model/
│   ├── User.java
│   ├── Product.java
│   ├── ProductVariant.java
│   ├── InventoryBatch.java
│   ├── StockMovement.java
│   ├── Supplier.java
│   ├── PurchaseOrder.java
│   ├── PurchaseItem.java
│   ├── Sale.java
│   ├── SaleItem.java
│   ├── Payment.java
│   ├── Customer.java
│   ├── Employee.java
│   ├── EmployeeSalary.java
│   ├── ExpenseCategory.java
│   └── DailyExpense.java
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── ProductCreateRequest.java
│   │   ├── PurchaseOrderRequest.java
│   │   ├── SaleRequest.java
│   │   └── ...
│   └── response/
│       ├── ApiResponse.java
│       ├── ProductResponse.java
│       ├── SaleResponse.java
│       └── ...
│
├── security/
│   ├── JwtTokenProvider.java            // JWT token generation/validation
│   ├── JwtAuthenticationFilter.java     // JWT filter
│   └── UserDetailsServiceImpl.java      // Load user details
│
├── exception/
│   ├── GlobalExceptionHandler.java      // Global error handler
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── UnauthorizedException.java
│
└── util/
    ├── BarcodeGenerator.java            // Generate barcodes
    ├── InvoicePDFGenerator.java         // PDF invoice generation
    ├── ExcelExporter.java               // Excel export utility
    └── DateUtil.java                    // Date utilities

src/main/resources/
├── application.properties               // Application config
├── application-dev.properties           // Dev environment
├── application-prod.properties          // Production environment
└── db/
    └── migration/
        ├── V1__initial_schema.sql
        ├── V2__seed_data.sql
        └── ...
```

---

## 5. FRONTEND ARCHITECTURE

### 5.1 React Project Structure

```
src/
│
├── api/
│   ├── axiosConfig.ts                   // Axios instance with interceptors
│   ├── authApi.ts                       // Auth API calls
│   ├── productApi.ts                    // Product API calls
│   ├── inventoryApi.ts                  // Inventory API calls
│   ├── purchaseApi.ts                   // Purchase API calls
│   ├── salesApi.ts                      // Sales API calls
│   ├── customerApi.ts                   // Customer API calls
│   ├── employeeApi.ts                   // Employee API calls
│   ├── expenseApi.ts                    // Expense API calls
│   └── reportApi.ts                     // Report API calls
│
├── components/
│   ├── common/
│   │   ├── Layout.tsx                   // Main layout wrapper
│   │   ├── Navbar.tsx                   // Top navigation
│   │   ├── Sidebar.tsx                  // Side menu
│   │   ├── Footer.tsx
│   │   ├── Breadcrumb.tsx
│   │   ├── LoadingSpinner.tsx
│   │   ├── ErrorBoundary.tsx
│   │   └── ProtectedRoute.tsx           // Auth guard
│   │
│   ├── product/
│   │   ├── ProductList.tsx
│   │   ├── ProductForm.tsx
│   │   ├── ProductCard.tsx
│   │   ├── VariantManager.tsx
│   │   └── CategoryTree.tsx
│   │
│   ├── inventory/
│   │   ├── StockSummary.tsx
│   │   ├── BatchList.tsx
│   │   ├── StockAdjustment.tsx
│   │   ├── LowStockAlert.tsx
│   │   └── ExpiryAlert.tsx
│   │
│   ├── purchase/
│   │   ├── PurchaseOrderList.tsx
│   │   ├── PurchaseOrderForm.tsx
│   │   ├── GoodsReceiving.tsx           // GRN component
│   │   └── SupplierList.tsx
│   │
│   ├── sales/
│   │   ├── SalesList.tsx
│   │   ├── SaleDetails.tsx
│   │   └── InvoicePrint.tsx
│   │
│   ├── pos/
│   │   ├── POSScreen.tsx                // Main POS interface
│   │   ├── ProductSearch.tsx
│   │   ├── Cart.tsx
│   │   ├── PaymentModal.tsx
│   │   ├── CashRegister.tsx
│   │   └── BarcodeScanner.tsx
│   │
│   ├── customer/
│   │   ├── CustomerList.tsx
│   │   ├── CustomerForm.tsx
│   │   └── CustomerLedger.tsx
│   │
│   ├── employee/
│   │   ├── EmployeeList.tsx
│   │   ├── EmployeeForm.tsx
│   │   ├── SalaryPayment.tsx
│   │   └── PayrollReport.tsx
│   │
│   ├── expense/
│   │   ├── ExpenseList.tsx
│   │   ├── ExpenseForm.tsx
│   │   ├── CategoryManager.tsx
│   │   └── ExpenseSummary.tsx
│   │
│   └── reports/
│       ├── Dashboard.tsx
│       ├── SalesReport.tsx
│       ├── InventoryReport.tsx
│       ├── PurchaseReport.tsx
│       ├── ProfitLossReport.tsx
│       └── ExpenseReport.tsx
│
├── pages/
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   ├── Products.tsx
│   ├── Inventory.tsx
│   ├── Purchase.tsx
│   ├── Sales.tsx
│   ├── POS.tsx
│   ├── Customers.tsx
│   ├── Employees.tsx
│   ├── Expenses.tsx
│   ├── Reports.tsx
│   └── Settings.tsx
│
├── store/
│   ├── index.ts                         // Redux store configuration
│   ├── slices/
│   │   ├── authSlice.ts
│   │   ├── productSlice.ts
│   │   ├── inventorySlice.ts
│   │   ├── cartSlice.ts                 // POS cart state
│   │   ├── salesSlice.ts
│   │   └── uiSlice.ts                   // UI state (modals, etc.)
│   └── hooks.ts                         // Typed hooks
│
├── types/
│   ├── auth.ts
│   ├── product.ts
│   ├── inventory.ts
│   ├── purchase.ts
│   ├── sales.ts
│   ├── customer.ts
│   ├── employee.ts
│   └── expense.ts
│
├── utils/
│   ├── formatters.ts                    // Number, date formatters
│   ├── validators.ts                    // Form validation helpers
│   ├── constants.ts                     // App constants
│   └── permissions.ts                   // Role-based permission checks
│
├── hooks/
│   ├── useAuth.ts
│   ├── useDebounce.ts
│   ├── usePagination.ts
│   └── usePermission.ts
│
├── styles/
│   ├── globals.css
│   ├── theme.ts                         // MUI/Ant Design theme
│   └── variables.css
│
├── App.tsx
├── main.tsx
└── vite.config.ts / package.json
```

### 5.2 Key Frontend Features

#### **5.2.1 POS Interface Design Principles**

- Large touch-friendly buttons
- Fast product search with auto-complete
- Barcode scanner integration (USB HID device)
- Real-time cart updates
- Split screen: Products on left, Cart on right
- Quick payment buttons (exact amount shortcuts)
- Keyboard shortcuts (F-keys for common actions)
- Print receipt immediately after sale

#### **5.2.2 State Management Strategy**

```typescript
// Example: Cart Slice (Redux Toolkit)
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CartItem {
  variantId: number;
  sku: string;
  productName: string;
  size: string;
  color: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  total: number;
}

interface CartState {
  items: CartItem[];
  subtotal: number;
  discountAmount: number;
  totalAmount: number;
}

const cartSlice = createSlice({
  name: 'cart',
  initialState: {
    items: [],
    subtotal: 0,
    discountAmount: 0,
    totalAmount: 0,
  } as CartState,
  reducers: {
    addItem: (state, action: PayloadAction<CartItem>) => {
      const existingItem = state.items.find(
        item => item.variantId === action.payload.variantId
      );
      if (existingItem) {
        existingItem.quantity += action.payload.quantity;
        existingItem.total = existingItem.quantity * existingItem.unitPrice - existingItem.discount;
      } else {
        state.items.push(action.payload);
      }
      calculateTotals(state);
    },
    removeItem: (state, action: PayloadAction<number>) => {
      state.items = state.items.filter(item => item.variantId !== action.payload);
      calculateTotals(state);
    },
    updateQuantity: (state, action: PayloadAction<{ variantId: number; quantity: number }>) => {
      const item = state.items.find(item => item.variantId === action.payload.variantId);
      if (item) {
        item.quantity = action.payload.quantity;
        item.total = item.quantity * item.unitPrice - item.discount;
        calculateTotals(state);
      }
    },
    applyDiscount: (state, action: PayloadAction<number>) => {
      state.discountAmount = action.payload;
      calculateTotals(state);
    },
    clearCart: (state) => {
      state.items = [];
      state.subtotal = 0;
      state.discountAmount = 0;
      state.totalAmount = 0;
    },
  },
});

function calculateTotals(state: CartState) {
  state.subtotal = state.items.reduce((sum, item) => sum + item.total, 0);
  state.totalAmount = state.subtotal - state.discountAmount;
}

export const { addItem, removeItem, updateQuantity, applyDiscount, clearCart } = cartSlice.actions;
export default cartSlice.reducer;
```

#### **5.2.3 API Integration Pattern**

```typescript
// api/axiosConfig.ts
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor (add auth token)
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor (handle errors globally)
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

```typescript
// api/salesApi.ts
import api from './axiosConfig';
import { Sale, SaleRequest } from '../types/sales';

export const salesApi = {
  createSale: async (data: SaleRequest): Promise<Sale> => {
    return api.post('/sales', data);
  },

  getSales: async (filters?: any): Promise<Sale[]> => {
    return api.get('/sales', { params: filters });
  },

  getSaleById: async (id: number): Promise<Sale> => {
    return api.get(`/sales/${id}`);
  },

  generateInvoice: async (id: number): Promise<Blob> => {
    return api.get(`/sales/invoice/${id}`, { responseType: 'blob' });
  },
};
```

---

## 6. AUTHENTICATION & AUTHORIZATION

### 6.1 JWT Implementation

**Token Structure:**
```json
{
  "sub": "admin",
  "userId": 1,
  "role": "ADMIN",
  "fullName": "System Administrator",
  "iat": 1711008000,
  "exp": 1711011600
}
```

**Java Implementation (Spring Boot):**

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("fullName", user.getFullName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 6.2 Role-Based Access Control (RBAC)

**Permission Matrix:**

| Module | Admin | Manager | Cashier | Inventory Manager |
|--------|-------|---------|---------|-------------------|
| Dashboard | ✅ Full | ✅ Full | ✅ Limited | ✅ Limited |
| Products | ✅ Full | ✅ Full | ❌ View Only | ✅ Full |
| Inventory | ✅ Full | ✅ Full | ❌ View Only | ✅ Full |
| Purchase | ✅ Full | ✅ Full | ❌ No Access | ✅ Full |
| POS/Sales | ✅ Full | ✅ Full | ✅ Full | ❌ View Only |
| Customers | ✅ Full | ✅ Full | ✅ View Only | ❌ View Only |
| Employees | ✅ Full | ✅ View Only | ❌ No Access | ❌ No Access |
| Expenses | ✅ Full | ✅ Full | ❌ No Access | ❌ No Access |
| Reports | ✅ Full | ✅ Full | ❌ Sales Only | ✅ Inventory Only |
| Settings | ✅ Full | ❌ No Access | ❌ No Access | ❌ No Access |
| Users | ✅ Full | ❌ No Access | ❌ No Access | ❌ No Access |

**Spring Security Configuration:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors()
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/v1/expenses/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated()
                .requestMatchers("/api/v1/pos/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**React Permission Hook:**

```typescript
// hooks/usePermission.ts
import { useSelector } from 'react-redux';
import { RootState } from '../store';

type Role = 'ADMIN' | 'MANAGER' | 'CASHIER' | 'INVENTORY_MANAGER';
type Permission = 'create' | 'read' | 'update' | 'delete';

const PERMISSIONS: Record<string, Record<Role, Permission[]>> = {
  products: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    MANAGER: ['create', 'read', 'update', 'delete'],
    INVENTORY_MANAGER: ['create', 'read', 'update', 'delete'],
    CASHIER: ['read'],
  },
  sales: {
    ADMIN: ['create', 'read', 'update', 'delete'],
    MANAGER: ['create', 'read', 'update', 'delete'],
    CASHIER: ['create', 'read'],
    INVENTORY_MANAGER: ['read'],
  },
  // ... other modules
};

export const usePermission = () => {
  const user = useSelector((state: RootState) => state.auth.user);

  const hasPermission = (module: string, permission: Permission): boolean => {
    if (!user) return false;
    const modulePermissions = PERMISSIONS[module]?.[user.role as Role];
    return modulePermissions?.includes(permission) || false;
  };

  const hasRole = (...roles: Role[]): boolean => {
    return user ? roles.includes(user.role as Role) : false;
  };

  return { hasPermission, hasRole };
};
```

---

## 7. BATCH TRACKING IMPLEMENTATION

### 7.1 Batch Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                    BATCH LIFECYCLE                           │
└─────────────────────────────────────────────────────────────┘

1. CREATION (During Goods Receiving)
   ↓
   [Purchase Order] → [Goods Receiving (GRN)] → [Batch Created]
   - Assign batch number
   - Set manufacturing date
   - Set expiry date (if applicable)
   - Link to supplier

2. STORAGE (In Inventory)
   ↓
   [Inventory Batch] → [Stock Quantity Tracked]
   - quantity_remaining updated in real-time
   - Available for sale

3. SALE (During POS Transaction)
   ↓
   [Sale] → [Batch Allocation] → [Stock Deduction]
   - FIFO strategy (First In, First Out)
   - Deduct from oldest batch first
   - Track which batch items sold from

4. REPORTING
   ↓
   [Batch Reports] → [Expiry Alerts] → [Batch-wise Profit]
   - Know which batches are moving fast/slow
   - Alert on expiring batches
   - Calculate profit per batch
```

### 7.2 FIFO Batch Allocation Algorithm

```java
@Service
public class BatchAllocationService {

    @Autowired
    private InventoryBatchRepository batchRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    /**
     * Allocate stock from batches using FIFO strategy
     * @param variantId Product variant ID
     * @param quantityNeeded Quantity to allocate
     * @return List of batch allocations
     */
    public List<BatchAllocation> allocateStock(Long variantId, Integer quantityNeeded) {
        // Get all batches for this variant, ordered by received date (FIFO)
        List<InventoryBatch> batches = batchRepository
                .findByProductVariantIdAndQuantityRemainingGreaterThan(variantId, 0,
                        Sort.by(Sort.Direction.ASC, "receivedDate"));

        if (batches.isEmpty()) {
            throw new InsufficientStockException("No stock available for variant ID: " + variantId);
        }

        List<BatchAllocation> allocations = new ArrayList<>();
        int remainingToAllocate = quantityNeeded;

        for (InventoryBatch batch : batches) {
            if (remainingToAllocate <= 0) break;

            int allocateFromBatch = Math.min(batch.getQuantityRemaining(), remainingToAllocate);

            allocations.add(BatchAllocation.builder()
                    .batchId(batch.getId())
                    .batchNumber(batch.getBatchNumber())
                    .quantity(allocateFromBatch)
                    .costPrice(batch.getCostPrice())
                    .build());

            remainingToAllocate -= allocateFromBatch;
        }

        if (remainingToAllocate > 0) {
            throw new InsufficientStockException(
                String.format("Insufficient stock. Required: %d, Available: %d",
                        quantityNeeded, quantityNeeded - remainingToAllocate)
            );
        }

        return allocations;
    }

    /**
     * Deduct stock from batches and create stock movement records
     */
    @Transactional
    public void deductStock(Long saleId, List<BatchAllocation> allocations) {
        for (BatchAllocation allocation : allocations) {
            InventoryBatch batch = batchRepository.findById(allocation.getBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

            // Deduct quantity
            batch.setQuantityRemaining(batch.getQuantityRemaining() - allocation.getQuantity());
            batchRepository.save(batch);

            // Create stock movement record
            StockMovement movement = StockMovement.builder()
                    .productVariantId(batch.getProductVariantId())
                    .batchId(batch.getId())
                    .movementType(MovementType.SALE)
                    .quantity(-allocation.getQuantity()) // Negative for outward movement
                    .referenceType("SALE")
                    .referenceId(saleId)
                    .build();
            stockMovementRepository.save(movement);
        }

        // Also update product_variants stock_quantity
        updateVariantStockQuantity(allocations.get(0).getVariantId());
    }
}
```

### 7.3 Batch Expiry Tracking

```java
@Service
public class BatchExpiryService {

    @Autowired
    private InventoryBatchRepository batchRepository;

    /**
     * Get batches expiring within specified days
     */
    public List<InventoryBatch> getBatchesExpiringWithin(Integer days) {
        LocalDate expiryThreshold = LocalDate.now().plusDays(days);
        return batchRepository.findByExpiryDateLessThanEqualAndQuantityRemainingGreaterThan(
                expiryThreshold, 0);
    }

    /**
     * Get expired batches still in stock
     */
    public List<InventoryBatch> getExpiredBatches() {
        return batchRepository.findByExpiryDateLessThanAndQuantityRemainingGreaterThan(
                LocalDate.now(), 0);
    }

    /**
     * Scheduled task to send expiry alerts (runs daily)
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
    public void sendExpiryAlerts() {
        // Alert for batches expiring in 30 days
        List<InventoryBatch> expiringBatches = getBatchesExpiringWithin(30);

        if (!expiringBatches.isEmpty()) {
            // Send notification (email/SMS/dashboard alert)
            notificationService.sendExpiryAlert(expiringBatches);
        }
    }
}
```

---

## 8. DATA MIGRATION STRATEGY

### 8.1 Migration Process

```
┌────────────────────────────────────────────────────────┐
│              DATA MIGRATION WORKFLOW                    │
└────────────────────────────────────────────────────────┘

Step 1: Data Assessment
- Analyze existing system data structure
- Identify data types: Products, Customers, Inventory, etc.
- Estimate data volume
- Check data quality

Step 2: Data Extraction
- Export data from existing system
- Preferred formats: Excel (.xlsx), CSV, SQL dump
- Verify completeness

Step 3: Data Transformation
- Clean data (remove duplicates, fix formatting)
- Map old fields to new schema
- Generate missing fields (SKU, barcode)
- Validate data integrity

Step 4: Test Migration
- Import data into staging database
- Run validation queries
- Generate reconciliation reports
- Fix any issues

Step 5: Production Migration
- Schedule migration (off-hours preferred)
- Backup existing data (if any)
- Import cleaned data
- Verify all records

Step 6: Post-Migration Validation
- Compare record counts
- Verify relationships (products → variants)
- Test application functionality
- Generate migration report
```

### 8.2 Migration Tools

**Java Migration Service:**

```java
@Service
public class DataMigrationService {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    /**
     * Import products from Excel file
     */
    @Transactional
    public MigrationResult importProducts(MultipartFile file) {
        MigrationResult result = new MigrationResult();

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    ProductDTO product = parseProductRow(row);
                    productService.createProduct(product);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(row.getRowNum(), e.getMessage());
                }
            }

            workbook.close();
        } catch (IOException e) {
            throw new MigrationException("Failed to read Excel file", e);
        }

        return result;
    }

    private ProductDTO parseProductRow(Row row) {
        return ProductDTO.builder()
                .name(getCellValueAsString(row.getCell(0)))
                .categoryName(getCellValueAsString(row.getCell(1)))
                .brand(getCellValueAsString(row.getCell(2)))
                .productType(getCellValueAsString(row.getCell(3)))
                .size(getCellValueAsString(row.getCell(4)))
                .color(getCellValueAsString(row.getCell(5)))
                .costPrice(getCellValueAsDouble(row.getCell(6)))
                .sellingPrice(getCellValueAsDouble(row.getCell(7)))
                .stockQuantity(getCellValueAsInt(row.getCell(8)))
                .build();
    }

    /**
     * Import customers from CSV
     */
    @Transactional
    public MigrationResult importCustomers(MultipartFile file) {
        MigrationResult result = new MigrationResult();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            boolean isHeader = true;

            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    CustomerDTO customer = parseCustomerCSV(line);
                    customerService.createCustomer(customer);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(line[0], e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new MigrationException("Failed to read CSV file", e);
        }

        return result;
    }
}
```

**Migration Data Templates:**

**Products Template (Excel):**
```
| Name | Category | Brand | Type | Size | Color | Cost Price | Selling Price | Stock Qty |
|------|----------|-------|------|------|-------|------------|---------------|-----------|
| Cotton Shirt | Male > Shirts | Gul Ahmed | STITCHED | M | Blue | 1200 | 1800 | 50 |
| Lawn Suit | Female > Suits | Khaadi | UNSTITCHED | 3.5m | Green | 3000 | 4500 | 20 |
```

**Customers Template (CSV):**
```csv
Name,Phone,Email,Address,City,Credit Limit
John Doe,+923001234567,john@example.com,House 123 Street 5,Lahore,50000
Jane Smith,+923009876543,jane@example.com,Flat 45 Block A,Karachi,30000
```

### 8.3 Migration REST APIs

```java
@RestController
@RequestMapping("/api/v1/migration")
@PreAuthorize("hasRole('ADMIN')")
public class MigrationController {

    @Autowired
    private DataMigrationService migrationService;

    @PostMapping("/products")
    public ResponseEntity<MigrationResult> importProducts(
            @RequestParam("file") MultipartFile file) {
        MigrationResult result = migrationService.importProducts(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/customers")
    public ResponseEntity<MigrationResult> importCustomers(
            @RequestParam("file") MultipartFile file) {
        MigrationResult result = migrationService.importCustomers(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/suppliers")
    public ResponseEntity<MigrationResult> importSuppliers(
            @RequestParam("file") MultipartFile file) {
        MigrationResult result = migrationService.importSuppliers(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        MigrationStatus status = migrationService.getStatus();
        return ResponseEntity.ok(status);
    }
}
```

---

## 9. DEVELOPMENT ENVIRONMENT SETUP

### 9.1 Backend Setup

**Prerequisites:**
- JDK 21
- Maven 3.9+ or Gradle 8+
- PostgreSQL 16
- IDE: IntelliJ IDEA / Eclipse

**Step-by-Step:**

```bash
# 1. Clone repository
git clone <repository-url>
cd retail-inventory-backend

# 2. Configure database
# Edit src/main/resources/application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/retail_inventory
spring.datasource.username=postgres
spring.datasource.password=your_password

# 3. Install dependencies
mvn clean install
# or
./gradlew build

# 4. Run database migrations
mvn flyway:migrate
# or
./gradlew flywayMigrate

# 5. Run application
mvn spring-boot:run
# or
./gradlew bootRun

# Application will start on http://localhost:8080
```

**application.properties:**

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/retail_inventory
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=3600000

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload.dir=./uploads

# Logging
logging.level.com.retail.inventory=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
```

### 9.2 Frontend Setup

**Prerequisites:**
- Node.js 20+
- npm or yarn

**Step-by-Step:**

```bash
# 1. Navigate to frontend directory
cd retail-inventory-frontend

# 2. Install dependencies
npm install
# or
yarn install

# 3. Configure environment variables
# Create .env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_TITLE=Retail Inventory System

# 4. Run development server
npm run dev
# or
yarn dev

# Application will start on http://localhost:5173
```

**package.json:**

```json
{
  "name": "retail-inventory-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\""
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "@reduxjs/toolkit": "^2.0.0",
    "react-redux": "^9.0.0",
    "axios": "^1.6.0",
    "antd": "^5.12.0",
    "@ant-design/icons": "^5.2.0",
    "react-hook-form": "^7.49.0",
    "yup": "^1.3.0",
    "date-fns": "^3.0.0",
    "recharts": "^2.10.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@vitejs/plugin-react": "^4.2.0",
    "typescript": "^5.3.0",
    "vite": "^5.0.0",
    "eslint": "^8.55.0",
    "prettier": "^3.1.0"
  }
}
```

### 9.3 Database Setup

```bash
# Install PostgreSQL 16

# Create database
psql -U postgres
CREATE DATABASE retail_inventory;
CREATE USER retail_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE retail_inventory TO retail_user;

# Exit psql
\q
```

---

## 10. DEPLOYMENT ARCHITECTURE

### 10.1 Production Deployment (Windows Server)

```
Server Setup:
┌──────────────────────────────────────────────────┐
│         Windows Server 2019/2022                 │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  PostgreSQL 16 (Windows Service)           │ │
│  │  - Port: 5432                              │ │
│  │  - Data: C:\PostgreSQL\data                │ │
│  │  - Backups: C:\PostgreSQL\backups          │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │  Java 21 JRE                               │ │
│  │  Spring Boot Application (JAR)             │ │
│  │  - Port: 8080                              │ │
│  │  - Auto-start: Windows Service             │ │
│  │  - Logs: C:\RetailInventory\logs           │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  Static IP: 192.168.1.100                        │
│  Hostname: RETAIL-SERVER                         │
└──────────────────────────────────────────────────┘

Client Access:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Client PC 1  │  │ Client PC 2  │  │ Client PC 3  │
│ Browser:     │  │ Browser:     │  │ Browser:     │
│ http://192.  │  │ http://192.  │  │ http://192.  │
│ 168.1.100:   │  │ 168.1.100:   │  │ 168.1.100:   │
│ 8080         │  │ 8080         │  │ 8080         │
└──────────────┘  └──────────────┘  └──────────────┘
```

### 10.2 Build & Deployment Steps

**Backend Build:**

```bash
# Build JAR file
cd retail-inventory-backend
mvn clean package -DskipTests
# or
./gradlew build -x test

# JAR file will be at: target/retail-inventory-1.0.0.jar
```

**Frontend Build:**

```bash
# Build production bundle
cd retail-inventory-frontend
npm run build
# or
yarn build

# Output: dist/ folder (static files)
```

**Deploy:**

```bash
# 1. Copy JAR to server
# Place at: C:\RetailInventory\retail-inventory.jar

# 2. Copy frontend build to server
# Place at: C:\RetailInventory\frontend\

# 3. Configure Spring Boot to serve React frontend
# In application-prod.properties:
spring.web.resources.static-locations=file:C:/RetailInventory/frontend/

# 4. Create Windows Service (using NSSM or WinSW)
nssm install RetailInventoryService "C:\Program Files\Java\jdk-21\bin\java.exe" "-jar C:\RetailInventory\retail-inventory.jar --spring.profiles.active=prod"

# 5. Start service
nssm start RetailInventoryService
```

### 10.3 Database Backup Strategy

**Automated Daily Backups:**

```sql
-- Create backup script (backup.bat)
@echo off
SET PGPASSWORD=your_password
SET BACKUP_DIR=C:\PostgreSQL\backups
SET DATE=%date:~-4,4%%date:~-7,2%%date:~-10,2%
SET TIME=%time:~0,2%%time:~3,2%%time:~6,2%
SET BACKUP_FILE=%BACKUP_DIR%\retail_inventory_%DATE%_%TIME%.sql

"C:\Program Files\PostgreSQL\16\bin\pg_dump.exe" -U retail_user -h localhost -F c retail_inventory > %BACKUP_FILE%

echo Backup completed: %BACKUP_FILE%
```

**Schedule with Windows Task Scheduler:**
- Run daily at 2:00 AM
- Keep last 30 days of backups
- Copy weekly backups to external drive

---

## 11. SECURITY CONSIDERATIONS

### 11.1 Security Checklist

- ✅ Password hashing (BCrypt with strength 12)
- ✅ JWT tokens with expiration (1 hour access, 7 days refresh)
- ✅ HTTPS (SSL certificate in production)
- ✅ SQL injection prevention (Parameterized queries via JPA)
- ✅ XSS protection (React auto-escapes, CSP headers)
- ✅ CSRF protection (disabled for stateless API, enabled for forms)
- ✅ Rate limiting (prevent brute force login attempts)
- ✅ Input validation (server-side + client-side)
- ✅ Audit logging (user activities tracked)
- ✅ Role-based access control (RBAC)
- ✅ Secure file uploads (validate file types, size limits)
- ✅ Database backups (encrypted, off-site storage)

### 11.2 Password Policy

```java
@Component
public class PasswordValidator {

    public boolean isValid(String password) {
        // Minimum 8 characters
        // At least 1 uppercase, 1 lowercase, 1 digit
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return password.matches(pattern);
    }
}
```

### 11.3 API Rate Limiting

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();

        if (isRateLimitExceeded(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitExceeded(String clientIp) {
        long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(clientIp, new ArrayList<>());

        List<Long> requests = requestCounts.get(clientIp);
        requests.removeIf(time -> currentTime - time > 60000); // Remove requests older than 1 minute

        if (requests.size() >= MAX_REQUESTS_PER_MINUTE) {
            return true;
        }

        requests.add(currentTime);
        return false;
    }
}
```

---

## 12. PERFORMANCE OPTIMIZATION

### 12.1 Database Optimization

**Indexes:**
```sql
-- Critical indexes for performance
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_barcode ON product_variants(barcode);
CREATE INDEX idx_variants_sku ON product_variants(sku);
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sale_items_variant ON sale_items(product_variant_id);
CREATE INDEX idx_batches_variant ON inventory_batches(product_variant_id);
CREATE INDEX idx_batches_expiry ON inventory_batches(expiry_date);
```

**Query Optimization:**
```java
// Use JOIN FETCH to avoid N+1 queries
@Query("SELECT p FROM Product p JOIN FETCH p.variants WHERE p.id = :id")
Product findByIdWithVariants(@Param("id") Long id);

// Use pagination for large result sets
Page<Product> findAll(Pageable pageable);

// Use projections for list views (avoid loading unnecessary data)
@Query("SELECT new com.retail.inventory.dto.ProductListDTO(p.id, p.name, p.brand, c.name) " +
       "FROM Product p JOIN p.category c")
List<ProductListDTO> findAllForList();
```

### 12.2 Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "categories",
            "expenseCategories",
            "activeProducts"
        );
    }
}

@Service
public class CategoryService {

    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(CategoryDTO dto) {
        // ... create logic
    }
}
```

### 12.3 Frontend Optimization

- **Code Splitting:** Lazy load routes with React.lazy()
- **Memoization:** Use React.memo() for expensive components
- **Debounce:** Debounce search inputs (500ms)
- **Virtual Scrolling:** For large lists (use react-window)
- **Image Optimization:** Compress images, lazy load
- **Bundle Size:** Analyze with webpack-bundle-analyzer

```typescript
// Lazy loading routes
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Products = lazy(() => import('./pages/Products'));
const POS = lazy(() => import('./pages/POS'));

// Debounced search
const debouncedSearch = useDebounce(searchQuery, 500);
```

---

## 13. TESTING STRATEGY

### 13.1 Backend Testing

**Unit Tests (JUnit 5 + Mockito):**

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        // Given
        ProductDTO dto = new ProductDTO(/* ... */);
        Product product = new Product(/* ... */);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        Product result = productService.createProduct(dto);

        // Then
        assertNotNull(result);
        assertEquals("Cotton Shirt", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
```

**Integration Tests (Spring Boot Test):**

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .header("Authorization", "Bearer " + getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
```

### 13.2 Frontend Testing

**Component Tests (React Testing Library):**

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import ProductCard from './ProductCard';

test('renders product card with correct data', () => {
  const product = {
    id: 1,
    name: 'Cotton Shirt',
    price: 1800,
  };

  render(<ProductCard product={product} />);

  expect(screen.getByText('Cotton Shirt')).toBeInTheDocument();
  expect(screen.getByText('Rs. 1,800')).toBeInTheDocument();
});
```

---

## 14. APPENDIX

### 14.1 Glossary

- **SKU:** Stock Keeping Unit (unique product identifier)
- **GRN:** Goods Receiving Note (document confirming goods receipt)
- **POS:** Point of Sale (checkout system)
- **FIFO:** First In, First Out (inventory valuation method)
- **RBAC:** Role-Based Access Control
- **JWT:** JSON Web Token (authentication token)
- **ORM:** Object-Relational Mapping
- **API:** Application Programming Interface
- **REST:** Representational State Transfer

### 14.2 Useful Commands

```bash
# Backend
mvn spring-boot:run                     # Run backend
mvn test                                # Run tests
mvn clean package                       # Build JAR

# Frontend
npm run dev                             # Run dev server
npm run build                           # Production build
npm run lint                            # Lint code

# Database
psql -U postgres -d retail_inventory    # Connect to DB
pg_dump -U postgres retail_inventory > backup.sql  # Backup
psql -U postgres retail_inventory < backup.sql     # Restore
```

### 14.3 Contact & Support

For technical queries during development:
- Backend Lead: [Name]
- Frontend Lead: [Name]
- Database Admin: [Name]
- Project Manager: [Name]

---

**Document End**

**Version:** 1.0
**Last Updated:** March 21, 2026
**Status:** Ready for Development
