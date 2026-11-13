package com.species.atlas.service;

import com.species.atlas.dto.response.SearchResultResponse;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 跨类别搜索物种
     *
     * @param keyword  关键词
     * @param pageSize 每类返回数量
     * @return 搜索结果列表
     */
    List<SearchResultResponse> searchSpecies(String keyword, int pageSize);
}
