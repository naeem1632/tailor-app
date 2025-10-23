package com.example.tailorapp.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@ConfigurationProperties(prefix = "tailor.upload")
@Getter
public class StorageProperties {

    /** Path for client profile pictures */
    private String clientPath = "D:/tailor-app/client-profiles";

}
