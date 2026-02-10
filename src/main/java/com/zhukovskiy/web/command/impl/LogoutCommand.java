package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.command.Router;
import com.zhukovskiy.web.util.PagePath;
import jakarta.servlet.http.HttpServletRequest;

public class LogoutCommand implements Command {
    @Override
    public Router execute(HttpServletRequest request) {
        request.getSession().invalidate();
        return new Router(PagePath.INDEX, Router.Type.FORWARD);
    }
}
