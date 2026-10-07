package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.dto.PlotDto;
import com.example.cropguard.entity.Insured;
import com.example.cropguard.entity.Plot;
import com.example.cropguard.exception.ResourceNotFoundException;
import com.example.cropguard.repository.PlotRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlotService {

    private final PlotRepository plotRepository;
    private final InsuredService insuredService;

    public PlotService(PlotRepository plotRepository, InsuredService insuredService) {
        this.plotRepository = plotRepository;
        this.insuredService = insuredService;
    }

    public Plot register(PlotDto dto, Long insuredId) {
        Insured insured = insuredService.findById(insuredId);
        Plot plot = new Plot(
            dto.cropType(),
            dto.hectares(),
            dto.bundesland(),
            dto.coordinateE(),
            dto.coordinateN(),
            dto.locationDescription(),
            insured
        );
        return plotRepository.save(plot);
    }

    public List<Plot> findByInsuredId(Long insuredId) {
        return plotRepository.findByInsuredId(insuredId);
    }

    public List<Plot> findByBundesland(Bundesland bundesland) {
        return plotRepository.findByBundesland(bundesland);
    }

    public List<Plot> findAll() {
        return plotRepository.findAll();
    }

    public List<Plot> findByCropType(CropType cropType) {
        return plotRepository.findByCropType(cropType);
    }

    public Plot findById(Long id) {
        return plotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plot", id));
    }
}
