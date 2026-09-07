package com.itc.employeeleaveattendance.controller;

import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.service.impl.LeaveRequestServiceImpl;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/manager/approve")
public class ApproveLeaveServlet extends HttpServlet {

    private final LeaveRequestService leaveRequestService = new LeaveRequestServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        try {
            Employee manager = (Employee) session.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE);
            long managerId = manager.getEmpId();
            long requestId = Long.parseLong(request.getParameter("requestId"));
            leaveRequestService.approveLeave(requestId, managerId);
            session.setAttribute("successMessage", "Leave request approved.");
        } catch (RuntimeException exception) {
            session.setAttribute("errorMessage", exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/dashboard");
    }
}