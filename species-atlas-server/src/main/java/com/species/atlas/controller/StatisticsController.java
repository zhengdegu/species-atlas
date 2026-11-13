package com.species.atlas.controller;

import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.dto.response.StatisticsResponse;
import com.species.atlas.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "数据统计", description = "物种数据统计接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "获取总览统计", description = "获取各类别物种数量统计")
    public ApiResponse<StatisticsResponse> getOverview() {
        StatisticsResponse response = statisticsService.getOverview();
        return ApiResponse.success(response);
    }
}
