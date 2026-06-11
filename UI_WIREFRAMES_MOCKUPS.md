# UI WIREFRAMES & MOCKUPS
## Retail Clothes Inventory Management System

**Version:** 1.0
**Date:** March 21, 2026
**Purpose:** Visual design specifications for development team

---

## TABLE OF CONTENTS

1. [Design System & Guidelines](#1-design-system--guidelines)
2. [Login Screen](#2-login-screen)
3. [Dashboard](#3-dashboard)
4. [Point of Sale (POS) Screen](#4-point-of-sale-pos-screen)
5. [Product Management](#5-product-management)
6. [Inventory Management](#6-inventory-management)
7. [Purchase Order Screen](#7-purchase-order-screen)
8. [Customer Management](#8-customer-management)
9. [Employee Management](#9-employee-management)
10. [Daily Expenses](#10-daily-expenses)
11. [Reports & Analytics](#11-reports--analytics)
12. [Invoice/Receipt Template](#12-invoicereceipt-template)

---

## 1. DESIGN SYSTEM & GUIDELINES

### 1.1 Color Palette

```
Primary Colors:
- Primary:     #1890ff (Blue)
- Success:     #52c41a (Green)
- Warning:     #faad14 (Orange)
- Error:       #f5222d (Red)
- Info:        #13c2c2 (Cyan)

Neutral Colors:
- Text Primary:    #262626
- Text Secondary:  #595959
- Background:      #f0f2f5
- Border:          #d9d9d9
- White:           #ffffff

Status Colors:
- In Stock:        #52c41a (Green)
- Low Stock:       #faad14 (Orange)
- Out of Stock:    #f5222d (Red)
- Paid:            #52c41a (Green)
- Pending:         #faad14 (Orange)
- Overdue:         #f5222d (Red)
```

### 1.2 Typography

```
Font Family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial

Headings:
- H1: 32px, Bold
- H2: 24px, Bold
- H3: 20px, Semibold
- H4: 16px, Semibold

Body:
- Regular: 14px
- Small: 12px
- Large: 16px
```

### 1.3 Spacing & Layout

```
Container Max Width: 1400px
Grid Columns: 24 columns (Ant Design Grid)
Gutter: 16px

Spacing Scale:
- xs:  4px
- sm:  8px
- md:  16px
- lg:  24px
- xl:  32px
- xxl: 48px
```

### 1.4 Component Library

**Use Ant Design (antd) components:**
- Buttons, Forms, Tables, Modals
- Icons from @ant-design/icons
- Responsive grid system
- Pre-built form validation

---

## 2. LOGIN SCREEN

### 2.1 Layout

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    [COMPANY LOGO]                           │
│                                                             │
│              RETAIL INVENTORY SYSTEM                        │
│                                                             │
│         ┌──────────────────────────────────┐               │
│         │                                  │               │
│         │  Username  [________________]    │               │
│         │                                  │               │
│         │  Password  [________________]    │               │
│         │                                  │               │
│         │  [ ] Remember me                 │               │
│         │                                  │               │
│         │     [      LOGIN BUTTON      ]   │               │
│         │                                  │               │
│         │     Forgot Password?             │               │
│         │                                  │               │
│         └──────────────────────────────────┘               │
│                                                             │
│              Version 1.0 | © 2026                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Specifications

**Card:**
- Width: 400px
- Padding: 40px
- Border Radius: 8px
- Box Shadow: 0 4px 12px rgba(0,0,0,0.15)
- Background: White

**Logo:**
- Size: 120px x 120px
- Margin bottom: 24px

**Input Fields:**
- Height: 40px
- Border: 1px solid #d9d9d9
- Border Radius: 4px
- Focus: Border color #1890ff

**Login Button:**
- Width: 100%
- Height: 40px
- Background: #1890ff
- Color: White
- Border Radius: 4px
- Hover: Background #40a9ff

**Validation:**
- Show error message below field on invalid input
- Red border on error
- Disable button while submitting

---

## 3. DASHBOARD

### 3.1 Layout

```
┌─────────────────────────────────────────────────────────────────────────┐
│ NAVBAR: [Logo] Dashboard  Products  Inventory  POS  ...  [User ▼] [🔔] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  DASHBOARD                                          [Date Range Picker]│
│                                                                         │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │
│  │ 💰 TOTAL     │ │ 📦 TOTAL     │ │ 👥 CUSTOMERS │ │ ⚠️  LOW      │  │
│  │    SALES     │ │   PRODUCTS   │ │              │ │    STOCK     │  │
│  │              │ │              │ │              │ │              │  │
│  │  Rs 2.5M     │ │    1,245     │ │     458      │ │     12       │  │
│  │  +12.5% ↑    │ │  In Stock    │ │  This Month  │ │   Items      │  │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘  │
│                                                                         │
│  ┌────────────────────────────────────┐ ┌──────────────────────────┐   │
│  │ 📊 SALES TREND (Last 7 Days)       │ │ 🔥 TOP SELLING PRODUCTS  │   │
│  │                                    │ │                          │   │
│  │     [LINE CHART]                   │ │  1. Cotton Shirt - 145   │   │
│  │                                    │ │  2. Lawn Suit - 98       │   │
│  │                                    │ │  3. Jeans - 87           │   │
│  │                                    │ │  4. Kurta - 76           │   │
│  │                                    │ │  5. Shawl - 65           │   │
│  └────────────────────────────────────┘ └──────────────────────────┘   │
│                                                                         │
│  ┌────────────────────────────────────┐ ┌──────────────────────────┐   │
│  │ 🛒 RECENT SALES                    │ │ 💼 PENDING TASKS         │   │
│  │                                    │ │                          │   │
│  │ INV-001  Walk-in  Rs 5,400  2h ago│ │ ⚠️ 3 Batches expiring   │   │
│  │ INV-002  Ali Khan Rs 3,200  3h ago│ │ 📦 5 POs to receive     │   │
│  │ INV-003  Walk-in  Rs 8,900  4h ago│ │ 💰 12 Pending payments  │   │
│  │                                    │ │                          │   │
│  │           [View All Sales]         │ │    [View All Tasks]      │   │
│  └────────────────────────────────────┘ └──────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Dashboard Cards Specifications

**Stat Cards (Top Row):**
- Width: 25% each (responsive: 50% on tablet, 100% on mobile)
- Height: 120px
- Padding: 20px
- Border Radius: 8px
- Background: White
- Box Shadow: 0 2px 8px rgba(0,0,0,0.1)

**Icon:**
- Size: 40px
- Color: Primary color

**Value:**
- Font Size: 32px
- Font Weight: Bold
- Color: #262626

**Label:**
- Font Size: 14px
- Color: #595959

**Change Indicator:**
- Font Size: 12px
- Green for positive, Red for negative
- Arrow icon up/down

**Charts & Lists:**
- Background: White
- Border Radius: 8px
- Padding: 24px
- Box Shadow: 0 2px 8px rgba(0,0,0,0.1)

---

## 4. POINT OF SALE (POS) SCREEN

### 4.1 Layout (Most Important Screen!)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│ POS                                    [Cashier: John Doe]  [Cash Register: OPEN]    │
├────────────────────────────────┬─────────────────────────────────────────────────────┤
│                                │                                                     │
│  PRODUCT SEARCH & SELECTION    │              SHOPPING CART                          │
│                                │                                                     │
│  [🔍 Search by name/barcode_]  │  Customer: [Select Customer ▼] or Walk-in          │
│  [📷] Scan Barcode             │                                                     │
│                                │  ┌───────────────────────────────────────────────┐ │
│  Categories:                   │  │ Item          Size  Color  Qty  Price   Total │ │
│  [All] [Male] [Female] [Kids]  │  ├───────────────────────────────────────────────┤ │
│                                │  │ Cotton Shirt  M     Blue   2    1,800   3,600 │ │
│  ┌────────────────────────┐    │  │ Jeans         32    Black  1    2,500   2,500 │ │
│  │ [IMG] Cotton Shirt     │    │  │ Lawn Suit     3.5m  Green  1    4,500   4,500 │ │
│  │ Rs 1,800 | Stock: 45   │    │  │                                  [X] [EDIT]    │ │
│  │ [+ ADD TO CART]        │    │  └───────────────────────────────────────────────┘ │
│  └────────────────────────┘    │                                                     │
│                                │  Subtotal:              Rs 10,600                   │
│  ┌────────────────────────┐    │  Discount: [___] % or Rs  - Rs 0                   │
│  │ [IMG] Jeans            │    │  Tax (0%):                Rs 0                      │
│  │ Rs 2,500 | Stock: 23   │    │  ──────────────────────────────                    │
│  │ [+ ADD TO CART]        │    │  TOTAL:                 Rs 10,600                   │
│  └────────────────────────┘    │                                                     │
│                                │  ┌─────────────────────────────────────────────┐   │
│  ┌────────────────────────┐    │  │ PAYMENT                                     │   │
│  │ [IMG] Lawn Suit        │    │  │                                             │   │
│  │ Rs 4,500 | Stock: 12   │    │  │ [💵 CASH] [💳 CARD] [📱 JAZZCASH] [🏦 BANK]│   │
│  │ [+ ADD TO CART]        │    │  │                                             │   │
│  └────────────────────────┘    │  │ Received: [___________]  Change: Rs 0       │   │
│                                │  │                                             │   │
│  [Load More Products...]       │  │    [HOLD]      [CLEAR]    [💰 CHECKOUT]    │   │
│                                │  └─────────────────────────────────────────────┘   │
│                                │                                                     │
└────────────────────────────────┴─────────────────────────────────────────────────────┘
```

### 4.2 POS Screen Specifications

**Screen Split:**
- Left Panel: 40% width (Product selection)
- Right Panel: 60% width (Cart & Payment)

**Product Search:**
- Input Height: 48px (large, touch-friendly)
- Auto-complete dropdown
- Debounce search: 300ms
- Show SKU, barcode, name in results

**Barcode Scanner Button:**
- Size: 48px x 48px
- Opens camera/scanner input
- USB barcode scanner auto-focuses search

**Category Filters:**
- Horizontal tabs
- Active tab: Primary color background
- Click to filter products

**Product Cards:**
- Width: 100% (stack vertically)
- Height: Auto
- Image: 80px x 80px
- Display: Name, Price, Stock
- "Low Stock" badge if < reorder level
- Add to Cart button: Full width, Primary color

**Shopping Cart:**
- Scrollable table (max height: 400px)
- Columns: Item, Size, Color, Qty, Price, Total
- Edit button: Opens quantity modal
- Remove button: X icon, red color
- Empty state: "No items in cart"

**Cart Actions:**
- Quantity: Can edit inline or via modal
- Remove item: Confirmation prompt
- Clear cart: Confirmation prompt

**Discount:**
- Toggle between % and fixed amount
- Real-time calculation
- Validate: Max 100%, or max subtotal

**Payment Section:**
- Payment method buttons: Large, icon + text
- Multiple payments: Click multiple buttons to split
- Received amount: Auto-focuses on amount input
- Change calculation: Real-time
- Checkout button: Large (60px height), Green color

**Keyboard Shortcuts:**
- F1: Focus search
- F2: Scan barcode
- F3: Select customer
- F4: Apply discount
- F5: Cash payment
- F6: Card payment
- F9: Hold transaction
- F10: Clear cart
- F12: Checkout

**Hold Transaction:**
- Saves current cart
- Shows list of held transactions
- Can retrieve and continue

---

## 5. PRODUCT MANAGEMENT

### 5.1 Product List Screen

```
┌─────────────────────────────────────────────────────────────────────────┐
│ PRODUCTS                                          [+ Add New Product]   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [🔍 Search products...]    Category: [All ▼]   Type: [All ▼]          │
│  [📂 Manage Categories]     [📥 Import]  [📤 Export]                    │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ ☑  Image   Name         Category    Brand    Type      Stock   │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ ☐  [IMG]  Cotton Shirt  Male>Shirts GulAhmed STITCHED  45     │   │
│  │           SKU: GUL-001 | 3 variants                    [Edit]  │   │
│  │                                                       [Delete] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ ☐  [IMG]  Lawn Suit     Female>Suits Khaadi  UNSTITCHED 12    │   │
│  │           SKU: KHA-002 | 2 variants                    [Edit]  │   │
│  │                                                       [Delete] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ ☐  [IMG]  Kids Jeans    Kids>Pants   Levi's  STITCHED  28     │   │
│  │           SKU: LEV-003 | 5 variants                    [Edit]  │   │
│  │                                                       [Delete] │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  Showing 1-10 of 245 products              [< 1 2 3 4 ... 25 >]        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Add/Edit Product Form

```
┌─────────────────────────────────────────────────────────────────────────┐
│ ADD NEW PRODUCT                                        [Save] [Cancel]  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  BASIC INFORMATION                                                      │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ Product Name *      [_____________________________________]        │ │
│  │                                                                   │ │
│  │ Category *          [Select Category ▼]    [+ Add New]           │ │
│  │                                                                   │ │
│  │ Brand               [_____________________________________]        │ │
│  │                                                                   │ │
│  │ Product Type *      ⚪ Stitched   ⚪ Unstitched                   │ │
│  │                                                                   │ │
│  │ Description         [________________________________]            │ │
│  │                     [________________________________]            │ │
│  │                     [________________________________]            │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  PRODUCT IMAGES                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ [📷 Upload]  [IMG] [IMG] [IMG]  (Max 5 images)                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  VARIANTS                                               [+ Add Variant]│
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ Size  Color  SKU        Barcode    Cost Price  Selling  Reorder  │ │
│  ├───────────────────────────────────────────────────────────────────┤ │
│  │ M     Blue   GUL-001-M  AUTO-GEN   1,200       1,800     5  [X]  │ │
│  │ L     Blue   GUL-001-L  AUTO-GEN   1,200       1,800     5  [X]  │ │
│  │ XL    Blue   GUL-001-XL AUTO-GEN   1,200       1,800     5  [X]  │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ☑ Generate barcodes automatically                                     │
│  ☑ Product is active                                                   │
│                                                                         │
│                                          [Save Product] [Save & New]   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.3 Product Form Specifications

**Form Layout:**
- Max Width: 900px
- Sections separated with headings
- Required fields marked with *

**Fields:**
- Input height: 40px
- Border: 1px solid #d9d9d9
- Focus: Border color #1890ff
- Error: Border color #f5222d, show error message below

**Category Selector:**
- Hierarchical dropdown (Male > Shirts)
- "Add New" button opens modal

**Image Upload:**
- Drag & drop area
- Multiple file selection
- Preview thumbnails
- Max 5 images
- Accepted formats: JPG, PNG
- Max size: 2MB per image

**Variants Table:**
- Add row button
- Inline editing
- Remove row button (X)
- SKU auto-generation: BRAND-NAME-SIZE-COLOR
- Barcode auto-generation: Use ZXing library

**Validation:**
- Product name: Required, min 2 chars, max 200
- Category: Required
- At least 1 variant required
- Cost price: Must be > 0
- Selling price: Must be >= cost price
- Reorder level: Default 5

**Save Options:**
- "Save Product": Save and return to list
- "Save & New": Save and open blank form

---

## 6. INVENTORY MANAGEMENT

### 6.1 Stock Summary Screen

```
┌─────────────────────────────────────────────────────────────────────────┐
│ INVENTORY                                                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  STOCK OVERVIEW                                                         │
│                                                                         │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐          │
│  │ 📦 TOTAL   │ │ 💰 STOCK   │ │ ⚠️ LOW     │ │ 🚫 OUT OF  │          │
│  │   ITEMS    │ │  VALUE     │ │   STOCK    │ │   STOCK    │          │
│  │   1,245    │ │ Rs 2.5M    │ │     12     │ │      3     │          │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘          │
│                                                                         │
│  [🔍 Search...]  Category: [All ▼]  Status: [All ▼]  [🔄 Refresh]     │
│  [📊 Stock Adjustment]  [⚠️ Low Stock Alert]  [📅 Expiry Alerts]       │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Product        SKU      Size  Color  Stock  Reorder  Status    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Cotton Shirt   GUL-001  M     Blue    45      5      ✅ In Stock│   │
│  │                                              [Batches] [Adjust] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Lawn Suit      KHA-002  3.5m  Green   12      10     ✅ In Stock│   │
│  │                                              [Batches] [Adjust] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Kids Jeans     LEV-003  28    Black   4       5      ⚠️ Low    │   │
│  │                                              [Batches] [Adjust] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Summer Dress   SUM-004  L     Red     0       5      🚫 Out    │   │
│  │                                              [Batches] [Adjust] │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Batch Details Modal

```
┌─────────────────────────────────────────────────────────────────────┐
│ BATCH DETAILS - Cotton Shirt (M, Blue)                         [✕] │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ Total Stock: 45 units                                               │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐│
│ │ Batch No.    Supplier   Received   Qty Remaining  Expiry      │││
│ ├─────────────────────────────────────────────────────────────────┤│
│ │ BATCH-001    Gul Ahmed  2026-02-15      25          -          │││
│ │ Cost: Rs 1,200 | Mfg: 2026-02-01                               │││
│ ├─────────────────────────────────────────────────────────────────┤│
│ │ BATCH-015    Gul Ahmed  2026-03-10      20          -          │││
│ │ Cost: Rs 1,200 | Mfg: 2026-03-01                               │││
│ └─────────────────────────────────────────────────────────────────┘│
│                                                                     │
│ ℹ️ Sales are allocated using FIFO (First In, First Out) method     │
│                                                                     │
│                                                    [Close] [Export] │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.3 Stock Adjustment Modal

```
┌─────────────────────────────────────────────────────────────┐
│ STOCK ADJUSTMENT                                       [✕] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Product:  Cotton Shirt (M, Blue)                            │
│ Current Stock: 45 units                                     │
│                                                             │
│ Adjustment Type:                                            │
│   ⚪ Increase (Received stock)                              │
│   ⚪ Decrease (Damage/Loss/Theft)                           │
│                                                             │
│ Quantity:  [_____]                                          │
│                                                             │
│ Reason:    [Select reason ▼]                                │
│            - Damaged                                        │
│            - Expired                                        │
│            - Lost/Stolen                                    │
│            - Return to Supplier                             │
│            - Other                                          │
│                                                             │
│ Remarks:   [_________________________________]              │
│            [_________________________________]              │
│                                                             │
│ New Stock: 45 units                                         │
│                                                             │
│                                  [Cancel] [Save Adjustment] │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. PURCHASE ORDER SCREEN

### 7.1 Purchase Order List

```
┌─────────────────────────────────────────────────────────────────────────┐
│ PURCHASE ORDERS                                    [+ Create New PO]    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [🔍 Search PO...]  Supplier: [All ▼]  Status: [All ▼]  Date: [___]   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ PO No.      Supplier    Date       Items  Amount    Status     │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ PO-001      Gul Ahmed   2026-03-15   5    150,000  🟢 RECEIVED │   │
│  │                                                    [View]       │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ PO-002      Khaadi      2026-03-18   3     85,000  🟡 SUBMITTED│   │
│  │                                           [Receive] [View]      │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ PO-003      Levi's      2026-03-20   8    250,000  ⚪ DRAFT    │   │
│  │                                      [Edit] [Submit] [Delete]   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Create Purchase Order Form

```
┌─────────────────────────────────────────────────────────────────────────┐
│ CREATE PURCHASE ORDER                            [Save Draft] [Submit] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  PO Number: PO-004 (Auto-generated)                                     │
│                                                                         │
│  Supplier *         [Select Supplier ▼]  [+ Add New Supplier]          │
│  Order Date         [2026-03-21 📅]                                     │
│  Expected Delivery  [__________ 📅]                                     │
│                                                                         │
│  ITEMS                                              [+ Add Product]     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Product       Size  Color  Quantity  Unit Price  Total     [X] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Cotton Shirt  M     Blue      50      1,200      60,000    [X] │   │
│  │ Cotton Shirt  L     Blue      30      1,200      36,000    [X] │   │
│  │ Lawn Suit     3.5m  Green     20      3,000      60,000    [X] │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│                                          Subtotal:     Rs 156,000       │
│                                          Tax (0%):     Rs 0             │
│                                          Discount:     Rs 0             │
│                                          ──────────────────────         │
│                                          Total:        Rs 156,000       │
│                                                                         │
│  Notes:  [_________________________________________]                    │
│          [_________________________________________]                    │
│                                                                         │
│                                      [Save as Draft] [Submit Order]    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.3 Goods Receiving (GRN) Screen

```
┌─────────────────────────────────────────────────────────────────────────┐
│ GOODS RECEIVING NOTE (GRN) - PO-002                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Supplier:     Khaadi                                                   │
│  PO Date:      2026-03-18                                               │
│  Received Date: [2026-03-21 📅]                                         │
│                                                                         │
│  ITEMS TO RECEIVE                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Product      Size  Color  Ordered  Received  Batch No.  Mfg    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Lawn Suit    3.5m  Green    50      [50]    BATCH-025  [Date]  │   │
│  │ Lawn Suit    3.5m  Red      30      [30]    BATCH-026  [Date]  │   │
│  │ Summer Dress L      Blue    20      [20]    BATCH-027  [Date]  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ☑ All items received in good condition                                │
│  ☐ Partial delivery                                                    │
│                                                                         │
│  Remarks:  [_________________________________________]                  │
│                                                                         │
│                                          [Cancel] [Receive Goods]      │
│                                                                         │
│  ℹ️ Inventory will be updated automatically after receiving            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 8. CUSTOMER MANAGEMENT

### 8.1 Customer List

```
┌─────────────────────────────────────────────────────────────────────────┐
│ CUSTOMERS                                          [+ Add New Customer] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [🔍 Search customers...]  Type: [All ▼]  [📤 Export]                  │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Name           Phone          City    Total Sales  Balance     │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Ali Khan       0300-1234567   Lahore   Rs 125,000   Rs 0       │   │
│  │                                               [View] [Edit]     │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Sara Ahmed     0321-9876543   Karachi  Rs 85,000    Rs 5,000   │   │
│  │                                               [View] [Edit]     │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Hassan Raza    0333-5555555   Multan   Rs 45,000    Rs 0       │   │
│  │                                               [View] [Edit]     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Customer Details Screen

```
┌─────────────────────────────────────────────────────────────────────────┐
│ CUSTOMER DETAILS - Ali Khan                           [Edit] [Delete]  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  INFORMATION                                                            │
│  Name:          Ali Khan                                                │
│  Phone:         0300-1234567                                            │
│  Email:         alikhan@email.com                                       │
│  Address:       House 123, Street 5, DHA Phase 6, Lahore                │
│  Credit Limit:  Rs 50,000                                               │
│                                                                         │
│  STATISTICS                                                             │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                   │
│  │ Total Sales  │ │ Total Paid   │ │ Outstanding  │                   │
│  │ Rs 125,000   │ │ Rs 125,000   │ │ Rs 0         │                   │
│  └──────────────┘ └──────────────┘ └──────────────┘                   │
│                                                                         │
│  PURCHASE HISTORY                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Invoice     Date        Items  Amount      Paid      Balance    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ INV-125     2026-03-20    3    Rs 15,000   Rs 15,000   Rs 0    │   │
│  │ INV-098     2026-03-10    5    Rs 25,000   Rs 25,000   Rs 0    │   │
│  │ INV-056     2026-02-28    2    Rs 8,500    Rs 8,500    Rs 0    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  PAYMENT HISTORY                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Date        Amount      Method      Reference                   │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ 2026-03-20  Rs 15,000   Cash        -                           │   │
│  │ 2026-03-10  Rs 25,000   Bank        TXN123456                   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 9. EMPLOYEE MANAGEMENT

### 9.1 Employee List

```
┌─────────────────────────────────────────────────────────────────────────┐
│ EMPLOYEES                                          [+ Add New Employee] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [🔍 Search employees...]  Status: [Active ▼]                           │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Photo  Name           Designation   Phone        Salary  Status │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ [IMG]  Ahmed Ali      Manager       0300-111111  50,000  Active │   │
│  │                                                   [View] [Edit]  │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ [IMG]  Sara Khan      Cashier       0321-222222  30,000  Active │   │
│  │                                                   [View] [Edit]  │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ [IMG]  Hassan Raza    Helper        0333-333333  25,000  Active │   │
│  │                                                   [View] [Edit]  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 9.2 Employee Details & Salary Tracking

```
┌─────────────────────────────────────────────────────────────────────────┐
│ EMPLOYEE DETAILS - Ahmed Ali                          [Edit] [Delete]  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  [PHOTO]          Name:         Ahmed Ali                              │
│   150x150         CNIC:         12345-1234567-1                         │
│                   Phone:        0300-1111111                            │
│                   Designation:  Manager                                 │
│                   Joining Date: 2025-01-15                              │
│                   Monthly Salary: Rs 50,000                             │
│                   Status:       Active ✅                               │
│                                                                         │
│  SALARY SUMMARY                                     [+ Record Payment]  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                   │
│  │ Total Paid   │ │ Advances     │ │ Outstanding  │                   │
│  │ Rs 150,000   │ │ Rs 10,000    │ │ Rs 0         │                   │
│  └──────────────┘ └──────────────┘ └──────────────┘                   │
│                                                                         │
│  SALARY PAYMENT HISTORY                                                 │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Date        Month    Type     Amount     Method      Ref        │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ 2026-03-31  2026-03  Salary   50,000     Bank        TXN456     │   │
│  │ 2026-02-28  2026-02  Salary   50,000     Bank        TXN345     │   │
│  │ 2026-02-15  2026-02  Advance  10,000     Cash        -          │   │
│  │ 2026-01-31  2026-01  Salary   50,000     Bank        TXN234     │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 9.3 Record Salary Payment Modal

```
┌─────────────────────────────────────────────────────────────┐
│ RECORD SALARY PAYMENT                                  [✕] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Employee:       Ahmed Ali                                   │
│ Monthly Salary: Rs 50,000                                   │
│                                                             │
│ Payment Date:   [2026-03-31 📅]                             │
│                                                             │
│ Salary Month:   [2026-03 ▼]                                 │
│                                                             │
│ Payment Type:   ⚪ Salary  ⚪ Advance  ⚪ Bonus              │
│                                                             │
│ Amount:         [50,000]                                    │
│                                                             │
│ Payment Method: [Bank Transfer ▼]                           │
│                 - Cash                                      │
│                 - Bank Transfer                             │
│                 - JazzCash                                  │
│                 - EasyPaisa                                 │
│                                                             │
│ Reference No:   [________________]                          │
│                                                             │
│ Notes:          [_____________________________]             │
│                                                             │
│                                    [Cancel] [Record Payment]│
└─────────────────────────────────────────────────────────────┘
```

---

## 10. DAILY EXPENSES

### 10.1 Expense List

```
┌─────────────────────────────────────────────────────────────────────────┐
│ DAILY EXPENSES                                        [+ Add Expense]   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Date Range: [2026-03-01] to [2026-03-31]  Category: [All ▼]           │
│  [📊 View Summary]  [⚙️ Manage Categories]                              │
│                                                                         │
│  QUICK STATS (This Month)                                               │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                   │
│  │ Total        │ │ Today        │ │ This Week    │                   │
│  │ Rs 75,000    │ │ Rs 2,500     │ │ Rs 15,000    │                   │
│  └──────────────┘ └──────────────┘ └──────────────┘                   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Date       Category      Amount    Method    Vendor      [Edit] │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ 2026-03-21 Utilities     5,000     Cash      WAPDA       [Del]  │   │
│  │            "Electricity bill for March"                          │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ 2026-03-20 Transportation 1,200   Cash      -           [Del]  │   │
│  │            "Fuel for delivery van"                               │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ 2026-03-18 Raw Materials  8,500   Bank      ABC Traders [Del]  │   │
│  │            "Thread, buttons, zippers"                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 10.2 Add Expense Modal

```
┌─────────────────────────────────────────────────────────────┐
│ ADD EXPENSE                                            [✕] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Expense Date:   [2026-03-21 📅]                             │
│                                                             │
│ Category:       [Utilities ▼]                               │
│                 - Utilities                                 │
│                 - Rent                                      │
│                 - Raw Materials                             │
│                 - Transportation                            │
│                 - Maintenance                               │
│                 - Marketing                                 │
│                 - Office Supplies                           │
│                 - Miscellaneous                             │
│                 [+ Add New Category]                        │
│                                                             │
│ Amount:         [_________]                                 │
│                                                             │
│ Payment Method: [Cash ▼]                                    │
│                                                             │
│ Vendor/Payee:   [_____________________________]             │
│                                                             │
│ Description:    [_____________________________]             │
│                 [_____________________________]             │
│                                                             │
│ Receipt:        [📎 Upload Receipt] (Optional)              │
│                                                             │
│                                      [Cancel] [Add Expense] │
└─────────────────────────────────────────────────────────────┘
```

### 10.3 Expense Summary Report

```
┌─────────────────────────────────────────────────────────────────────────┐
│ EXPENSE SUMMARY REPORT                                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Period: March 2026                                      [📥 Export PDF]│
│                                                                         │
│  TOTAL EXPENSES: Rs 75,000                                              │
│                                                                         │
│  CATEGORY BREAKDOWN                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Category          Amount      %      [Bar Chart]                │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ Utilities         25,000     33.3%   ████████████               │   │
│  │ Rent              20,000     26.7%   ██████████                 │   │
│  │ Raw Materials     15,000     20.0%   ████████                   │   │
│  │ Transportation    8,000      10.7%   ████                       │   │
│  │ Office Supplies   5,000      6.7%    ███                        │   │
│  │ Miscellaneous     2,000      2.7%    █                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  PAYMENT METHOD BREAKDOWN                                               │
│  Cash:         Rs 35,000  (46.7%)                                       │
│  Bank Transfer: Rs 30,000  (40.0%)                                      │
│  Card:         Rs 10,000  (13.3%)                                       │
│                                                                         │
│  TOP EXPENSES                                                           │
│  1. Electricity Bill - Rs 25,000                                        │
│  2. Shop Rent - Rs 20,000                                               │
│  3. Fabric Purchase - Rs 15,000                                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 11. REPORTS & ANALYTICS

### 11.1 Reports Dashboard

```
┌─────────────────────────────────────────────────────────────────────────┐
│ REPORTS & ANALYTICS                                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Date Range: [2026-03-01] to [2026-03-31]                 [Apply Filter]│
│                                                                         │
│  QUICK REPORTS                                                          │
│                                                                         │
│  SALES REPORTS                                                          │
│  ┌──────────────────────┐  ┌──────────────────────┐                    │
│  │ 📊 Sales Summary     │  │ 📈 Sales by Category │                    │
│  │                      │  │                      │                    │
│  │ [View Report]        │  │ [View Report]        │                    │
│  └──────────────────────┘  └──────────────────────┘                    │
│                                                                         │
│  ┌──────────────────────┐  ┌──────────────────────┐                    │
│  │ 🛍️ Top Products      │  │ 👤 Cashier Summary   │                    │
│  │                      │  │                      │                    │
│  │ [View Report]        │  │ [View Report]        │                    │
│  └──────────────────────┘  └──────────────────────┘                    │
│                                                                         │
│  INVENTORY REPORTS                                                      │
│  ┌──────────────────────┐  ┌──────────────────────┐                    │
│  │ 📦 Stock Valuation   │  │ ⚠️ Low Stock Items   │                    │
│  │                      │  │                      │                    │
│  │ [View Report]        │  │ [View Report]        │                    │
│  └──────────────────────┘  └──────────────────────┘                    │
│                                                                         │
│  ┌──────────────────────┐  ┌──────────────────────┐                    │
│  │ 📅 Batch Expiry      │  │ 🔄 Stock Movement    │                    │
│  │                      │  │                      │                    │
│  │ [View Report]        │  │ [View Report]        │                    │
│  └──────────────────────┘  └──────────────────────┘                    │
│                                                                         │
│  FINANCIAL REPORTS                                                      │
│  ┌──────────────────────┐  ┌──────────────────────┐                    │
│  │ 💰 Profit & Loss     │  │ 💵 Cash Flow         │                    │
│  │                      │  │                      │                    │
│  │ [View Report]        │  │ [View Report]        │                    │
│  └──────────────────────┘  └──────────────────────┘                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 11.2 Profit & Loss Report

```
┌─────────────────────────────────────────────────────────────────────────┐
│ PROFIT & LOSS STATEMENT                                  [📥 Export PDF]│
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Period: March 1, 2026 - March 31, 2026                                 │
│                                                                         │
│  REVENUE                                                                │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Total Sales                                      Rs 1,250,000   │   │
│  │ Less: Returns                                       (25,000)    │   │
│  │ ─────────────────────────────────────────────────────────────   │   │
│  │ Net Sales                                        Rs 1,225,000   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  COST OF GOODS SOLD                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Opening Stock                                    Rs   500,000   │   │
│  │ Purchases                                        Rs   800,000   │   │
│  │ Less: Closing Stock                                (550,000)    │   │
│  │ ─────────────────────────────────────────────────────────────   │   │
│  │ Cost of Goods Sold                               Rs   750,000   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  GROSS PROFIT                                        Rs   475,000   │
│  Gross Margin:                                          38.78%        │
│                                                                         │
│  OPERATING EXPENSES                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Employee Salaries                                Rs   150,000   │   │
│  │ Utilities                                        Rs    25,000   │   │
│  │ Rent                                             Rs    20,000   │   │
│  │ Transportation                                   Rs     8,000   │   │
│  │ Office Supplies                                  Rs     5,000   │   │
│  │ Other Expenses                                   Rs    17,000   │   │
│  │ ─────────────────────────────────────────────────────────────   │   │
│  │ Total Operating Expenses                         Rs   225,000   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  NET PROFIT                                          Rs   250,000   │
│  Net Margin:                                            20.41%        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 12. INVOICE/RECEIPT TEMPLATE

### 12.1 Sales Invoice (Thermal Printer - 80mm)

```
┌──────────────────────────────────────────────┐
│                                              │
│           [COMPANY LOGO/NAME]                │
│        Retail Clothes Emporium               │
│                                              │
│  Address: Shop 123, Main Market, Lahore      │
│  Phone: 042-12345678                         │
│  Email: info@retailclothes.com               │
│                                              │
│──────────────────────────────────────────────│
│                                              │
│  SALES INVOICE                               │
│                                              │
│  Invoice No:    INV-2026-00125               │
│  Date & Time:   2026-03-21 14:35             │
│  Cashier:       Sara Khan                    │
│  Customer:      Ali Khan                     │
│  Phone:         0300-1234567                 │
│                                              │
│──────────────────────────────────────────────│
│                                              │
│  ITEMS                                       │
│  ──────────────────────────────────────────  │
│                                              │
│  Cotton Shirt (M, Blue)                      │
│  2 x Rs 1,800                  Rs 3,600      │
│                                              │
│  Jeans (32, Black)                           │
│  1 x Rs 2,500                  Rs 2,500      │
│                                              │
│  Lawn Suit (3.5m, Green)                     │
│  1 x Rs 4,500                  Rs 4,500      │
│                                              │
│──────────────────────────────────────────────│
│                                              │
│  Subtotal:                     Rs 10,600     │
│  Discount (5%):                Rs   (530)    │
│  Tax (0%):                     Rs      0     │
│  ──────────────────────────────────────────  │
│  TOTAL:                        Rs 10,070     │
│                                              │
│  Payment Method:  Cash                       │
│  Received:                     Rs 11,000     │
│  Change:                       Rs    930     │
│                                              │
│──────────────────────────────────────────────│
│                                              │
│  [BARCODE: INV-2026-00125]                   │
│                                              │
│  *** Thank you for shopping! ***             │
│  Exchange policy: 7 days with receipt        │
│                                              │
│  Powered by Retail Inventory System          │
│                                              │
└──────────────────────────────────────────────┘
```

### 12.2 Invoice Specifications

**Format:**
- Paper width: 80mm (thermal printer standard)
- Font: Monospace for alignment
- Font size: 10-12pt

**Sections:**
1. Header: Logo, store name, contact info
2. Invoice details: Number, date, cashier, customer
3. Items list: Product name (variant), quantity, price, total
4. Summary: Subtotal, discount, tax, total
5. Payment: Method, received, change
6. Footer: Barcode, thank you message, policies

**Barcode:**
- Format: CODE-128
- Encode: Invoice number
- Size: 40mm width x 15mm height

**Print Options:**
- Auto-print after checkout
- Reprint option from sales list
- Email invoice (if customer has email)

---

## 13. RESPONSIVE DESIGN BREAKPOINTS

### 13.1 Breakpoints

```
Mobile:   < 768px
Tablet:   768px - 1024px
Desktop:  > 1024px
```

### 13.2 Responsive Behavior

**Dashboard:**
- Mobile: Stack cards vertically (1 column)
- Tablet: 2 columns
- Desktop: 4 columns

**POS Screen:**
- Mobile: Single column, toggle between products/cart
- Tablet: Side by side (40/60 split)
- Desktop: Side by side (40/60 split)

**Product List:**
- Mobile: Card view (1 column)
- Tablet: Grid view (2 columns)
- Desktop: Table view

**Forms:**
- Mobile: Full width inputs, stack labels on top
- Tablet: 2-column layout where appropriate
- Desktop: 2-column layout with wider inputs

---

## 14. UI COMPONENT LIBRARY (Ant Design)

### 14.1 Common Components

**Buttons:**
```tsx
// Primary action
<Button type="primary" size="large">Checkout</Button>

// Secondary action
<Button>Cancel</Button>

// Danger action
<Button danger>Delete</Button>

// Icon button
<Button icon={<PlusOutlined />}>Add Product</Button>
```

**Tables:**
```tsx
<Table
  columns={columns}
  dataSource={data}
  pagination={{ pageSize: 20 }}
  rowSelection={rowSelection}
/>
```

**Forms:**
```tsx
<Form layout="vertical">
  <Form.Item label="Product Name" required>
    <Input placeholder="Enter product name" />
  </Form.Item>

  <Form.Item label="Category">
    <Select placeholder="Select category">
      <Option value="1">Male Clothing</Option>
    </Select>
  </Form.Item>
</Form>
```

**Modals:**
```tsx
<Modal
  title="Add Expense"
  open={isOpen}
  onOk={handleOk}
  onCancel={handleCancel}
>
  {/* Modal content */}
</Modal>
```

**Cards:**
```tsx
<Card
  title="Total Sales"
  extra={<InfoCircleOutlined />}
>
  <Statistic value={250000} prefix="Rs" />
</Card>
```

---

## 15. DESIGN NOTES

### 15.1 General Guidelines

1. **Consistency:**
   - Use Ant Design components throughout
   - Maintain consistent spacing (8px grid)
   - Use design system colors

2. **Accessibility:**
   - Minimum font size: 12px
   - Contrast ratio: 4.5:1 for text
   - Keyboard navigation support
   - Screen reader friendly labels

3. **Performance:**
   - Lazy load images
   - Virtualize long lists
   - Debounce search inputs
   - Optimize re-renders

4. **User Experience:**
   - Loading states for async operations
   - Error messages clearly displayed
   - Success confirmations (toast notifications)
   - Confirmation dialogs for destructive actions

5. **Touch-Friendly (POS):**
   - Minimum button size: 44px x 44px
   - Adequate spacing between clickable elements
   - Large input fields
   - Swipe gestures where appropriate

### 15.2 Icons

Use @ant-design/icons:
- Search: SearchOutlined
- Add: PlusOutlined
- Edit: EditOutlined
- Delete: DeleteOutlined
- Info: InfoCircleOutlined
- Warning: WarningOutlined
- Success: CheckCircleOutlined
- Error: CloseCircleOutlined
- Export: DownloadOutlined
- Print: PrinterOutlined
- Scan: ScanOutlined
- User: UserOutlined
- Dashboard: DashboardOutlined
- Inventory: InboxOutlined
- Sales: ShoppingCartOutlined
- Reports: BarChartOutlined

---

**END OF UI WIREFRAMES & MOCKUPS DOCUMENT**

**Version:** 1.0
**Last Updated:** March 21, 2026
**Status:** Ready for Development

**Notes:**
- These wireframes are ASCII-based for clarity in documentation
- Development team should implement using Ant Design components
- Refer to Ant Design documentation for exact component props and styling
- UI can be adjusted based on client feedback during development
