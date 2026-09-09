package com.itc.employeeleaveattendance.controller.employee;

import com.itc.employeeleaveattendance.constant.LeaveType;
import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
import com.itc.employeeleaveattendance.exception.InsufficientBalanceException;
import com.itc.employeeleaveattendance.exception.OverlappingLeaveException;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveRequest;
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
 * delegates to {@link LeaveService#applyLeave}, then:
 * <ul>
 *   <li>On success — redirects to the leave-history page.</li>
 *   <li>On {@link InsufficientBalanceException} or {@link OverlappingLeaveException}
 *       — sets {@code errorMessage} and forwards back to the form.</li>
 * </ul>
 *
 * <p>Security note: the employeeId is sourced exclusively from the HTTP session
 * (set by {@link AuthenticationFilter}) — never from a request parameter.
 */
@WebServlet("/employee/apply-leave")
public class ApplyLeaveServlet extends HttpServlet {

    /**
     * Wired in {@link #init()} from DAOs registered on the {@code ServletContext}
     * by the application's context listener.
     */
    private LeaveService leaveService;

    @Override
    public void init() throws ServletException {
        LeaveRequestDAO leaveRequestDAO =
                (LeaveRequestDAO) getServletContext().getAttribute("leaveRequestDAO");
        LeaveBalanceDAO leaveBalanceDAO =
                (LeaveBalanceDAO) getServletContext().getAttribute("leaveBalanceDAO");
        this.leaveService = new LeaveServiceImpl(leaveRequestDAO, leaveBalanceDAO);
    }

    // -----------------------------------------------------------------------
    // GET — serve the blank apply-leave form
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setHolidaysAttribute(request);
        request.getRequestDispatcher("/WEB-INF/views/employee/apply-leave.jsp")
               .forward(request, response);
    }

    // -----------------------------------------------------------------------
    // POST — validate and submit the leave request
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
                
        // Validation: Employee must have a manager assigned to apply for leave
        if (employee.getManagerId() == null || employee.getManagerId() == 0) {
            request.setAttribute("errorMessage", "You cannot apply for leave because you do not have an assigned manager. Please contact HR.");
            setHolidaysAttribute(request);
            request.getRequestDispatcher("/WEB-INF/views/employee/apply-leave.jsp")
                   .forward(request, response);
            return;
        }

        long employeeId = employee.getEmpId();

        // --- Build leave request ---
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(employeeId);
        leaveRequest.setLeaveType(LeaveType.valueOf(leaveTypeParam));
        leaveRequest.setStartDate(LocalDate.parse(startDateParam));
        leaveRequest.setEndDate(LocalDate.parse(endDateParam));
        leaveRequest.setReason(reason);

        // --- Delegate to service (validation happens inside) ---
        try {
            leaveService.applyLeave(leaveRequest);
            // Success — redirect to leave history (PRG pattern)
            response.sendRedirect(request.getContextPath() + "/employee/leave-history");

        } catch (InsufficientBalanceException | OverlappingLeaveException | IllegalArgumentException ex) {
            // Validation failure — redisplay form with the error message
            request.setAttribute("errorMessage", ex.getMessage());
            setHolidaysAttribute(request);
            request.getRequestDispatcher("/WEB-INF/views/employee/apply-leave.jsp")
                   .forward(request, response);
        }
    }
    
    private void setHolidaysAttribute(HttpServletRequest request) {
        java.util.List<String> holidays = com.itc.employeeleaveattendance.config.HolidayConfig.MANDATORY_HOLIDAYS.stream()
            .map(LocalDate::toString)
            .collect(java.util.stream.Collectors.toList());
        request.setAttribute("holidays", holidays);
    }
}
