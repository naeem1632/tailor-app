# Implementation Plan: Employee Management & Daily Expense Modules

## Overview
This document outlines the implementation plan for adding two new modules to the Tailor Shop Management Application:
1. **Employee Management Module**
2. **Daily Expense Management Module**

---

## Current Architecture

The application follows a clean Spring Boot MVC architecture:
- **Model Layer**: JPA entities with Lombok annotations
- **Repository Layer**: Spring Data JPA repositories
- **Service Layer**: Business logic and data operations
- **Controller Layer**: MVC controllers handling HTTP requests
- **View Layer**: Thymeleaf templates with Bootstrap UI

---

## Module 1: Employee Management

### Features

#### 1.1 Employee CRUD Operations
- Add new employees
- Edit employee information
- Delete/Archive employees
- View employee details
- Employee photo upload (similar to Client module)
- Active/Inactive status tracking

#### 1.2 Salary Management
- Monthly salary configuration
- Salary payment tracking
- Payment history with dates and payment types
- Advance payment tracking
- Outstanding balance calculation
- Multiple payment methods (Cash, Bank Transfer, JazzCash, EasyPaisa)

#### 1.3 Future Enhancements (Optional)
- Daily attendance tracking
- Leave management
- Performance reviews
- Commission tracking for tailors

---

### Database Schema

#### Employee Table
```java
@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Pattern(regexp = "^[0-9]{5}-[0-9]{7}-[0-9]$", message = "CNIC format: 12345-1234567-1")
    private String cnic;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$")
    @Size(min = 10, max = 20)
    private String mobile;

    @Size(max = 200)
    private String address;

    private LocalDate joiningDate;

    private Long monthlySalary; // Base monthly salary

    private String designation; // Tailor, Helper, Manager, Accountant, etc.

    private String pictureFilename; // Employee photo

    private Boolean isActive; // Track current vs past employees

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeSalaryPayment> salaryPayments = new ArrayList<>();

    // Calculated fields (not persisted)
    @Transient
    private Long totalPaid;

    @Transient
    private Long totalAdvance;

    @Transient
    private Long remainingBalance;
}
```

#### EmployeeSalaryPayment Table
```java
@Entity
@Data
public class EmployeeSalaryPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate paymentDate;

    private Long amount;

    private String paymentType; // Cash, Bank Transfer, JazzCash, EasyPaisa

    private String month; // Format: "2025-03" for March 2025

    private String paymentCategory; // Salary, Advance, Bonus, Deduction

    private String note; // Optional notes

    @PrePersist
    private void setDefaults() {
        if (this.paymentType == null || this.paymentType.isEmpty()) {
            this.paymentType = "Cash";
        }
        if (this.paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }
    }
}
```

---

### API Endpoints

#### Employee Management
- `GET /employees` - List all employees
- `GET /employees/add` - Show add employee form
- `POST /employees/add` - Create new employee
- `GET /employees/edit/{id}` - Show edit employee form
- `POST /employees/edit/{id}` - Update employee
- `POST /employees/delete/{id}` - Delete/Archive employee
- `GET /employees/view/{id}` - View employee details with salary history

#### Salary Payment Management
- `POST /employees/salary/add` - Add salary payment
- `POST /employees/salary/edit` - Edit salary payment
- `POST /employees/salary/delete/{id}` - Delete salary payment

---

### Views to Create

1. **employees/list.html**
   - Table with employee list
   - Columns: ID, Photo, Name, Designation, Mobile, Monthly Salary, Status, Actions
   - Filter by status (Active/Inactive)
   - Search by name
   - Add new employee button

2. **employees/add.html**
   - Form with all employee fields
   - Photo upload
   - Validation messages

3. **employees/edit.html**
   - Pre-filled form with employee data
   - Photo preview and update

4. **employees/view.html**
   - Employee details section
   - Salary payment history table
   - Add payment modal/form
   - Monthly salary summary
   - Outstanding balance display

