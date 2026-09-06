package com.itc.employeeleaveattendance.controller.employee;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveRequest;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.service.LeaveService;
import com.itc.employeeleaveattendance.service.impl.LeaveServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Handles the "Apply for Leave" flow for employees.
 *
 * <p>GET  /employee/apply-leave → forwards to the apply-leave form JSP.
 * <p>POST /employee/apply-leave → reads form parameters, builds a LeaveRequest,
 * delegates to {@link LeaveService#applyLeave}, then redirects to leave-history.
 *
 * <p>Security note: the employeeId is sourced exclusively from the HTTP session
 * (set by {@link AuthenticationFilter}) — never from a request parameter.
 *
 * <p>First-pass implementation: no validation, overlap checks, or balance checks.
 */
@WebServlet("/employee/apply-leave")
public class ApplyLeaveServlet extends HttpServlet {

    /**
     * Wired in {@link #init()} from a DAO registered on the {@code ServletContext}
     * by the application's context listener.
     */
    private LeaveService leaveService;

    @Override
    public void init() throws ServletException {
        LeaveRequestDAO leaveRequestDAO =
                (LeaveRequestDAO) getServletContext().getAttribute("leaveRequestDAO");
        this.leaveService = new LeaveServiceImpl(leaveRequestDAO);
    }

    // -----------------------------------------------------------------------
    // GET — serve the blank apply-leave form
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/employee/apply-leave.jsp")
               .forward(request, response);
    }

    // -----------------------------------------------------------------------
    // POST — process the submitted form
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // --- Read form parameters ---
        String leaveTypeParam = request.getParameter("leaveType");
        String startDateParam = request.getParameter("startDate");
        String endDateParam   = request.getParameter("endDate");
        String reason         = request.getParameter("reason");

        // --- Get authenticated employee from session ---
        HttpSession session  = request.getSession(false);
        Employee    employee = (Employee) session.getAttribute(
                AuthenticationFilter.SESSION_ATTR_EMPLOYEE);
        long employeeId = employee.getEmpId();

        // --- Build leave request ---
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(employeeId);
        leaveRequest.setLeaveType(LeaveType.valueOf(leaveTypeParam));
        leaveRequest.setStartDate(LocalDate.parse(startDateParam));
        leaveRequest.setEndDate(LocalDate.parse(endDateParam));
        leaveRequest.setReason(reason);

        // --- Delegate to service ---
        leaveService.applyLeave(leaveRequest);

        // --- Redirect to leave-history on success ---
        response.sendRedirect(request.getContextPath() + "/employee/leave-history");
    }
}
