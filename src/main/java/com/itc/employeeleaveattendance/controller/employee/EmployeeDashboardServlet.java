package com.itc.employeeleaveattendance.controller.employee;

import com.itc.employeeleaveattendance.dao.LeaveBalanceDAO;
import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.model.LeaveBalance;
import com.itc.employeeleaveattendance.service.LeaveBalanceService;
import com.itc.employeeleaveattendance.service.impl.LeaveBalanceServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Serves the employee dashboard page.
 *
 * <p>GET /employee/dashboard → loads the authenticated employee from session,
 * fetches their leave balance, and forwards to the dashboard JSP.
 *
 * <p>Access is already protected by {@code AuthenticationFilter} and
 * {@code AuthorizationFilter}; by the time this servlet runs, the session
 * is guaranteed to contain an EMPLOYEE-role user.
 */
@WebServlet("/employee/dashboard")
public class EmployeeDashboardServlet extends HttpServlet {

    /**
     * Wired in {@link #init()} from a DAO registered on the {@code ServletContext}
     * by the application's context listener.
     */
    private LeaveBalanceService leaveBalanceService;

    @Override
    public void init() throws ServletException {
        LeaveBalanceDAO leaveBalanceDAO =
                (LeaveBalanceDAO) getServletContext().getAttribute("leaveBalanceDAO");
        this.leaveBalanceService = new LeaveBalanceServiceImpl(leaveBalanceDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session  = request.getSession(false);
        Employee    employee = (Employee) session.getAttribute(
                AuthenticationFilter.SESSION_ATTR_EMPLOYEE);

        // Expose employee to JSP
        request.setAttribute("employee", employee);

        // Fetch and expose leave balance (gracefully handle missing balance row)
        LeaveBalance balance = leaveBalanceService.getBalance(employee.getEmpId(), null);
        if (balance != null) {
            request.setAttribute("leaveBalance", balance);
        }

        request.getRequestDispatcher("/WEB-INF/views/employee/dashboard.jsp")
               .forward(request, response);
    }
}
