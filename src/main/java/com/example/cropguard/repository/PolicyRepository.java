package com.example.cropguard.repository;

import com.example.cropguard.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByPlotInsuredId(Long insuredId);
    List<Policy> findByStatus(Policy.Status status);
}
