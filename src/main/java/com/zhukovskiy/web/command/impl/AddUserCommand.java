package com.zhukovskiy.web.command.impl;

import com.zhukovskiy.web.command.Command;
import jakarta.servlet.http.HttpServletRequest;

public class AddUserCommand implements Command {
    @Override
    public String execute(HttpServletRequest request) {
        return "";
    }
}
