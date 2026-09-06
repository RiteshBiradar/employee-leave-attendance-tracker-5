package com.itc.employeeleaveattendance.controller.manager;

import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Placeholder for the Manager Dashboard.
 * Loads the authenticated employee from session and forwards to the dashboard JSP.
 * The actual manager-specific functionality is assigned to another team member.
 */
@WebServlet("/manager/dashboard")
public class ManagerDashboardStubServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Load employee from session so the JSP can render ${employee.name} etc.
        HttpSession session  = request.getSession(false);
        Employee    employee = (Employee) session.getAttribute(
                AuthenticationFilter.SESSION_ATTR_EMPLOYEE);

        request.setAttribute("employee", employee);

        request.getRequestDispatcher("/WEB-INF/views/manager/dashboard.jsp")
               .forward(request, response);
    }
}