---

## Module 2: Daily Expense Management

### Features

#### 2.1 Expense Categories
- Pre-defined categories (Utilities, Rent, Materials, Transportation, etc.)
- Add custom categories
- Active/Inactive category management
- Category-wise expense tracking

#### 2.2 Daily Expense Tracking
- Date-wise expense entries
- Category selection
- Amount tracking
- Payment method selection
- Vendor/payee name
- Description/notes
- Optional receipt upload

#### 2.3 Expense Reports
- Daily expense summary
- Weekly/Monthly summaries
- Category-wise breakdown
- Date range filtering
- Payment method breakdown
- Export to Excel/PDF (future enhancement)

---

### Database Schema

#### ExpenseCategory Table
```java
@Entity
@Data
public class ExpenseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50)
    private String name; // Utilities, Rent, Materials, Transportation, etc.

    @Size(max = 200)
    private String description;

    private Boolean isActive; // Allow deactivating categories

    private Integer displayOrder; // For sorting in dropdowns

    public ExpenseCategory() {
        this.isActive = true;
    }
}
```

#### DailyExpense Table
```java
@Entity
@Data
public class DailyExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate expenseDate;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    private Long amount;

    private String paymentMethod; // Cash, Bank Transfer, JazzCash, EasyPaisa

    @Size(max = 500)
    private String description;

    private String receiptFilename; // Optional: store receipt image/PDF

    @Size(max = 100)
    private String vendorName; // Optional: who was paid

    @PrePersist
    private void setDefaults() {
        if (this.expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
        if (this.paymentMethod == null || this.paymentMethod.isEmpty()) {
            this.paymentMethod = "Cash";
        }
    }
}
```

---

### API Endpoints

#### Daily Expense Management
- `GET /daily-expenses` - List all expenses with filters
- `GET /daily-expenses/add` - Show add expense form
- `POST /daily-expenses/add` - Create new expense
- `GET /daily-expenses/edit/{id}` - Show edit expense form
- `POST /daily-expenses/edit/{id}` - Update expense
- `POST /daily-expenses/delete/{id}` - Delete expense
- `GET /daily-expenses/report` - Expense report with date range

#### Category Management
- `GET /daily-expenses/categories` - List all categories
- `POST /daily-expenses/categories/add` - Add new category
- `POST /daily-expenses/categories/edit/{id}` - Update category
- `POST /daily-expenses/categories/toggle/{id}` - Toggle active status

---

### Views to Create

1. **daily-expenses/list.html**
   - Table with expense list
   - Columns: Date, Category, Amount, Payment Method, Vendor, Description, Actions
   - Filters: Date range, Category, Payment method
   - Add new expense button
   - Quick summary cards (Today, This Week, This Month totals)

2. **daily-expenses/add.html**
   - Form with all expense fields
   - Category dropdown
   - Payment method dropdown
   - Date picker (default: today)
   - Optional receipt upload

3. **daily-expenses/edit.html**
   - Pre-filled form with expense data
   - Receipt preview and update

4. **daily-expenses/report.html**
   - Date range selector
   - Category-wise breakdown (pie chart/table)
   - Payment method breakdown
   - Daily expense trend (line chart - future)
   - Total expense summary
   - Export options (future)

5. **daily-expenses/categories.html**
   - List of all categories
   - Add/Edit/Toggle status
   - Reorder categories (drag-drop - future)

---

## Integration Points

### 1. Navigation Menu Update
**File**: `src/main/resources/templates/fragments/navbar.html`

Add after Expenses menu item:
```html
<li class="nav-item">
    <a class="nav-link" href="/employees">
        <i class="fas fa-user-tie"></i> Employees
    </a>
</li>
<li class="nav-item">
    <a class="nav-link" href="/daily-expenses">
        <i class="fas fa-receipt"></i> Daily Expenses
    </a>
</li>
```

