package com.species.atlas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 物种查询请求 DTO
 */
@Data
@Schema(description = "物种查询请求")
public class SpeciesQueryRequest {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数", example = "20", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "物种编码", example = "BENTHOS_001")
    private String speciesCode;

    @Schema(description = "中文名（模糊搜索）", example = "摇蚊")
    private String chineseName;

    @Schema(description = "拉丁名（模糊搜索）", example = "Chiro")
    private String latinName;

    @Schema(description = "基因名称", example = "COI")
    private String geneName;
}
