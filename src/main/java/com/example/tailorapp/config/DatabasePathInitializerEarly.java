package com.example.tailorapp.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;

public class DatabasePathInitializerEarly implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        String datasourceUrl = env.getProperty("spring.datasource.url");

        try {
            if (datasourceUrl != null && datasourceUrl.startsWith("jdbc:sqlite:")) {
                String dbPath = datasourceUrl.substring("jdbc:sqlite:".length());
                File dbFile = new File(dbPath);
                File parentDir = dbFile.getParentFile();

                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        System.out.println("✅ Created database folder early: " + parentDir.getAbsolutePath());
                    } else {
                        System.out.println("⚠️ Could not create folder: " + parentDir.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating database folder: " + e.getMessage());
        }
    }
}
