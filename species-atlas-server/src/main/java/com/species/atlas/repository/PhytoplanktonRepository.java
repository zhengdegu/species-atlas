package com.species.atlas.repository;

import com.species.atlas.entity.Phytoplankton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 浮游植物数据访问接口
 */
@Repository
public interface PhytoplanktonRepository extends JpaRepository<Phytoplankton, Long>, JpaSpecificationExecutor<Phytoplankton> {

    /**
     * 统计未删除记录数
     */
    @Query("SELECT COUNT(e) FROM Phytoplankton e WHERE e.deleted = false")
    long countActive();

    /**
     * 检查物种编码是否存在
     */
    boolean existsBySpeciesCodeAndDeletedFalse(String speciesCode);
}
