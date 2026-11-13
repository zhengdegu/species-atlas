package com.species.atlas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物种信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物种信息响应")
public class SpeciesResponse {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "序号", example = "001")
    private String serialNumber;

    @Schema(description = "物种编码", example = "BENTHOS_001")
    private String speciesCode;

    @Schema(description = "中文名", example = "摇蚊幼虫")
    private String chineseName;

    @Schema(description = "拉丁名", example = "Chironomidae")
    private String latinName;

    @Schema(description = "照片编码", example = "PHOTO_001")
    private String photoCode;

    @Schema(description = "照片路径列表")
    private List<String> photoPaths;

    @Schema(description = "分类地位", example = "节肢动物门 > 昆虫纲 > 双翅目")
    private String taxonomicStatus;

    @Schema(description = "基因名称", example = "COI")
    private String geneName;

    @Schema(description = "DNA序列编码", example = "SEQ_001")
    private String dnaSequenceCode;

    @Schema(description = "DNA编码属性")
    private String dnaCodingProperty;

    @Schema(description = "形态描述")
    private String morphologicalDescription;

    @Schema(description = "参考文献")
    private String reference;

    @Schema(description = "地理分布")
    private String geographicalDistribution;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
