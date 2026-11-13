package com.species.atlas.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 物种对象转换器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpeciesConverter {

    private final ObjectMapper objectMapper;

    /**
     * 通用实体转响应 DTO
     */
    public SpeciesResponse toResponse(BaseSpeciesEntity entity) {
        if (entity == null) {
            return null;
        }

        return SpeciesResponse.builder()
                .id(entity.getId())
                .serialNumber(entity.getSerialNumber())
                .speciesCode(entity.getSpeciesCode())
                .chineseName(entity.getChineseName())
                .latinName(entity.getLatinName())
                .photoCode(entity.getPhotoCode())
                .photoPaths(parsePhotoPaths(entity.getPhotoPaths()))
                .taxonomicStatus(entity.getTaxonomicStatus())
                .geneName(entity.getGeneName())
                .dnaSequenceCode(entity.getDnaSequenceCode())
                .dnaCodingProperty(entity.getDnaCodingProperty())
                .morphologicalDescription(entity.getMorphologicalDescription())
                .reference(entity.getReference())
                .geographicalDistribution(entity.getGeographicalDistribution())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 创建请求转底栖动物实体
     */
    public BenthicAnimal toBenthicAnimal(SpeciesCreateRequest request) {
        BenthicAnimal entity = new BenthicAnimal();
        copyFromCreateRequest(entity, request);
        return entity;
    }

    /**
     * 创建请求转水生植物实体
     */
    public AquaticPlant toAquaticPlant(SpeciesCreateRequest request) {
        AquaticPlant entity = new AquaticPlant();
        copyFromCreateRequest(entity, request);
        return entity;
    }

    /**
     * 创建请求转浮游植物实体
     */
    public Phytoplankton toPhytoplankton(SpeciesCreateRequest request) {
        Phytoplankton entity = new Phytoplankton();
        copyFromCreateRequest(entity, request);
        return entity;
    }

    /**
     * 创建请求转鱼类实体
     */
    public Fish toFish(SpeciesCreateRequest request) {
        Fish entity = new Fish();
        copyFromCreateRequest(entity, request);
        return entity;
    }

    /**
     * 创建请求转浮游动物实体
     */
    public Zooplankton toZooplankton(SpeciesCreateRequest request) {
        Zooplankton entity = new Zooplankton();
        copyFromCreateRequest(entity, request);
        return entity;
    }

    /**
     * 更新请求更新实体
     */
    public void updateEntity(BaseSpeciesEntity entity, SpeciesUpdateRequest request) {
        if (request.getSerialNumber() != null) {
            entity.setSerialNumber(request.getSerialNumber());
        }
        if (request.getSpeciesCode() != null) {
            entity.setSpeciesCode(request.getSpeciesCode());
        }
        if (request.getChineseName() != null) {
            entity.setChineseName(request.getChineseName());
        }
        if (request.getLatinName() != null) {
            entity.setLatinName(request.getLatinName());
        }
        if (request.getPhotoCode() != null) {
            entity.setPhotoCode(request.getPhotoCode());
        }
        if (request.getPhotoPaths() != null) {
            entity.setPhotoPaths(toJson(request.getPhotoPaths()));
        }
        if (request.getTaxonomicStatus() != null) {
            entity.setTaxonomicStatus(request.getTaxonomicStatus());
        }
        if (request.getGeneName() != null) {
            entity.setGeneName(request.getGeneName());
        }
        if (request.getDnaSequenceCode() != null) {
            entity.setDnaSequenceCode(request.getDnaSequenceCode());
        }
        if (request.getDnaCodingProperty() != null) {
            entity.setDnaCodingProperty(request.getDnaCodingProperty());
        }
        if (request.getMorphologicalDescription() != null) {
            entity.setMorphologicalDescription(request.getMorphologicalDescription());
        }
        if (request.getReference() != null) {
            entity.setReference(request.getReference());
        }
        if (request.getGeographicalDistribution() != null) {
            entity.setGeographicalDistribution(request.getGeographicalDistribution());
        }
    }

    /**
     * 复制创建请求到实体
     */
    private void copyFromCreateRequest(BaseSpeciesEntity entity, SpeciesCreateRequest request) {
        entity.setSerialNumber(request.getSerialNumber());
        entity.setSpeciesCode(request.getSpeciesCode());
        entity.setChineseName(request.getChineseName());
        entity.setLatinName(request.getLatinName());
        entity.setPhotoCode(request.getPhotoCode());
        entity.setPhotoPaths(toJson(request.getPhotoPaths()));
        entity.setTaxonomicStatus(request.getTaxonomicStatus());
        entity.setGeneName(request.getGeneName());
        entity.setDnaSequenceCode(request.getDnaSequenceCode());
        entity.setDnaCodingProperty(request.getDnaCodingProperty());
        entity.setMorphologicalDescription(request.getMorphologicalDescription());
        entity.setReference(request.getReference());
        entity.setGeographicalDistribution(request.getGeographicalDistribution());
    }

    /**
     * 解析照片路径 JSON
     */
    private List<String> parsePhotoPaths(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析照片路径失败: {}", json);
            return Collections.emptyList();
        }
    }

    /**
     * 列表转 JSON
     */
    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("序列化照片路径失败");
            return null;
        }
    }
}
