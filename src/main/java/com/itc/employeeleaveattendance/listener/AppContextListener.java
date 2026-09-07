package com.itc.employeeleaveattendance.listener;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAOImpl;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAOImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // Instantiate DAOs exactly once
        LeaveBalanceDAO leaveBalanceDAO = new LeaveBalanceDAOImpl();
        LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAOImpl();

        // Register them in the ServletContext so servlets can wire them in init()
        context.setAttribute("leaveBalanceDAO", leaveBalanceDAO);
        context.setAttribute("leaveRequestDAO", leaveRequestDAO);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}
