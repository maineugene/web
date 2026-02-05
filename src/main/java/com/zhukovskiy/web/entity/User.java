package com.zhukovskiy.web.entity;

import com.zhukovskiy.web.util.PasswordHasher;

public class User extends AbstractEntity {
    private String login;
    private String passwordHash;
    private UserRole role;

    public User() {
    }

    public User(String login, String plainPassword, UserRole role) {
        this.login = login;
        setPasswordHash(plainPassword);
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean checkPassword(String plainPassword) {
        return PasswordHasher.checkPassword(plainPassword, this.passwordHash);
    }

    public UserDaoDto toDaoDto(){
        return new UserDaoDto(getId(), getLogin(), passwordHash, getRole());
    }

    public static User fromDaoDto(UserDaoDto dto){
        User user = new User(dto.login(), "", dto.role());
        user.setId(dto.id());
        user.passwordHash = dto.passwordHash();
        return user;
    }
}
