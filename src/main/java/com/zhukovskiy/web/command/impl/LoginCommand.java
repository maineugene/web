package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.service.UserService;
import com.zhukovskiy.web.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;

public class LoginCommand implements Command {
    @Override
    public String execute(HttpServletRequest request) {
        String login = request.getParameter("login");
        String password = request.getParameter("pass");
        UserService userService = UserServiceImpl.getInstance();
        String page;
        if (userService.authenticate(login, password)) {
            request.setAttribute("user", login);
            page = "pages/main.jsp";
        } else {
            request.setAttribute("login_msg", "incorrect login or password");
            page = "index.jsp";
        }

        return page;
    }
}
