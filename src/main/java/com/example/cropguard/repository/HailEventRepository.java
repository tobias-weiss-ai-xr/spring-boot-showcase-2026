package com.example.cropguard.repository;

import com.example.cropguard.entity.HailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HailEventRepository extends JpaRepository<HailEvent, Long> {
    List<HailEvent> findByEventDateBetween(java.time.LocalDate start, java.time.LocalDate end);
    List<HailEvent> findBySeverity(HailEvent.Severity severity);
}
