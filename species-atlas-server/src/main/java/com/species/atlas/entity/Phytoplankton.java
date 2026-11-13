package com.species.atlas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 浮游植物实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "phytoplankton", indexes = {
        @Index(name = "idx_pp_chinese_name", columnList = "chinese_name"),
        @Index(name = "idx_pp_species_code", columnList = "species_code"),
        @Index(name = "idx_pp_latin_name", columnList = "latin_name")
})
public class Phytoplankton extends BaseSpeciesEntity {
    
    // 可以添加浮游植物特有的字段
}
