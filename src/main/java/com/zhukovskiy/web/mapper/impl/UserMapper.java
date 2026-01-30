package com.zhukovskiy.web.mapper.impl;

import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.entity.UserRole;
import com.zhukovskiy.web.mapper.EntityMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements EntityMapper<User> {
    @Override
    public User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));
        user.setPasswordHash(resultSet.getString("passwordHash"));
        String roleStr = resultSet.getString("role");
        user.setRole(UserRole.valueOf(roleStr.toUpperCase()));

        return user;
    }
}
