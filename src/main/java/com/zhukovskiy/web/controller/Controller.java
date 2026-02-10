package com.zhukovskiy.web.controller;

import java.io.*;

import com.zhukovskiy.web.command.Command;
import com.zhukovskiy.web.command.CommandType;
import com.zhukovskiy.web.command.Router;
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
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String commandStr = request.getParameter(RequestParameter.COMMAND);
        logger.debug("Processing command: {}", commandStr);
        Command command = CommandType.define(commandStr);
        try {
            Router router = command.execute(request);
            if(router == null){
                throw new CommandException("Command returned null router");
            }
            handleRouter(router, request, response);
        } catch (CommandException e) {
            logger.error("Command execution failed: {}", commandStr, e);
            request.setAttribute(RequestAttribute.ERROR_MSG, e.getMessage());
            request.getRequestDispatcher(PagePath.ERROR_500).forward(request, response);
        } catch (Exception e) {
            logger.error("Unexpected error processing command: {}", commandStr, e);
            request.setAttribute(RequestAttribute.ERROR_MSG, "Internal server error");
            request.getRequestDispatcher(PagePath.ERROR_500).forward(request, response);
        }
    }

    private void handleRouter(Router router, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String page = router.getPage();
        if (router.isRedirect()) {
            response.sendRedirect(request.getContextPath() + page);
        } else {
            request.getRequestDispatcher(page).forward(request, response);
        }
    }

    public void destroy() {
        ConnectionPool.getInstance().destroyPool();
        logger.info("Servlet destroyed:{}", this.getServletInfo());
    }
}