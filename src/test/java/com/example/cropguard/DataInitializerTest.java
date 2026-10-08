package com.example.cropguard;

import com.example.cropguard.repository.ClaimRepository;
import com.example.cropguard.repository.HailEventRepository;
import com.example.cropguard.repository.InsuredRepository;
import com.example.cropguard.repository.PlotRepository;
import com.example.cropguard.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock InsuredRepository insuredRepo;
    @Mock PlotRepository plotRepo;
    @Mock PolicyRepository policyRepo;
    @Mock ClaimRepository claimRepo;
    @Mock HailEventRepository hailEventRepo;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DataInitializer initializer;

    @Test
    void run_onEmptyDatabase_seedsDemoData() {
        when(insuredRepo.count()).thenReturn(0L);
        when(insuredRepo.save(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(inv -> inv.getArgument(0));
        when(plotRepo.save(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(inv -> inv.getArgument(0));
        when(policyRepo.save(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(inv -> inv.getArgument(0));
        when(hailEventRepo.save(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(inv -> inv.getArgument(0));
        when(claimRepo.save(org.mockito.ArgumentMatchers.any()))
            .thenAnswer(inv -> inv.getArgument(0));

        initializer.run();

        verify(insuredRepo, org.mockito.Mockito.times(2)).save(org.mockito.ArgumentMatchers.any()); // farmer + assessor
        verify(plotRepo).save(org.mockito.ArgumentMatchers.any());
        verify(claimRepo).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void run_onPersistentDatabaseWithData_skipsSeeding() {
        when(insuredRepo.count()).thenReturn(3L);

        initializer.run();

        verify(insuredRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(plotRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(policyRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(claimRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(hailEventRepo, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
