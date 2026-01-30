package com.zhukovskiy.web.command;

import com.zhukovskiy.web.command.impl.AddUserCommand;
import com.zhukovskiy.web.command.impl.DefaultCommand;
import com.zhukovskiy.web.command.impl.LoginCommand;
import com.zhukovskiy.web.command.impl.LogoutCommand;

public enum CommandType {
    ADD_USER(new AddUserCommand()),
    LOGIN(new LoginCommand()),
    LOGOUT(new LogoutCommand()),
    DEFAULT(new DefaultCommand());

    final Command command;

    CommandType(Command command) {
        this.command = command;
    }

    public static Command define(String commandStr) {
        CommandType current = CommandType.valueOf(commandStr.toUpperCase());
        return current.command;
    }

}
