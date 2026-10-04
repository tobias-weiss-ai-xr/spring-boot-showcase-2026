package com.example.cropguard.repository;

import com.example.cropguard.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlotRepository extends JpaRepository<Plot, Long> {
    List<Plot> findByInsuredId(Long insuredId);
    List<Plot> findByBundesland(com.example.cropguard.domain.Bundesland bundesland);
    List<Plot> findByCropType(com.example.cropguard.domain.CropType cropType);
}
