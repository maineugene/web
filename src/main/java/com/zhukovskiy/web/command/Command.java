package com.zhukovskiy.web.command;

import com.zhukovskiy.web.exception.CommandException;
import jakarta.servlet.http.HttpServletRequest;

public interface Command {
    Router execute(HttpServletRequest request) throws CommandException;
}
