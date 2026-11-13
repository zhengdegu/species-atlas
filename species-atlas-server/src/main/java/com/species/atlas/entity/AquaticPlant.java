package com.species.atlas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 水生植物实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "aquatic_plant", indexes = {
        @Index(name = "idx_ap_chinese_name", columnList = "chinese_name"),
        @Index(name = "idx_ap_species_code", columnList = "species_code"),
        @Index(name = "idx_ap_latin_name", columnList = "latin_name")
})
public class AquaticPlant extends BaseSpeciesEntity {
    
    // 可以添加水生植物特有的字段
}
