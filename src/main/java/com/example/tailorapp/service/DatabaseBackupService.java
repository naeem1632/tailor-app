package com.example.tailorapp.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for automatic database backup
 * - Runs on application startup
 * - Runs every 8 days (scheduled)
 * - Removes backups older than 5 days
 * - Stores backups in D:/tailor-app/db-backup/
 * - Filename format: YYYY-MM-DD_HHMMSS.db (e.g., 2026-03-08_125731.db)
 */
@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    private static final int MAX_BACKUP_AGE_DAYS = 5;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;





    /**
     * Run backup on application startup
     */
    @PostConstruct
    public void performStartupBackup() {
        logger.info("Starting database backup on application startup...");
        try {
            createBackup();
            logger.info("Startup backup completed successfully");
        } catch (Exception e) {
            logger.error("Startup backup failed", e);
        }
    }

    @Scheduled(cron = "0 0 2 */8 * ?")
    public void performScheduledBackup() {
        logger.info("Starting scheduled database backup...");
        try {
            createBackup();
            logger.info("Scheduled backup completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled backup failed", e);
        }
    }

    /**
     * Create a backup of the database
     */
    public void createBackup() throws IOException {
        // Extract database file path from datasource URL
        // Format: jdbc:sqlite:D:/tailor-app/data/tailor.db
        String dbPath = datasourceUrl.replace("jdbc:sqlite:", "");

        // Remove ${TAILOR_DB_PATH:...} if present (use default)
        if (dbPath.contains("${")) {
            dbPath = "D:/tailor-app/data/tailor.db";
        }

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            logger.error("Database file not found: {}", dbPath);
            throw new IOException("Database file not found: " + dbPath);
        }

        // Use static path on D drive: D:/tailor-app/db-backup/
        String backupDirPath = "D:/tailor-app/db-backup";
        File backupDir = new File(backupDirPath);

        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create backup directory: " + backupDirPath);
            }
            logger.info("Created backup directory: {}", backupDirPath);
        }

        // Create backup filename with date and time format: YYYY-MM-DD_HHMMSS.db
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String backupFileName = timestamp + ".db";
        Path backupPath = Paths.get(backupDirPath, backupFileName);

        // Copy database file to backup location
        Files.copy(dbFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);

        // Set the file's last modified time to current time to ensure accurate timestamp
        File backupFile = backupPath.toFile();
        backupFile.setLastModified(System.currentTimeMillis());

        logger.info("Database backup created: {}", backupPath);

        // Clean up old backups (older than 5 days)
        cleanupOldBackups(backupDir);
    }

    /**
     * Delete backups older than MAX_BACKUP_AGE_DAYS (5 days)
     */
    private void cleanupOldBackups(File backupDir) {
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".db"));

        if (backupFiles == null || backupFiles.length == 0) {
            logger.info("No backups found for cleanup");
            return;
        }

        // Calculate cutoff time: current time minus 5 days
        long currentTimeMillis = System.currentTimeMillis();
        long cutoffTimeMillis = currentTimeMillis - (MAX_BACKUP_AGE_DAYS * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;

        logger.info("Cleanup check: Current time={}, Cutoff time={}, Max age={} days",
                    currentTimeMillis, cutoffTimeMillis, MAX_BACKUP_AGE_DAYS);

        for (File backupFile : backupFiles) {
            try {
                // Get file's last modified time in milliseconds
                long lastModifiedMillis = backupFile.lastModified();
                long ageInMillis = currentTimeMillis - lastModifiedMillis;
                long ageInDays = ageInMillis / (24L * 60L * 60L * 1000L);

                logger.info("Checking backup: {} | LastModified={} | Age={} days",
                           backupFile.getName(), lastModifiedMillis, ageInDays);

                // Delete if older than 5 days
                if (lastModifiedMillis < cutoffTimeMillis && lastModifiedMillis > 0) {
                    if (backupFile.delete()) {
                        logger.info("Deleted old backup (older than {} days): {}", MAX_BACKUP_AGE_DAYS, backupFile.getName());
                        deletedCount++;
                    } else {
                        logger.warn("Failed to delete old backup: {}", backupFile.getName());
                    }
                } else if (lastModifiedMillis == 0) {
                    logger.warn("Backup file has invalid timestamp (0): {}", backupFile.getName());
                } else {
                    logger.info("Keeping backup (age: {} days): {}", ageInDays, backupFile.getName());
                }
            } catch (Exception e) {
                logger.error("Error processing backup file: {}", backupFile.getName(), e);
            }
        }

        if (deletedCount > 0) {
            logger.info("Backup cleanup completed. Deleted {} old backups. Remaining: {}",
                        deletedCount, backupFiles.length - deletedCount);
        } else {
            logger.info("Backup cleanup completed. No old backups to delete. Total backups: {}", backupFiles.length);
        }
    }

    /**
     * Get list of existing backups
     */
    public List<String> listBackups() {
        String backupDirPath = "D:/tailor-app/db-backup";
        File backupDir = new File(backupDirPath);

        if (!backupDir.exists()) {
            return List.of();
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".db"));
        if (backupFiles == null) {
            return List.of();
        }

        return Arrays.stream(backupFiles)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(File::getName)
                .collect(Collectors.toList());
    }
}