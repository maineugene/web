package com.zhukovskiy.web.service;

import com.zhukovskiy.web.exception.ServiceException;

public interface UserService {
    boolean authenticate(String login, String password) throws ServiceException;
}
