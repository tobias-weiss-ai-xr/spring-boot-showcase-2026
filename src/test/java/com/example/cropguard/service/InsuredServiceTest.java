package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.dto.InsuredDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.repository.InsuredRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsuredServiceTest {

    @Mock InsuredRepository insuredRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks InsuredService insuredService;

    @Test
    void register_ignoresRequestedRole_forcesFarmer() {
        when(insuredRepository.existsByEmail("eve@evil.de")).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("hash");
        when(insuredRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InsuredDto dto = new InsuredDto(null, "Eve", "eve@evil.de", "geheim123",
            "ASSESSOR", Bundesland.HESSEN);

        Insured result = insuredService.register(dto);
        assertEquals("FARMER", result.getRole());
    }
}
