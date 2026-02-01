package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
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
            if (userService.authenticate(login, password)) {
                request.setAttribute(RequestAttribute.USER, login);
                session.setAttribute(SessionAttribute.USER_NAME, login);
                page = PagePath.MAIN;
            } else {
                request.setAttribute(RequestAttribute.LOGIN_MSG, "incorrect login or password");
                page = PagePath.INDEX;
            }
            session.setAttribute(SessionAttribute.CURRENT_PAGE, page);
        } catch (ServiceException e) {
            logger.error("Registration failed for {}: {}", login, e.getMessage());
            throw new CommandException(e);
        }

        return page;
    }
}