### 2. Dashboard Updates
**File**: `src/main/java/com/example/tailorapp/controller/HomeController.java`
**File**: `src/main/resources/templates/index.html`

Add new dashboard cards:
- **Active Employees**: Count of active employees
- **Monthly Salary Expense**: Total monthly salary for all active employees
- **Today's Expenses**: Sum of daily expenses for today
- **This Month's Expenses**: Sum of daily expenses for current month

### 3. Profit Analysis Integration
**File**: `src/main/java/com/example/tailorapp/controller/ProfitAnalysisController.java`

Add new calculations:
```java
// Employee salary expense for the period
long employeeSalaryExpense = calculateEmployeeSalaryExpense(startDate, endDate);

// Daily expenses for the period
long dailyExpenses = calculateDailyExpenses(startDate, endDate);

// Updated profit calculation
long totalProfit = totalRevenue - totalProductionExpense - employeeSalaryExpense - dailyExpenses;
```

Update `profit-analysis/report.html` to show:
- Employee salary breakdown
- Daily expense breakdown by category
- Adjusted profit calculations

### 4. Reports Dashboard
**File**: `src/main/resources/templates/reports/dashboard.html`

Add new report options:
- Employee Salary Report
- Daily Expense Report
- Comprehensive Profit & Loss Statement

---

## File Structure

```
src/main/java/com/example/tailorapp/
├── model/
│   ├── Employee.java
│   ├── EmployeeSalaryPayment.java
│   ├── ExpenseCategory.java
│   └── DailyExpense.java
│
├── repository/
│   ├── EmployeeRepository.java
│   ├── EmployeeSalaryPaymentRepository.java
│   ├── ExpenseCategoryRepository.java
│   └── DailyExpenseRepository.java
│
├── service/
│   ├── EmployeeService.java
│   ├── EmployeeSalaryPaymentService.java
│   ├── ExpenseCategoryService.java
│   └── DailyExpenseService.java
│
└── controller/
    ├── EmployeeController.java
    └── DailyExpenseController.java

src/main/resources/templates/
├── employees/
│   ├── list.html
│   ├── add.html
│   ├── edit.html
│   └── view.html
│
└── daily-expenses/
    ├── list.html
    ├── add.html
    ├── edit.html
    ├── report.html
    └── categories.html
```

---

## Implementation Timeline

### Phase 1: Employee Management Module (1-2 Weeks)

**Week 1:**
1. Create Employee and EmployeeSalaryPayment models
2. Create repositories
3. Create EmployeeService with business logic
4. Create EmployeeController with CRUD operations
5. Create employee list and add/edit views

**Week 2:**
1. Implement salary payment tracking
2. Create employee view page with salary history
3. Add employee photo upload functionality
4. Create employee reports
5. Testing and bug fixes

### Phase 2: Daily Expense Module (1-2 Weeks)

**Week 1:**
1. Create ExpenseCategory and DailyExpense models
2. Create repositories
3. Create services
4. Create DailyExpenseController
5. Seed initial expense categories

**Week 2:**
1. Create expense list and add/edit views
2. Implement category management
3. Create expense report with date filtering
4. Add receipt upload functionality (optional)
5. Testing and bug fixes

### Phase 3: Integration & Enhancement (1 Week)

1. Update navigation menu
2. Update dashboard with new widgets
3. Integrate employee salary and daily expenses into Profit Analysis
4. Create comprehensive P&L report
5. End-to-end testing
6. Documentation updates
7. User training/demo

---

## Technical Considerations

### 1. Database Migration
- Use Flyway or Liquibase for schema versioning (optional)
- Ensure backward compatibility
- Create seed data for expense categories

### 2. Photo/Receipt Storage
- Reuse existing storage mechanism for Client photos
- Store in configured upload directory
- Implement file validation (size, type)

### 3. Payment Method Consistency
- Use same payment types across all modules
- Consider creating a shared enum or constant class

### 4. Date Range Queries
- Optimize queries with proper indexing
- Use LocalDate for consistency
- Handle timezone considerations

