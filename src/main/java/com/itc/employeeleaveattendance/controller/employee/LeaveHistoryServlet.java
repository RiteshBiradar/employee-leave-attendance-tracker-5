package com.itc.employeeleaveattendance.controller.employee;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.dao.LeaveRequestDAO;
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
import java.util.List;

/**
 * Serves the employee's own leave-history page.
 *
 * <p>GET /employee/leave-history → loads only the authenticated employee's
 * leave records from the database and forwards to the leave-history JSP.
 *
 * <p>Security note: The employee ID is taken exclusively from the session.
 * Any request parameter named "empId" is deliberately ignored to prevent
 * insecure direct object reference (IDOR) attacks.
 */
@WebServlet("/employee/leave-history")
public class LeaveHistoryServlet extends HttpServlet {

    /**
     * Wired in {@link #init()} from a DAO registered on the {@code ServletContext}
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

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // --- Get authenticated employee from session (guaranteed by filters) ---
        HttpSession session  = request.getSession(false);
        Employee    employee = (Employee) session.getAttribute(
                AuthenticationFilter.SESSION_ATTR_EMPLOYEE);

        // empId comes from the SESSION — never from request parameters
        int empId = employee.getEmpId();

        List<LeaveRequest> leaveHistory = leaveService.getLeaveHistory(empId);

        request.setAttribute("employee",    employee);
        request.setAttribute("leaveHistory", leaveHistory);

        request.getRequestDispatcher("/WEB-INF/views/employee/leave-history.jsp")
               .forward(request, response);
    }
}
