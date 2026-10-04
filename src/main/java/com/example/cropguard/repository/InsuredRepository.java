package com.example.cropguard.repository;

import com.example.cropguard.entity.Insured;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InsuredRepository extends JpaRepository<Insured, Long> {
    Optional<Insured> findByEmail(String email);
    boolean existsByEmail(String email);
}
