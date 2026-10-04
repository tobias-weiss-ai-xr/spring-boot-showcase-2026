package com.example.cropguard;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import com.example.cropguard.entity.*;
import com.example.cropguard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final InsuredRepository insuredRepo;
    private final PlotRepository plotRepo;
    private final PolicyRepository policyRepo;
    private final ClaimRepository claimRepo;
    private final HailEventRepository hailEventRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(InsuredRepository insuredRepo, PlotRepository plotRepo,
                           PolicyRepository policyRepo, ClaimRepository claimRepo,
                           HailEventRepository hailEventRepo, PasswordEncoder passwordEncoder) {
        this.insuredRepo = insuredRepo;
        this.plotRepo = plotRepo;
        this.policyRepo = policyRepo;
        this.claimRepo = claimRepo;
        this.hailEventRepo = hailEventRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Insured farmer = insuredRepo.save(new Insured(
            "Max Mustermann", "max@bauernhof.de",
            passwordEncoder.encode("passwort123"), "FARMER", Bundesland.HESSEN));

        Insured assessor = insuredRepo.save(new Insured(
            "Lisa Gutachter", "lisa@cropguard.de",
            passwordEncoder.encode("assessor123"), "ASSESSOR", Bundesland.BAYERN));

        Plot plot = plotRepo.save(new Plot(
            CropType.WHEAT, 25.0, Bundesland.HESSEN,
            3456000.0, 5564000.0,
            "Feld am Rande von Kassel, Nordhessen", farmer));

        Policy policy = policyRepo.save(new Policy(
            25000.0, 1125.0, Deductible.TEN_PERCENT,
            Policy.Status.ACTIVE,
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 31),
            plot));

        HailEvent event = hailEventRepo.save(new HailEvent(
            LocalDate.of(2026, 7, 15),
            "HESSEN,NORDRHEIN_WESTFALEN",
            HailEvent.Severity.SEVERE,
            "Schweres Hagelereignis mit 4cm grossen Korn, betroffen sind Hessen und NRW.",
            40));

        Claim claim = new Claim(
            LocalDate.of(2026, 7, 15),
            "Vollstaendiger Ernteverlust auf 25 Hektar Weizen durch Hagel",
            Claim.Status.SUBMITTED, policy);
        claim.setHailEventId(event.getId());
        claimRepo.save(claim);
    }
}
