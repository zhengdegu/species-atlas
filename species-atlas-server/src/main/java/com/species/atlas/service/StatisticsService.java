package com.species.atlas.service;

import com.species.atlas.dto.response.StatisticsResponse;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取总览统计
     *
     * @return 统计响应
     */
    StatisticsResponse getOverview();
}
