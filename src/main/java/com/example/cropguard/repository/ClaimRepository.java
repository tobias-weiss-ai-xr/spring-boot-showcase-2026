package com.example.cropguard.repository;

import com.example.cropguard.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyPlotInsuredId(Long insuredId);
    List<Claim> findByStatus(Claim.Status status);
    List<Claim> findByHailEventId(Long hailEventId);
}
