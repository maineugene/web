package com.zhukovskiy.web.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebFilter(filterName = "ForwardFilter", dispatcherTypes = DispatcherType.FORWARD, urlPatterns = "/pages/*")
public class ForwardFilter implements Filter {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpSession httpSession = httpServletRequest.getSession(false);
        httpSession.setAttribute("filter_attr", "DispatcherType.FORWARD");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
