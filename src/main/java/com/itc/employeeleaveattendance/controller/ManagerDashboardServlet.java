package com.itc.employeeleaveattendance.controller;

import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.service.impl.LeaveRequestServiceImpl;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object empId = session == null ? null : session.getAttribute("empId");
        if (!(empId instanceof Number)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("pendingRequests",
                leaveRequestService.getPendingRequestsForManager(((Number) empId).intValue()));
        request.getRequestDispatcher("/WEB-INF/views/manager/dashboard.jsp")
                .forward(request, response);
    }
}