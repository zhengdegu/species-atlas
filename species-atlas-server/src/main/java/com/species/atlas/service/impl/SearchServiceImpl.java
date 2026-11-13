package com.species.atlas.service.impl;

import com.species.atlas.converter.SpeciesConverter;
import com.species.atlas.dto.response.SearchResultResponse;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.entity.*;
import com.species.atlas.enums.SpeciesType;
import com.species.atlas.repository.*;
import com.species.atlas.service.SearchService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final BenthicAnimalRepository benthicAnimalRepository;
    private final AquaticPlantRepository aquaticPlantRepository;
    private final PhytoplanktonRepository phytoplanktonRepository;
    private final FishRepository fishRepository;
    private final ZooplanktonRepository zooplanktonRepository;
    private final SpeciesConverter converter;

    @Override
    public List<SearchResultResponse> searchSpecies(String keyword, int pageSize) {
        List<SearchResultResponse> results = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, pageSize);

        // 搜索底栖动物
        results.addAll(searchBenthicAnimal(keyword, pageRequest));

        // 搜索水生植物
        results.addAll(searchAquaticPlant(keyword, pageRequest));

        // 搜索浮游植物
        results.addAll(searchPhytoplankton(keyword, pageRequest));

        // 搜索鱼类
        results.addAll(searchFish(keyword, pageRequest));

        // 搜索浮游动物
        results.addAll(searchZooplankton(keyword, pageRequest));

        log.info("搜索关键词: {}, 结果数量: {}", keyword, results.size());
        return results;
    }

    private List<SearchResultResponse> searchBenthicAnimal(String keyword, PageRequest pageRequest) {
        Specification<BenthicAnimal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.like(root.get("chineseName"), "%" + keyword + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return benthicAnimalRepository.findAll(spec, pageRequest).getContent().stream()
                .map(entity -> buildSearchResult(entity, SpeciesType.BENTHIC_ANIMAL))
                .collect(Collectors.toList());
    }

    private List<SearchResultResponse> searchAquaticPlant(String keyword, PageRequest pageRequest) {
        Specification<AquaticPlant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.like(root.get("chineseName"), "%" + keyword + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return aquaticPlantRepository.findAll(spec, pageRequest).getContent().stream()
                .map(entity -> buildSearchResult(entity, SpeciesType.AQUATIC_PLANT))
                .collect(Collectors.toList());
    }

    private List<SearchResultResponse> searchPhytoplankton(String keyword, PageRequest pageRequest) {
        Specification<Phytoplankton> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.like(root.get("chineseName"), "%" + keyword + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return phytoplanktonRepository.findAll(spec, pageRequest).getContent().stream()
                .map(entity -> buildSearchResult(entity, SpeciesType.PHYTOPLANKTON))
                .collect(Collectors.toList());
    }

    private List<SearchResultResponse> searchFish(String keyword, PageRequest pageRequest) {
        Specification<Fish> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.like(root.get("chineseName"), "%" + keyword + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return fishRepository.findAll(spec, pageRequest).getContent().stream()
                .map(entity -> buildSearchResult(entity, SpeciesType.FISH))
                .collect(Collectors.toList());
    }

    private List<SearchResultResponse> searchZooplankton(String keyword, PageRequest pageRequest) {
        Specification<Zooplankton> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.like(root.get("chineseName"), "%" + keyword + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return zooplanktonRepository.findAll(spec, pageRequest).getContent().stream()
                .map(entity -> buildSearchResult(entity, SpeciesType.ZOOPLANKTON))
                .collect(Collectors.toList());
    }

    private SearchResultResponse buildSearchResult(BaseSpeciesEntity entity, SpeciesType type) {
        SpeciesResponse speciesResponse = converter.toResponse(entity);
        return SearchResultResponse.builder()
                .type(type.getChineseName())
                .typePath(type.getPath())
                .species(speciesResponse)
                .build();
    }
}
