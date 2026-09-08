package com.itc.employeeleaveattendance.controller;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.service.impl.LeaveRequestServiceImpl;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/manager/dashboard")
public class ManagerDashboardServlet extends HttpServlet {

    private final LeaveRequestService leaveRequestService = new LeaveRequestServiceImpl();
    private LeaveBalanceDAO leaveBalanceDAO;

    @Override
    public void init() throws ServletException {
        this.leaveBalanceDAO = (LeaveBalanceDAO) getServletContext().getAttribute("leaveBalanceDAO");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Employee employee = session == null ? null
            : (Employee) session.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        LeaveBalance leaveBalance = leaveBalanceDAO.findByEmployeeId(employee.getEmpId());
        request.setAttribute("leaveBalance", leaveBalance);
        
        request.setAttribute("pendingRequests",
                leaveRequestService.getPendingRequestsForManager(employee.getEmpId()));
        request.setAttribute("employee", employee);
        request.getRequestDispatcher("/WEB-INF/views/manager/dashboard.jsp")
                .forward(request, response);
    }
}