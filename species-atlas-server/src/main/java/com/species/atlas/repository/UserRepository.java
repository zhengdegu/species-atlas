package com.species.atlas.repository;

import com.species.atlas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查询（未删除）
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);

    /**
     * 根据邮箱查询（未删除）
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsernameAndDeletedFalse(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmailAndDeletedFalse(String email);
}
