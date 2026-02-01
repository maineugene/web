package com.zhukovskiy.web.controller;

import java.io.*;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.command.CommandType;
import com.zhukovskiy.web.exception.CommandException;
import com.zhukovskiy.web.pool.ConnectionPool;
import com.zhukovskiy.web.util.PagePath;
import com.zhukovskiy.web.util.RequestAttribute;
import com.zhukovskiy.web.util.RequestParameter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(name = "helloServlet", urlPatterns = {"/controller", "*.do"})
public class Controller extends HttpServlet {
    private static final Logger logger = LogManager.getLogger();

    public void init() {
        ConnectionPool.getInstance();
        logger.info("Servlet init(): {}", this.getServletInfo());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        String commandStr = request.getParameter(RequestParameter.COMMAND);
        Command command = CommandType.define(commandStr);
        String page;
        try {
            page = command.execute(request);
            request.getRequestDispatcher(page).forward(request, response);
            //response.sendRedirect(page);
        } catch (CommandException e) {
            //response.sendError(500);  //1
            //throw new ServletException(e);//2
            request.setAttribute(RequestAttribute.ERROR_MSG, e.getCause());
            request.getRequestDispatcher(PagePath.ERROR_500).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String commandStr = request.getParameter(RequestParameter.COMMAND);
        Command command = CommandType.define(commandStr);
        String page;
        try {
            page = command.execute(request);
            request.getRequestDispatcher(page).forward(request, response);
            //response.sendRedirect(page);
        } catch (CommandException e) {
            //response.sendError(500);  //1
            //throw new ServletException(e);//2
            request.setAttribute(RequestAttribute.ERROR_MSG, e.getCause());
            request.getRequestDispatcher(PagePath.ERROR_500).forward(request, response);
        }

    }

    public void destroy() {
        ConnectionPool.getInstance().destroyPool();
        logger.info("Servlet destroyed:{}", this.getServletInfo());
    }
}