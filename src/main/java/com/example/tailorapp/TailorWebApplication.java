package com.example.tailorapp;

import com.example.tailorapp.config.DatabasePathInitializerEarly;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TailorWebApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TailorWebApplication.class);
        app.addInitializers(new DatabasePathInitializerEarly());
        app.run(args);
    }
}
