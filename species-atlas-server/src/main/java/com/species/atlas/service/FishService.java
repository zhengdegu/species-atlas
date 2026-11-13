package com.species.atlas.service;

import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;

import java.util.List;

/**
 * 鱼类服务接口
 */
public interface FishService {

    PageResponse<SpeciesResponse> list(SpeciesQueryRequest request);

    SpeciesResponse getById(Long id);

    SpeciesResponse create(SpeciesCreateRequest request);

    SpeciesResponse update(Long id, SpeciesUpdateRequest request);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
