package com.example.tailorapp.service;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for automatic database backup
 * - Runs every 8 days
 * - Keeps only the last 4 backups
 * - Stores backups in D:/tailor-app/backups/
 */
@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    private static final String BACKUP_DIR = "D:/tailor-app/backups";
    private static final int MAX_BACKUPS = 4;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

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

        // Create backup directory if it doesn't exist
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create backup directory: " + BACKUP_DIR);
            }
            logger.info("Created backup directory: {}", BACKUP_DIR);
        }

        // Create backup filename with timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String backupFileName = "tailor_backup_" + timestamp + ".db";
        Path backupPath = Paths.get(BACKUP_DIR, backupFileName);

        // Copy database file to backup location
        Files.copy(dbFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Database backup created: {}", backupPath);

        // Clean up old backups
        cleanupOldBackups();
    }

    /**
     * Delete old backups, keeping only the last MAX_BACKUPS files
     */
    private void cleanupOldBackups() {
        File backupDir = new File(BACKUP_DIR);
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("tailor_backup_") && name.endsWith(".db"));

        if (backupFiles == null || backupFiles.length <= MAX_BACKUPS) {
            logger.info("No old backups to clean up. Current count: {}", backupFiles != null ? backupFiles.length : 0);
            return;
        }

        // Sort by last modified date (oldest first)
        List<File> sortedBackups = Arrays.stream(backupFiles)
                .sorted(Comparator.comparingLong(File::lastModified))
                .collect(Collectors.toList());

        // Delete oldest backups, keeping only MAX_BACKUPS
        int toDelete = sortedBackups.size() - MAX_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            File oldBackup = sortedBackups.get(i);
            if (oldBackup.delete()) {
                logger.info("Deleted old backup: {}", oldBackup.getName());
            } else {
                logger.warn("Failed to delete old backup: {}", oldBackup.getName());
            }
        }

        logger.info("Backup cleanup completed. Remaining backups: {}", MAX_BACKUPS);
    }

    /**
     * Get list of existing backups
     */
    public List<String> listBackups() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            return List.of();
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("tailor_backup_") && name.endsWith(".db"));
        if (backupFiles == null) {
            return List.of();
        }

        return Arrays.stream(backupFiles)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(File::getName)
                .collect(Collectors.toList());
    }
}