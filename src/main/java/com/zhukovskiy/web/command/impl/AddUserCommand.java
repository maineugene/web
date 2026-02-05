package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.entity.User;
import com.zhukovskiy.web.entity.UserRole;
import com.zhukovskiy.web.exception.CommandException;
import com.zhukovskiy.web.exception.ServiceException;
import com.zhukovskiy.web.service.UserService;
import com.zhukovskiy.web.service.impl.UserServiceImpl;
import com.zhukovskiy.web.util.PagePath;
import com.zhukovskiy.web.util.RequestAttribute;
import com.zhukovskiy.web.util.RequestParameter;
import com.zhukovskiy.web.util.SessionAttribute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AddUserCommand implements Command {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public String execute(HttpServletRequest request) throws CommandException {
        String login = request.getParameter(RequestParameter.LOGIN);
        String password = request.getParameter(RequestParameter.PASSWORD);
        if (login == null || login.isBlank()) {
            request.setAttribute(RequestAttribute.REGISTER_MSG, "Login cannot be empty");
            request.setAttribute(RequestAttribute.LOGIN, login);
            return PagePath.INDEX;
        }

        if (password == null || password.isBlank()) {
            request.setAttribute(RequestAttribute.REGISTER_MSG, "Password cannot be empty");
            request.setAttribute(RequestAttribute.LOGIN, login);
            return PagePath.INDEX;
        }

        UserService userService = UserServiceImpl.getInstance();
        String page;
        HttpSession session = request.getSession();
        logger.info("Attempting to register new user: {}", login);
        try {
            if (userService.findByLogin(login).isPresent()) {
                logger.info("User with login {} already exists", login);
                request.setAttribute(RequestAttribute.REGISTER_MSG, "Login '" + login + "' is already taken");
                request.setAttribute(RequestAttribute.LOGIN, login);
                return PagePath.INDEX;
            }

            User user = userService.register(login, password);
            logger.info("User {} registered successfully", login);

            Optional<User> authResult = userService.authenticate(login, password);
            if (authResult.isPresent()) {
                User authUser = authResult.get();
                request.setAttribute(RequestAttribute.USER, authUser);
                session.setAttribute(SessionAttribute.USER, authUser);
                session.setAttribute(SessionAttribute.USER_NAME, authUser.getLogin());
                session.setAttribute(SessionAttribute.USER_ROLE, authUser.getRole());
                session.setAttribute(SessionAttribute.USER_ID, authUser.getId());
                if (authUser.getRole() == UserRole.ADMIN) {
                    page = PagePath.ADMIN;
                    logger.info("Admin user {} auto-logged after registration", login);
                } else {
                    page = PagePath.MAIN;
                    logger.info("User {} auto-logged after registration", login);
                }
                request.setAttribute(RequestAttribute.REGISTER_MSG,
                        "Registration successful! Welcome, " + login + "!");
                session.setAttribute(SessionAttribute.CURRENT_PAGE, page);
                return page;
            } else {
                request.setAttribute(RequestAttribute.REGISTER_MSG,
                        "Registration successful! Please login with your credentials.");
                logger.warn("User {} registered but auto-login failed", login);
                return PagePath.INDEX;
            }
        } catch (ServiceException e) {
            logger.error("Registration failed for {}: {}", login, e.getMessage());
            throw new CommandException(e);
        }
    }
}
