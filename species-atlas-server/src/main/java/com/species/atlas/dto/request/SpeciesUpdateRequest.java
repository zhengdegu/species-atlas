package com.species.atlas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新物种请求 DTO
 */
@Data
@Schema(description = "更新物种请求")
public class SpeciesUpdateRequest {

    @Schema(description = "序号", example = "001")
    private String serialNumber;

    @Size(max = 100, message = "物种编码长度不能超过100个字符")
    @Schema(description = "物种编码", example = "BENTHOS_001")
    private String speciesCode;

    @Size(max = 200, message = "中文名长度不能超过200个字符")
    @Schema(description = "中文名", example = "摇蚊幼虫")
    private String chineseName;

    @Size(max = 200, message = "拉丁名长度不能超过200个字符")
    @Schema(description = "拉丁名", example = "Chironomidae")
    private String latinName;

    @Schema(description = "照片编码", example = "PHOTO_001")
    private String photoCode;

    @Schema(description = "照片路径列表")
    private List<String> photoPaths;

    @Size(max = 500, message = "分类地位长度不能超过500个字符")
    @Schema(description = "分类地位", example = "节肢动物门 > 昆虫纲 > 双翅目")
    private String taxonomicStatus;

    @Schema(description = "基因名称", example = "COI")
    private String geneName;

    @Schema(description = "DNA序列编码", example = "SEQ_001")
    private String dnaSequenceCode;

    @Schema(description = "DNA编码属性")
    private String dnaCodingProperty;

    @Schema(description = "形态描述", example = "体长5-15mm，体色淡黄...")
    private String morphologicalDescription;

    @Schema(description = "参考文献")
    private String reference;

    @Schema(description = "地理分布", example = "全国各地淡水水体")
    private String geographicalDistribution;
}
