package com.zhukovskiy.web.service;

import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.entity.UserRole;
import com.zhukovskiy.web.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> authenticate(String login, String password) throws ServiceException;
    User register(String login, String password) throws ServiceException;
    void changeUserRole(int userId, UserRole newRole) throws ServiceException;
    List<User> findAllUsers() throws ServiceException;
    Optional<User> findByLogin(String login) throws ServiceException;
}
