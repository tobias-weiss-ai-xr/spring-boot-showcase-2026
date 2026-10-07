package com.example.cropguard.service;

import com.example.cropguard.dto.LoginRequest;
import com.example.cropguard.dto.LoginResponse;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.exception.UnauthorizedException;
import com.example.cropguard.repository.InsuredRepository;
import com.example.cropguard.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final InsuredRepository insuredRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(InsuredRepository insuredRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.insuredRepository = insuredRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Insured insured = insuredRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), insured.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return new LoginResponse(
            jwtService.generateToken(insured.getEmail(), insured.getRole()),
            insured.getId(), insured.getName(), insured.getRole());
    }
}
