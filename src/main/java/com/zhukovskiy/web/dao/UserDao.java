package com.zhukovskiy.web.dao;

public interface UserDao {
    boolean authenticate(String login, String password);
}
