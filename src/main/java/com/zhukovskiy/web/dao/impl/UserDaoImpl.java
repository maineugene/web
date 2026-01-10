package com.zhukovskiy.web.dao.impl;

import com.zhukovskiy.web.dao.BaseDao;
import com.zhukovskiy.web.dao.UserDao;
import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.exception.DaoException;
import com.zhukovskiy.web.pool.ConnectionPool;

import java.sql.*;
import java.util.List;

public class UserDaoImpl extends BaseDao<User> implements UserDao {
    private static final String SELECT_PASSWORD_FROM_USERS_WHERE_LOGIN = "SELECT password FROM users WHERE login = ?";
    private static UserDaoImpl instance = new UserDaoImpl();

    private UserDaoImpl() {
    }

    public static UserDaoImpl getInstance() {
        return instance;
    }

    @Override
    public boolean insert(User user) {
        return false;
    }

    @Override
    public boolean delete(User user) {
        return false;
    }

    @Override
    public List<User> findAll(User user) {
        return List.of();
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public boolean authenticate(String login, String password) throws DaoException {

        boolean match = false;
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_PASSWORD_FROM_USERS_WHERE_LOGIN)) {
            statement.setString(1, login);
            statement.execute();
            ResultSet resultSet = statement.executeQuery();
            String passFromDb;
            if(resultSet.next()){
                passFromDb = resultSet.getString(1);
                match = password.equals(passFromDb);
            }
        } catch (SQLException e) {
            throw new DaoException("SQL error: " + e.getMessage(), e);
        }

        return match;
    }
}
