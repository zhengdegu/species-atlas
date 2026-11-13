package com.species.atlas.repository;

import com.species.atlas.entity.Zooplankton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 浮游动物数据访问接口
 */
@Repository
public interface ZooplanktonRepository extends JpaRepository<Zooplankton, Long>, JpaSpecificationExecutor<Zooplankton> {

    /**
     * 统计未删除记录数
     */
    @Query("SELECT COUNT(e) FROM Zooplankton e WHERE e.deleted = false")
    long countActive();

    /**
     * 检查物种编码是否存在
     */
    boolean existsBySpeciesCodeAndDeletedFalse(String speciesCode);
}
