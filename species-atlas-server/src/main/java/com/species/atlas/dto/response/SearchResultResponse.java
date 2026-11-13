package com.species.atlas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索结果响应")
public class SearchResultResponse {

    @Schema(description = "物种类型", example = "底栖动物")
    private String type;

    @Schema(description = "物种类型路径", example = "benthic-animal")
    private String typePath;

    @Schema(description = "物种信息")
    private SpeciesResponse species;
}
