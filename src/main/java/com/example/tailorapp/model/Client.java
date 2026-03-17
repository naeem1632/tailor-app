package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^(?!\\s*$).+", message = "Name cannot be only whitespace")
    String name;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Mobile number must contain only numbers and valid characters (+, -, spaces, parentheses)")
    @Size(min = 10, max = 20, message = "Mobile number must be between 10 and 20 characters")
    String mobile;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "WhatsApp number must contain only numbers and valid characters (+, -, spaces, parentheses)")
    @Size(max = 20, message = "WhatsApp number must not exceed 20 characters")
    String whatsAppNo;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    String address;

    String pictureFilename;

    public Client() {
    }
}
