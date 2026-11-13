package com.species.atlas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 鱼类实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fish", indexes = {
        @Index(name = "idx_fish_chinese_name", columnList = "chinese_name"),
        @Index(name = "idx_fish_species_code", columnList = "species_code"),
        @Index(name = "idx_fish_latin_name", columnList = "latin_name")
})
public class Fish extends BaseSpeciesEntity {
    
    // 可以添加鱼类特有的字段
}
