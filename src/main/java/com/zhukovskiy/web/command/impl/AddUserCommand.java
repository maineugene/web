package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.entity.User;
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

            if (userService.authenticate(login, password)) {
                request.setAttribute(RequestAttribute.USER, user);
                session.setAttribute(SessionAttribute.USER_NAME, login);
                request.setAttribute("success_msg",
                        "Registration successful! Welcome, " + login + "!");
                page = PagePath.MAIN;
                logger.info("User {} logged in after registration", login);
            } else {
                request.setAttribute("register_msg",
                        "Registration successful! Please login with your credentials.");
                page = PagePath.INDEX;
                logger.warn("User {} registered but auto-login failed", login);
            }
            session.setAttribute("current_page", page);
        } catch (ServiceException e) {
            logger.error("Registration failed for {}: {}", login, e.getMessage());
            throw new CommandException(e);
        }

        return page;
    }
}
