package com.zhukovskiy.web.service;

public interface UserService {
    boolean authenticate(String login, String password);
}
