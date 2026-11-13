package com.species.atlas.service.impl;

import com.species.atlas.converter.SpeciesConverter;
import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.entity.BenthicAnimal;
import com.species.atlas.exception.BusinessException;
import com.species.atlas.exception.ErrorCode;
import com.species.atlas.exception.NotFoundException;
import com.species.atlas.repository.BenthicAnimalRepository;
import com.species.atlas.service.BenthicAnimalService;
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

/**
 * 底栖动物服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenthicAnimalServiceImpl implements BenthicAnimalService {

    private final BenthicAnimalRepository repository;
    private final SpeciesConverter converter;

    @Override
    public PageResponse<SpeciesResponse> list(SpeciesQueryRequest request) {
        // 构建查询条件
        Specification<BenthicAnimal> spec = buildSpecification(request);

        // 分页查询
        PageRequest pageRequest = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<BenthicAnimal> page = repository.findAll(spec, pageRequest);

        // 转换响应
        List<SpeciesResponse> rows = page.getContent().stream()
                .map(converter::toResponse)
                .toList();

        return PageResponse.of(rows, page.getTotalElements(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public SpeciesResponse getById(Long id) {
        BenthicAnimal entity = findById(id);
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public SpeciesResponse create(SpeciesCreateRequest request) {
        // 检查编码是否已存在
        if (repository.existsBySpeciesCodeAndDeletedFalse(request.getSpeciesCode())) {
            throw new BusinessException(ErrorCode.SPECIES_CODE_EXISTS, "物种编码已存在: " + request.getSpeciesCode());
        }

        BenthicAnimal entity = converter.toBenthicAnimal(request);
        entity = repository.save(entity);

        log.info("创建底栖动物: id={}, name={}", entity.getId(), entity.getChineseName());
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public SpeciesResponse update(Long id, SpeciesUpdateRequest request) {
        BenthicAnimal entity = findById(id);

        // 如果修改了编码，检查是否重复
        if (request.getSpeciesCode() != null 
                && !request.getSpeciesCode().equals(entity.getSpeciesCode())
                && repository.existsBySpeciesCodeAndDeletedFalse(request.getSpeciesCode())) {
            throw new BusinessException(ErrorCode.SPECIES_CODE_EXISTS, "物种编码已存在: " + request.getSpeciesCode());
        }

        converter.updateEntity(entity, request);
        entity = repository.save(entity);

        log.info("更新底栖动物: id={}", id);
        return converter.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BenthicAnimal entity = findById(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("删除底栖动物: id={}", id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<BenthicAnimal> entities = repository.findAllById(ids);
        entities.forEach(e -> e.setDeleted(true));
        repository.saveAll(entities);
        log.info("批量删除底栖动物: ids={}", ids);
    }

    /**
     * 根据 ID 查找实体
     */
    private BenthicAnimal findById(Long id) {
        return repository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new NotFoundException("底栖动物", id));
    }

    /**
     * 构建查询条件
     */
    private Specification<BenthicAnimal> buildSpecification(SpeciesQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 排除已删除
            predicates.add(cb.equal(root.get("deleted"), false));

            // 按中文名模糊搜索
            if (StringUtils.hasText(request.getChineseName())) {
                predicates.add(cb.like(root.get("chineseName"), "%" + request.getChineseName() + "%"));
            }

            // 按拉丁名模糊搜索
            if (StringUtils.hasText(request.getLatinName())) {
                predicates.add(cb.like(root.get("latinName"), "%" + request.getLatinName() + "%"));
            }

            // 按物种编码精确搜索
            if (StringUtils.hasText(request.getSpeciesCode())) {
                predicates.add(cb.equal(root.get("speciesCode"), request.getSpeciesCode()));
            }

            // 按基因名称搜索
            if (StringUtils.hasText(request.getGeneName())) {
                predicates.add(cb.equal(root.get("geneName"), request.getGeneName()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
