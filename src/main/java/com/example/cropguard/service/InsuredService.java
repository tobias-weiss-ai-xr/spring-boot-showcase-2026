package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.dto.InsuredDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.exception.BusinessException;
import com.example.cropguard.exception.ResourceNotFoundException;
import com.example.cropguard.repository.InsuredRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class InsuredService {

    private final InsuredRepository insuredRepository;
    private final PasswordEncoder passwordEncoder;

    public InsuredService(InsuredRepository insuredRepository, PasswordEncoder passwordEncoder) {
        this.insuredRepository = insuredRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Insured register(InsuredDto dto) {
        if (insuredRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email already registered: " + dto.email());
        }
        Insured insured = new Insured(
            dto.name(),
            dto.email(),
            passwordEncoder.encode(dto.password()),
            dto.effectiveRole(),
            dto.bundesland() != null ? dto.bundesland() : Bundesland.HESSEN
        );
        return insuredRepository.save(insured);
    }

    public Insured findById(Long id) {
        return insuredRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Insured", id));
    }

    public Insured findByEmail(String email) {
        return insuredRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Insured with email " + email, 0L));
    }
}
