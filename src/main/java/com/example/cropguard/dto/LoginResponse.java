package com.example.cropguard.dto;

public record LoginResponse(
    String token,
    Long id,
    String name,
    String role
) {}
