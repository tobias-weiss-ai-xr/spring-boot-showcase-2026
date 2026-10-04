package com.example.cropguard.entity;

import com.example.cropguard.domain.Bundesland;
import jakarta.persistence.*;

/** A farmer or agricultural business buying hail insurance. Module 5: JPA entity. */
@Entity
@Table(name = "insureds")
public class Insured {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** FARMER files claims; ASSESSOR reviews them. Module 8: role-based access. */
    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Bundesland bundesland;

    protected Insured() {}

    public Insured(String name, String email, String passwordHash, String role, Bundesland bundesland) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.bundesland = bundesland;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public Bundesland getBundesland() { return bundesland; }
}
