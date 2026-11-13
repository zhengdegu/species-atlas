package com.species.atlas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统计响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计响应")
public class StatisticsResponse {

    @Schema(description = "物种总数", example = "1580")
    private Long total;

    @Schema(description = "各类别数量统计")
    private Map<String, Long> byType;
}
