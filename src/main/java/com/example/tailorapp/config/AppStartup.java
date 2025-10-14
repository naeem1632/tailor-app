package com.example.tailorapp.config;

import com.example.tailorapp.service.StorageProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@EnableConfigurationProperties(StorageProperties.class)
public class AppStartup implements ApplicationRunner {

    private final StorageProperties props;

    public AppStartup(StorageProperties props) {
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String uploadDir = props.getUploadDir() == null ? "uploads" : props.getUploadDir();
        Path p = Paths.get(uploadDir);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
        }
    }
}
