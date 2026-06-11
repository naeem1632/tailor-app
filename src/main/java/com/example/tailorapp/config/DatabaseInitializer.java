package com.example.tailorapp.config;

import com.example.tailorapp.model.ExpenseHistory;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.repository.ExpenseHistoryRepository;
import com.example.tailorapp.repository.PaymentsRepository;
import com.example.tailorapp.service.ExpenseService;
import com.example.tailorapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final ExpenseService expenseService;
    private final ExpenseHistoryRepository expenseHistoryRepository;
    private final PaymentsRepository paymentsRepository;
    private final UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(ExpenseService expenseService,
                              ExpenseHistoryRepository expenseHistoryRepository,
                              PaymentsRepository paymentsRepository,
                              UserService userService) {
        this.expenseService = expenseService;
        this.expenseHistoryRepository = expenseHistoryRepository;
        this.paymentsRepository = paymentsRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Run database migrations first
        runDatabaseMigrations();

        // Then run data initialization
        initializeExpenseHistory();
        initializeOrderStatuses();
        initializeDefaultAdminUser();
    }

    private void runDatabaseMigrations() {
        System.out.println("Running database migrations...");

        try {
            migrateEmployeePaymentTable();
            dropLegacyColumnsFromEmployeePayment();
            migrateWorkAssignmentTable();
            createEmployeePaymentTransactionTable();
            System.out.println("✓ Database migrations completed successfully!");
        } catch (Exception e) {
            System.err.println("✗ Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void migrateEmployeePaymentTable() {
        try {
            System.out.println("  - Migrating EmployeePayment table...");

            // Check and add each column individually
            if (!columnExists("employee_payment", "total_amount")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE employee_payment ADD COLUMN total_amount INTEGER DEFAULT 0");
                    System.out.println("    Added total_amount column");
                } catch (Exception e) {
                    System.err.println("    Error adding total_amount: " + e.getMessage());
                }
            } else {
                System.out.println("    total_amount column already exists");
            }

            if (!columnExists("employee_payment", "amount_paid")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE employee_payment ADD COLUMN amount_paid INTEGER DEFAULT 0");
                    System.out.println("    Added amount_paid column");
                } catch (Exception e) {
                    System.err.println("    Error adding amount_paid: " + e.getMessage());
                }
            } else {
                System.out.println("    amount_paid column already exists");
            }

            if (!columnExists("employee_payment", "payment_status")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE employee_payment ADD COLUMN payment_status VARCHAR(255) DEFAULT 'PENDING'");
                    System.out.println("    Added payment_status column");
                } catch (Exception e) {
                    System.err.println("    Error adding payment_status: " + e.getMessage());
                }
            } else {
                System.out.println("    payment_status column already exists");
            }

            // Migrate existing data - only if legacy 'amount' column still exists
            if (columnExists("employee_payment", "amount")) {
                try {
                    jdbcTemplate.execute(
                        "UPDATE employee_payment SET total_amount = COALESCE(amount, 0), " +
                        "amount_paid = COALESCE(amount, 0), payment_status = 'PAID' " +
                        "WHERE amount IS NOT NULL AND (total_amount IS NULL OR total_amount = 0)"
                    );
                    System.out.println("    Migrated existing payment data from legacy 'amount' column");
                } catch (Exception e) {
                    System.err.println("    Error migrating data: " + e.getMessage());
                }
            } else {
                System.out.println("    Legacy 'amount' column not found, skipping data migration");
            }

            // Update NULL values
            try {
                jdbcTemplate.execute("UPDATE employee_payment SET total_amount = 0 WHERE total_amount IS NULL");
                jdbcTemplate.execute("UPDATE employee_payment SET amount_paid = 0 WHERE amount_paid IS NULL");
                jdbcTemplate.execute("UPDATE employee_payment SET payment_status = 'PENDING' WHERE payment_status IS NULL OR payment_status = ''");
                System.out.println("    Updated NULL values");
            } catch (Exception e) {
                System.err.println("    Error updating NULLs: " + e.getMessage());
            }

            System.out.println("  ✓ EmployeePayment table migrated successfully");
        } catch (Exception e) {
            System.err.println("  ✗ Error migrating EmployeePayment table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            jdbcTemplate.queryForObject(
                "SELECT " + columnName + " FROM " + tableName + " LIMIT 1",
                Object.class
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Drop legacy columns (amount, payment_date, payment_type) from employee_payment table
     * SQLite requires recreating the table to drop columns
     */
    private void dropLegacyColumnsFromEmployeePayment() {
        try {
            // Check if legacy columns still exist
            if (!columnExists("employee_payment", "amount")) {
                System.out.println("  - Legacy columns already removed from EmployeePayment table");
                return;
            }

            System.out.println("  - Removing legacy columns from EmployeePayment table...");

            // SQLite doesn't support DROP COLUMN directly, so we recreate the table
            // Step 1: Create new table with only new schema
            jdbcTemplate.execute(
                "CREATE TABLE employee_payment_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employee_id INTEGER NOT NULL, " +
                "work_period_start DATE NOT NULL, " +
                "work_period_end DATE NOT NULL, " +
                "total_amount INTEGER, " +
                "amount_paid INTEGER, " +
                "payment_status VARCHAR(255), " +
                "notes VARCHAR(500), " +
                "created_at TIMESTAMP, " +
                "FOREIGN KEY (employee_id) REFERENCES employee(id)" +
                ")"
            );

            // Step 2: Copy data from old table to new table
            jdbcTemplate.execute(
                "INSERT INTO employee_payment_new " +
                "(id, employee_id, work_period_start, work_period_end, total_amount, amount_paid, payment_status, notes, created_at) " +
                "SELECT id, employee_id, work_period_start, work_period_end, total_amount, amount_paid, payment_status, notes, created_at " +
                "FROM employee_payment"
            );

            // Step 3: Drop old table
            jdbcTemplate.execute("DROP TABLE employee_payment");

            // Step 4: Rename new table to original name
            jdbcTemplate.execute("ALTER TABLE employee_payment_new RENAME TO employee_payment");

            System.out.println("  ✓ Legacy columns removed successfully");
        } catch (Exception e) {
            System.err.println("  ✗ Error removing legacy columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void migrateWorkAssignmentTable() {
        System.out.println("  - Migrating WorkAssignment table...");

        // Check and add started_date column
        try {
            jdbcTemplate.execute("ALTER TABLE work_assignment ADD COLUMN started_date DATE");
            System.out.println("    Added started_date column");
        } catch (Exception e) {
            if (e.getMessage().contains("duplicate column")) {
                System.out.println("    started_date column already exists");
            } else {
                System.out.println("    Error adding started_date: " + e.getMessage());
            }
        }

        // Check and add started_time column
        try {
            jdbcTemplate.execute("ALTER TABLE work_assignment ADD COLUMN started_time TIME");
            System.out.println("    Added started_time column");
        } catch (Exception e) {
            if (e.getMessage().contains("duplicate column")) {
                System.out.println("    started_time column already exists");
            } else {
                System.out.println("    Error adding started_time: " + e.getMessage());
            }
        }

        // Check and add completed_time column
        try {
            jdbcTemplate.execute("ALTER TABLE work_assignment ADD COLUMN completed_time TIME");
            System.out.println("    Added completed_time column");
        } catch (Exception e) {
            if (e.getMessage().contains("duplicate column")) {
                System.out.println("    completed_time column already exists");
            } else {
                System.out.println("    Error adding completed_time: " + e.getMessage());
            }
        }

        System.out.println("  ✓ WorkAssignment table migrated successfully");
    }

    private void createEmployeePaymentTransactionTable() {
        try {
            // Check if table exists
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employee_payment_transaction",
                Integer.class
            );
            System.out.println("  - EmployeePaymentTransaction table already exists");
        } catch (Exception e) {
            System.out.println("  - Creating EmployeePaymentTransaction table...");

            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS employee_payment_transaction (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employee_payment_id INTEGER NOT NULL, " +
                "payment_date DATE NOT NULL, " +
                "payment_time TIME, " +
                "amount_paid INTEGER NOT NULL, " +
                "payment_method VARCHAR(255) NOT NULL, " +
                "notes VARCHAR(500), " +
                "created_at TIMESTAMP, " +
                "FOREIGN KEY (employee_payment_id) REFERENCES employee_payment(id)" +
                ")"
            );

            System.out.println("  ✓ EmployeePaymentTransaction table created successfully");
        }
    }

    /**
     * Initialize expense history with default record dated February 1, 2026
     * This ensures all existing orders use the current expense rates
     */
    private void initializeExpenseHistory() {
        // Check if expense history is empty
        List<ExpenseHistory> existingHistory = expenseHistoryRepository.findAll();

        if (existingHistory.isEmpty()) {
            // Create default expense history record with Feb 1, 2026 as effective date
            // Using default values of 0 for all expenses
            ExpenseHistory defaultHistory = new ExpenseHistory();
            defaultHistory.setEffectiveDate(LocalDate.of(2026, 2, 1));
            defaultHistory.setDressExpense(0L);
            defaultHistory.setWaistcoatExpense(0L);
            defaultHistory.setShirtExpense(0L);
            defaultHistory.setMatelExpense(0L);
            defaultHistory.setTichExpense(0L);
            defaultHistory.setKantaExpense(0L);
            defaultHistory.setJaliExpense(0L);
            defaultHistory.setKrhaiExpense(0L);
            defaultHistory.setNotes("Initial expense rates (auto-created for existing data compatibility)");

            expenseHistoryRepository.save(defaultHistory);

            System.out.println("✓ Expense history initialized with default record dated Feb 1, 2026");
        } else {
            System.out.println("✓ Expense history already exists, skipping initialization");
        }
    }

    /**
     * Set default orderStatus for existing payments that don't have one
     */
    private void initializeOrderStatuses() {
        List<Payments> allPayments = paymentsRepository.findAll();

        int updatedCount = 0;
        for (Payments payment : allPayments) {
            if (payment.getOrderStatus() == null || payment.getOrderStatus().isEmpty()) {
                // Set default status based on readyStatus
                if ("PICKED_UP".equals(payment.getReadyStatus())) {
                    payment.setOrderStatus("COMPLETED");
                } else {
                    payment.setOrderStatus("ACTIVE");
                }
                paymentsRepository.save(payment);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            System.out.println("✓ Set default orderStatus for " + updatedCount + " existing payments");
        } else {
            System.out.println("✓ All payments already have orderStatus set");
        }
    }

    /**
     * Create default admin user if no users exist
     * Default credentials: admin / admin123
     */
    private void initializeDefaultAdminUser() {
        try {
            // Check if any users exist
            if (userService.findAll().isEmpty()) {
                System.out.println("  - Creating default admin user...");

                userService.createAdmin(
                    "admin",
                    "admin123",
                    "admin@stitchandstyle.com",
                    "Administrator"
                );

                System.out.println("✓ Default admin user created successfully!");
                System.out.println("  Username: admin");
                System.out.println("  Password: admin123");
                System.out.println("  IMPORTANT: Please change the default password after first login!");
            } else {
                System.out.println("✓ Users already exist, skipping default admin creation");
            }
        } catch (Exception e) {
            System.err.println("✗ Error creating default admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
