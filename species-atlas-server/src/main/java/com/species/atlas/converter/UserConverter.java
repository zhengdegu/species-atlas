package com.species.atlas.converter;

import com.species.atlas.dto.response.UserResponse;
import com.species.atlas.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用户对象转换器
 */
@Component
public class UserConverter {

    /**
     * 实体转响应 DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .role(user.getRole())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
