package com.species.atlas.service.impl;

import com.species.atlas.converter.SpeciesConverter;
import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.entity.Fish;
import com.species.atlas.exception.BusinessException;
import com.species.atlas.exception.ErrorCode;
import com.species.atlas.exception.NotFoundException;
import com.species.atlas.repository.FishRepository;
import com.species.atlas.service.FishService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FishServiceImpl implements FishService {

    private final FishRepository repository;
    private final SpeciesConverter converter;

    @Override
    public PageResponse<SpeciesResponse> list(SpeciesQueryRequest request) {
        Specification<Fish> spec = buildSpecification(request);
        PageRequest pageRequest = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<Fish> page = repository.findAll(spec, pageRequest);
        List<SpeciesResponse> rows = page.getContent().stream()
                .map(converter::toResponse)
                .toList();

        return PageResponse.of(rows, page.getTotalElements(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public SpeciesResponse getById(Long id) {
        Fish entity = findById(id);
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public SpeciesResponse create(SpeciesCreateRequest request) {
        if (repository.existsBySpeciesCodeAndDeletedFalse(request.getSpeciesCode())) {
            throw new BusinessException(ErrorCode.SPECIES_CODE_EXISTS, "物种编码已存在: " + request.getSpeciesCode());
        }

        Fish entity = converter.toFish(request);
        entity = repository.save(entity);

        log.info("创建鱼类: id={}, name={}", entity.getId(), entity.getChineseName());
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public SpeciesResponse update(Long id, SpeciesUpdateRequest request) {
        Fish entity = findById(id);

        if (request.getSpeciesCode() != null
                && !request.getSpeciesCode().equals(entity.getSpeciesCode())
                && repository.existsBySpeciesCodeAndDeletedFalse(request.getSpeciesCode())) {
            throw new BusinessException(ErrorCode.SPECIES_CODE_EXISTS, "物种编码已存在: " + request.getSpeciesCode());
        }

        converter.updateEntity(entity, request);
        entity = repository.save(entity);

        log.info("更新鱼类: id={}", id);
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Fish entity = findById(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("删除鱼类: id={}", id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<Fish> entities = repository.findAllById(ids);
        entities.forEach(e -> e.setDeleted(true));
        repository.saveAll(entities);
        log.info("批量删除鱼类: ids={}", ids);
    }

    private Fish findById(Long id) {
        return repository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("鱼类", id));
    }

    private Specification<Fish> buildSpecification(SpeciesQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (StringUtils.hasText(request.getChineseName())) {
                predicates.add(cb.like(root.get("chineseName"), "%" + request.getChineseName() + "%"));
            }
            if (StringUtils.hasText(request.getLatinName())) {
                predicates.add(cb.like(root.get("latinName"), "%" + request.getLatinName() + "%"));
            }
            if (StringUtils.hasText(request.getSpeciesCode())) {
                predicates.add(cb.equal(root.get("speciesCode"), request.getSpeciesCode()));
            }
            if (StringUtils.hasText(request.getGeneName())) {
                predicates.add(cb.equal(root.get("geneName"), request.getGeneName()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
