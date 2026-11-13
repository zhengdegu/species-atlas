package com.species.atlas.service.impl;

import com.species.atlas.dto.response.StatisticsResponse;
import com.species.atlas.repository.*;
import com.species.atlas.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BenthicAnimalRepository benthicAnimalRepository;
    private final AquaticPlantRepository aquaticPlantRepository;
    private final PhytoplanktonRepository phytoplanktonRepository;
    private final FishRepository fishRepository;
    private final ZooplanktonRepository zooplanktonRepository;

    @Override
    public StatisticsResponse getOverview() {
        Map<String, Long> byType = new LinkedHashMap<>();

        long benthicCount = benthicAnimalRepository.countActive();
        long aquaticCount = aquaticPlantRepository.countActive();
        long phytoplanktonCount = phytoplanktonRepository.countActive();
        long fishCount = fishRepository.countActive();
        long zooplanktonCount = zooplanktonRepository.countActive();

        byType.put("benthicAnimal", benthicCount);
        byType.put("aquaticPlant", aquaticCount);
        byType.put("phytoplankton", phytoplanktonCount);
        byType.put("fish", fishCount);
        byType.put("zooplankton", zooplanktonCount);

        long total = benthicCount + aquaticCount + phytoplanktonCount + fishCount + zooplanktonCount;

        return StatisticsResponse.builder()
                .total(total)
                .byType(byType)
                .build();
    }
}
