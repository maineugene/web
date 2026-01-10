package com.zhukovskiy.web.service.impl;

import com.zhukovskiy.web.dao.impl.UserDaoImpl;
import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.exception.DaoException;
import com.zhukovskiy.web.exception.ServiceException;
import com.zhukovskiy.web.service.UserService;

public class UserServiceImpl implements UserService {
    private static UserServiceImpl instance = new UserServiceImpl();

    private UserServiceImpl() {
    }

    public static UserServiceImpl getInstance() {
        return instance;
    }

    @Override
    public boolean authenticate(String login, String password) throws ServiceException {
        // validate login, pass + md5
        UserDaoImpl userDao = UserDaoImpl.getInstance();
        boolean match = false;
        try {
            match = userDao.authenticate(login, password);
        } catch (DaoException e) {
            throw new ServiceException(e);
        }
        return match;
    }

}
