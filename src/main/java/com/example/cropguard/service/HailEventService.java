package com.example.cropguard.service;

import com.example.cropguard.dto.HailEventDto;
import com.example.cropguard.entity.Claim;
import com.example.cropguard.entity.HailEvent;
import com.example.cropguard.exception.ResourceNotFoundException;
import com.example.cropguard.repository.ClaimRepository;
import com.example.cropguard.repository.HailEventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class HailEventService {

    private final HailEventRepository hailEventRepository;
    private final ClaimRepository claimRepository;

    public HailEventService(HailEventRepository hailEventRepository,
                            ClaimRepository claimRepository) {
        this.hailEventRepository = hailEventRepository;
        this.claimRepository = claimRepository;
    }

    public HailEvent register(HailEventDto dto) {
        HailEvent event = new HailEvent(
            dto.eventDate(),
            dto.affectedBundeslaender(),
            dto.severity(),
            dto.description(),
            dto.hailstoneDiameterMm()
        );
        return hailEventRepository.save(event);
    }

    public HailEvent findById(Long id) {
        return hailEventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HailEvent", id));
    }

    public List<HailEvent> findByDateRange(LocalDate start, LocalDate end) {
        return hailEventRepository.findByEventDateBetween(start, end);
    }

    public List<HailEvent> findBySeverity(HailEvent.Severity severity) {
        return hailEventRepository.findBySeverity(severity);
    }

    public List<Claim> findClaimsForEvent(Long hailEventId) {
        return claimRepository.findByHailEventId(hailEventId);
    }
}
