package com.itc.employeeleaveattendance.controller;

import com.itc.employeeleaveattendance.filter.AuthenticationFilter;
import com.itc.employeeleaveattendance.model.Employee;
import com.itc.employeeleaveattendance.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles the login page (GET) and login form submission (POST).
 *
 * <p>GET  /login  → displays the login form
 * <p>POST /login  → validates credentials; on success creates a session and
 *                   redirects to the employee or manager dashboard.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    /** Show the login form. */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in, skip the login page
        HttpSession session  = request.getSession(false);
        if (session != null &&
                session.getAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE) != null) {

            Employee existing = (Employee) session.getAttribute(
                    AuthenticationFilter.SESSION_ATTR_EMPLOYEE);
            redirectToDashboard(existing, request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
               .forward(request, response);
    }

    /** Process login form submission. */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String empIdStr = request.getParameter("empId");
        String password = request.getParameter("password");

        // --- Input validation ---
        if (empIdStr == null || empIdStr.isBlank() ||
                password == null || password.isBlank()) {
            request.setAttribute("errorMessage", "Employee ID and Password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);
            return;
        }

        int empId;
        try {
            empId = Integer.parseInt(empIdStr.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Employee ID must be a number.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);
            return;
        }

        // --- Authentication ---
        try {
            Optional<Employee> result = authService.authenticate(empId, password);

            if (result.isEmpty()) {
                // Invalid credentials
                request.setAttribute("errorMessage",
                        "Invalid Employee ID or Password. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                       .forward(request, response);
                return;
            }

            Employee employee = result.get();

            // --- Session creation ---
            // Invalidate any pre-existing session to prevent session fixation
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(AuthenticationFilter.SESSION_ATTR_EMPLOYEE, employee);
            // 30-minute inactivity timeout (also set in web.xml — the lower of the two wins)
            newSession.setMaxInactiveInterval(30 * 60);

            redirectToDashboard(employee, request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);
        } catch (SQLException e) {
            // Do not expose DB details to the user
            getServletContext().log("Database error during login", e);
            request.setAttribute("errorMessage",
                    "A system error occurred. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);
        }
    }

    /** Redirect to the appropriate dashboard based on the employee's role. */
    private void redirectToDashboard(Employee employee,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException {
        String ctx = request.getContextPath();
        if ("MANAGER".equalsIgnoreCase(employee.getRole())) {
            response.sendRedirect(ctx + "/manager/dashboard");
        } else {
            response.sendRedirect(ctx + "/employee/dashboard");
        }
    }
}