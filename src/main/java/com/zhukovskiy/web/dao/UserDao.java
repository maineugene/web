package com.zhukovskiy.web.dao;

import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.exception.DaoException;

public interface UserDao extends BaseDao<User> {
    //boolean authenticate(String login, String password) throws DaoException;
}