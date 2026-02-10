package com.zhukovskiy.web.command;

import com.zhukovskiy.web.util.PagePath;

public class Router {
    private String page;
    private Type type;

     public enum Type{
        FORWARD,
        REDIRECT
    };

     public Router(){
         this.page = PagePath.INDEX;
         this.type = Type.FORWARD;
     }

    public Router(String page) {
        this(page, Type.FORWARD);
    }

    public Router(String page, Type type) {
        this.page = page;
        this.type = type;
    }

    public String getPage() {
        return page;
    }

    public Type getType() {
        return type;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isRedirect() {
        return type == Type.REDIRECT;
    }
}
