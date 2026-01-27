package com.zhukovskiy.web.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebFilter(urlPatterns = {"/pages/*"},
        initParams = {@WebInitParam(name = "INDEX_PATH", value = "/index.jsp")})
public class PageRedirectSecurityFilter implements Filter {
    private static final Logger logger = LogManager.getLogger();
    private String indexPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        indexPath = filterConfig.getInitParameter("INDEX_PATH");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + indexPath);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
