package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.command.Router;
import com.zhukovskiy.web.util.PagePath;
import jakarta.servlet.http.HttpServletRequest;

public class DefaultCommand implements Command {
    @Override
    public Router execute(HttpServletRequest request) {
        return new Router(PagePath.INDEX, Router.Type.FORWARD);
    }
}
