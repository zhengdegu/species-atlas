package com.species.atlas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 物种基础实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class BaseSpeciesEntity extends BaseEntity {

    /**
     * 序号
     */
    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    /**
     * 物种编码
     */
    @Column(name = "species_code", nullable = false, length = 100)
    private String speciesCode;

    /**
     * 中文名
     */
    @Column(name = "chinese_name", nullable = false, length = 200)
    private String chineseName;

    /**
     * 拉丁名
     */
    @Column(name = "latin_name", length = 200)
    private String latinName;

    /**
     * 照片编码
     */
    @Column(name = "photo_code", length = 100)
    private String photoCode;

    /**
     * 照片路径（JSON数组）
     */
    @Column(name = "photo_paths", columnDefinition = "JSON")
    private String photoPaths;

    /**
     * 分类地位
     */
    @Column(name = "taxonomic_status", length = 500)
    private String taxonomicStatus;

    /**
     * 基因名称
     */
    @Column(name = "gene_name", length = 200)
    private String geneName;

    /**
     * DNA序列编码
     */
    @Column(name = "dna_sequence_code", length = 100)
    private String dnaSequenceCode;

    /**
     * DNA编码属性
     */
    @Column(name = "dna_coding_property", columnDefinition = "TEXT")
    private String dnaCodingProperty;

    /**
     * 形态描述
     */
    @Column(name = "morphological_description", columnDefinition = "TEXT")
    private String morphologicalDescription;

    /**
     * 参考文献
     */
    @Column(name = "reference", columnDefinition = "TEXT")
    private String reference;

    /**
     * 地理分布
     */
    @Column(name = "geographical_distribution", columnDefinition = "TEXT")
    private String geographicalDistribution;
}
