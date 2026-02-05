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

public class LoginCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    @Override
    public String execute(HttpServletRequest request) throws CommandException {
        String login = request.getParameter(RequestParameter.LOGIN);
        String password = request.getParameter(RequestParameter.PASSWORD);
        UserService userService = UserServiceImpl.getInstance();
        String page;
        HttpSession session = request.getSession();
        try {
            Optional<User> userOptional = userService.authenticate(login, password);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                session.setAttribute(SessionAttribute.USER, user);
                session.setAttribute(SessionAttribute.USER_ROLE, user.getRole());
                session.setAttribute(SessionAttribute.USER_ID, user.getId());
                session.setAttribute(SessionAttribute.USER_NAME, user.getLogin());

                request.setAttribute(RequestAttribute.USER_NAME, user.getLogin());

                if(user.getRole() == UserRole.ADMIN){
                    logger.info("Admin user logged in: {}", login);
                    page = PagePath.ADMIN;
                } else {
                    logger.info("Regular user logged in: {}", login);
                    page = PagePath.MAIN;
                }
                request.removeAttribute(RequestAttribute.LOGIN_MSG);
            } else {
                request.setAttribute(RequestAttribute.LOGIN_MSG, "incorrect login or password");
                request.setAttribute(RequestAttribute.LOGIN, login);
                page = PagePath.INDEX;
            }
            session.setAttribute(SessionAttribute.CURRENT_PAGE, page);
        } catch (ServiceException e) {
            logger.error("Log in failed for {}: {}", login, e.getMessage());
            throw new CommandException(e);
        }
        return page;
    }
}
