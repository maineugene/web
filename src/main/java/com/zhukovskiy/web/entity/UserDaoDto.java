package com.zhukovskiy.web.entity;

public record UserDaoDto(
        int id,
        String login,
        String passwordHash,
        UserRole role) {
}
