package com.species.atlas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 浮游动物实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "zooplankton", indexes = {
        @Index(name = "idx_zp_chinese_name", columnList = "chinese_name"),
        @Index(name = "idx_zp_species_code", columnList = "species_code"),
        @Index(name = "idx_zp_latin_name", columnList = "latin_name")
})
public class Zooplankton extends BaseSpeciesEntity {
    
    // 可以添加浮游动物特有的字段
}