### 5. Authorization & Security
- All modules accessible after report password authentication
- Consider role-based access in future (Admin, Manager, Viewer)

### 6. Performance
- Implement pagination for employee/expense lists
- Lazy loading for related entities
- Caching for frequently accessed data (categories, active employees)

---

## Default Expense Categories

When implementing, seed these categories:

1. **Utilities** - Electricity, water, gas, internet bills
2. **Rent** - Shop/workspace rent
3. **Raw Materials** - Thread, buttons, zippers, fabric, etc.
4. **Transportation** - Fuel, vehicle maintenance, delivery costs
5. **Maintenance** - Equipment repairs, shop maintenance
6. **Marketing** - Advertising, promotional materials
7. **Office Supplies** - Stationery, printing, etc.
8. **Miscellaneous** - Other expenses
9. **Professional Services** - Legal, accounting, consulting
10. **Equipment** - Purchase of new machines/tools

---

## UI/UX Guidelines

1. **Consistency**: Follow existing Bootstrap theme and color scheme
2. **Responsive**: Ensure all views work on mobile devices
3. **Validation**: Client-side and server-side validation
4. **Feedback**: Success/error messages using flash attributes
5. **Icons**: Use Font Awesome icons consistently
6. **Modals**: Use modals for quick actions (add payment, add expense)
7. **Tables**: Sortable, searchable, with pagination

---

## Testing Checklist

### Employee Module
- [ ] Add new employee with all fields
- [ ] Edit employee information
- [ ] Upload/update employee photo
- [ ] Add salary payment
- [ ] Add advance payment
- [ ] Calculate outstanding balance correctly
- [ ] Filter active/inactive employees
- [ ] Search employees by name
- [ ] Delete employee (archive)

### Daily Expense Module
- [ ] Add new expense category
- [ ] Add daily expense
- [ ] Edit expense
- [ ] Delete expense
- [ ] Filter by date range
- [ ] Filter by category
- [ ] Filter by payment method
- [ ] Calculate daily/weekly/monthly totals
- [ ] Upload receipt (if implemented)
- [ ] Toggle category active/inactive

### Integration
- [ ] Navigation menu shows new items
- [ ] Dashboard shows employee and expense widgets
- [ ] Profit analysis includes salary and daily expenses
- [ ] Reports authenticate properly
- [ ] Date range filtering works across all reports

---

## Future Enhancements (Phase 4+)

1. **Employee Attendance System**
   - Daily check-in/check-out
   - Monthly attendance reports
   - Leave tracking

2. **Employee Commission Tracking**
   - Commission rates per employee
   - Order-based commission calculation
   - Commission payment tracking

3. **Expense Analytics**
   - Category-wise trends (charts)
   - Budget vs actual tracking
   - Expense forecasting

4. **Advanced Reporting**
   - Comprehensive P&L statement
   - Cash flow statement
   - Balance sheet
   - Export to Excel/PDF

5. **Notifications**
   - Salary payment reminders
   - Employee birthday reminders
   - Expense budget alerts

6. **Multi-user Support**
   - Role-based access control
   - User activity logs
   - Manager approval workflows

---

## Notes

- This plan follows the existing application architecture and patterns
- All code should follow the same style and conventions as current codebase
- Use Lombok annotations (@Data, @Entity, etc.) consistently
- Follow Spring Boot best practices
- Ensure proper exception handling and validation
- Write clear comments for complex business logic

---

## Questions to Consider

Before starting implementation:

1. Should employees be soft-deleted or hard-deleted?
2. Should we track employee work history (different roles/salary changes)?
3. Do we need to track specific employee performance metrics?
4. Should daily expenses support multiple attachments or just one?
5. Do we need approval workflow for expenses above certain amount?
6. Should the system support multiple currencies (currently using Long for PKR)?

---

**Document Version**: 1.0
**Created**: 2025
**Last Updated**: 2025
**Status**: Planning Phase