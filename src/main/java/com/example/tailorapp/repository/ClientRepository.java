package com.example.tailorapp.repository;

import com.example.tailorapp.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNameContainingIgnoreCaseOrMobileContaining(String name, String mobile);
}
