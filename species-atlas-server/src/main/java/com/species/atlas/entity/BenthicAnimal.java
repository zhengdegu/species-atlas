package com.species.atlas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 底栖动物实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "benthic_animal", indexes = {
        @Index(name = "idx_ba_chinese_name", columnList = "chinese_name"),
        @Index(name = "idx_ba_species_code", columnList = "species_code"),
        @Index(name = "idx_ba_latin_name", columnList = "latin_name")
})
public class BenthicAnimal extends BaseSpeciesEntity {
    
    // 可以添加底栖动物特有的字段
}
