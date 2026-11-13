package com.species.atlas.enums;

import lombok.Getter;

/**
 * 物种类型枚举
 */
@Getter
public enum SpeciesType {
    
    BENTHIC_ANIMAL("benthic-animal", "底栖动物", "benthic_animal"),
    AQUATIC_PLANT("aquatic-plant", "水生植物", "aquatic_plant"),
    PHYTOPLANKTON("phytoplankton", "浮游植物", "phytoplankton"),
    FISH("fish", "鱼类", "fish"),
    ZOOPLANKTON("zooplankton", "浮游动物", "zooplankton");

    /**
     * URL路径标识
     */
    private final String path;
    
    /**
     * 中文名称
     */
    private final String chineseName;
    
    /**
     * 数据库表名
     */
    private final String tableName;

    SpeciesType(String path, String chineseName, String tableName) {
        this.path = path;
        this.chineseName = chineseName;
        this.tableName = tableName;
    }

    /**
     * 根据路径获取枚举
     */
    public static SpeciesType fromPath(String path) {
        for (SpeciesType type : values()) {
            if (type.getPath().equals(path)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown species type path: " + path);
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static SpeciesType fromChineseName(String chineseName) {
        for (SpeciesType type : values()) {
            if (type.getChineseName().equals(chineseName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown species type: " + chineseName);
    }
}
