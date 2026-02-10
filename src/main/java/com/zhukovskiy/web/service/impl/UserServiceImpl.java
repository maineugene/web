package com.zhukovskiy.web.service.impl;

import com.zhukovskiy.web.dao.UserDao;
import com.zhukovskiy.web.dao.impl.UserDaoImpl;
import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.entity.UserRole;
import com.zhukovskiy.web.exception.DaoException;
import com.zhukovskiy.web.exception.ServiceException;
import com.zhukovskiy.web.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger();

    private static class InstanceHolder{
         static final UserServiceImpl instance = new UserServiceImpl();
    }

    private final UserDao userDao;

    private UserServiceImpl() {
        this.userDao = UserDaoImpl.getInstance();
    }

    public static UserServiceImpl getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public Optional<User> authenticate(String login, String password) throws ServiceException {
        // validate login, pass + md5
        try {
            Optional<User> userOptional = userDao.findByLogin(login);
            if (userOptional.isEmpty()) {
                logger.debug("User not found: {}", login);
                return Optional.empty();
            }
            User user = userOptional.get();
            if (user.checkPassword(password)) {
                logger.info("User authenticated: {} (role: {})", login, user.getRole());
                return Optional.of(user);
            }
            logger.debug("Invalid password for user: {}", login);
            return Optional.empty();
        } catch (DaoException e) {
            throw new ServiceException("Authentication error for user: " + login, e);
        }
    }

    @Override
    public User register(String login, String password) throws ServiceException {
        try {
            if (userDao.findByLogin(login).isPresent()) {
                logger.warn("User with login {} already exists", login);
                throw new ServiceException("Login '" + login + "' is already taken");
            }

            User user = new User(login, password, UserRole.CLIENT);
            if (!userDao.create(user)) {
                logger.error("Registration failed: could not save user '{}' to database", login);
                throw new ServiceException("Registration failed. Please try again.");
            }

            logger.info("User '{}' registered successfully with ID: {}",
                    login, user.getId());
            return user;
        } catch (DaoException e) {
            logger.error("Database error during registration for user '{}': {}",
                    login, e.getMessage(), e);
            throw new ServiceException("Registration error. Please try again later.", e);
        }
    }

    @Override
    public void changeUserRole(int userId, UserRole newRole) throws ServiceException {
        try {
            logger.info("Attempting to change role for user with id:{}", userId);
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new ServiceException("User with Id " + userId + " not found"));
            user.setRole(newRole);
            userDao.update(user);
            logger.info("Role changed successfully, {} is now {}", user.getLogin(), user.getRole());
        } catch (DaoException e) {
            logger.error("Database error while changing role for user with Id {}: {}",
                    userId, e.getMessage(), e);
            throw new ServiceException("Error while changing userRole", e);
        }
    }

    @Override
    public List<User> findAllUsers() throws ServiceException {
        try {
            return userDao.findAll();
        } catch (DaoException e) {
            logger.error("Error retrieving users list: {}", e.getMessage(), e);
            throw new ServiceException("Cannot retrieve users list", e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) throws ServiceException {
        try {
            Optional<User> userOptional = userDao.findByLogin(login);
            logger.debug("Found user with login: {}", userOptional.isPresent());
            return userOptional;
        } catch (DaoException e) {
            logger.error("Database error finding user by login '{}': {}",
                    login, e.getMessage(), e);
            throw new ServiceException("Error searching for user", e);
        }
    }
}