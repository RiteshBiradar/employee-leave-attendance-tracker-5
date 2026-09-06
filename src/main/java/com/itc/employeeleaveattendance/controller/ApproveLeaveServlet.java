package com.itc.employeeleaveattendance.controller;

import com.itc.employeeleaveattendance.service.LeaveRequestService;
import com.itc.employeeleaveattendance.service.impl.LeaveRequestServiceImpl;

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
            int managerId = ((Number) session.getAttribute("empId")).intValue();
            int requestId = Integer.parseInt(request.getParameter("requestId"));
            leaveRequestService.approveLeave(requestId, managerId);
            session.setAttribute("successMessage", "Leave request approved.");
        } catch (RuntimeException exception) {
            session.setAttribute("errorMessage", exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/manager/dashboard");
    }
}