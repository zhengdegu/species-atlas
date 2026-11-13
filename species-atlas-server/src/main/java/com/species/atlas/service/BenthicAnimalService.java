package com.species.atlas.service;

import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;

import java.util.List;

/**
 * 底栖动物服务接口
 */
public interface BenthicAnimalService {

    /**
     * 分页查询
     */
    PageResponse<SpeciesResponse> list(SpeciesQueryRequest request);

    /**
     * 获取详情
     */
    SpeciesResponse getById(Long id);

    /**
     * 创建
     */
    SpeciesResponse create(SpeciesCreateRequest request);

    /**
     * 更新
     */
    SpeciesResponse update(Long id, SpeciesUpdateRequest request);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 批量删除
     */
    void batchDelete(List<Long> ids);
}
