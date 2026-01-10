package com.zhukovskiy.web.dao;

import com.zhukovskiy.web.exception.DaoException;

public interface UserDao {
    boolean authenticate(String login, String password) throws DaoException;
}
