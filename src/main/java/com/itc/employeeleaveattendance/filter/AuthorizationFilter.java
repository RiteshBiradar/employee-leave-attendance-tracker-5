package com.itc.employeeleaveattendance.filter;

import com.itc.employeeleaveattendance.model.Employee;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Role-based access control (RBAC) filter.
 *
 * <p>Runs after {@link AuthenticationFilter} has already confirmed a valid
 * session. This filter checks that the authenticated employee's role matches
 * the URL space being accessed:
 * <ul>
 *   <li>{@code /employee/*} — requires role {@code EMPLOYEE}</li>
 *   <li>{@code /manager/*}  — requires role {@code MANAGER}</li>
 * </ul>
 *
 * <p>Any role mismatch results in HTTP 403 and a forward to the 403 error page.
 */
@WebFilter(urlPatterns = {"/employee/*", "/manager/*"})
public class AuthorizationFilter implements Filter {

    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String ROLE_MANAGER  = "MANAGER";

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialisation needed
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // AuthenticationFilter already ran — session and employee are guaranteed
        HttpSession session  = httpReq.getSession(false);
        Employee    employee = (Employee) session.getAttribute(
                AuthenticationFilter.SESSION_ATTR_EMPLOYEE);

        String servletPath = httpReq.getServletPath(); // e.g. "/employee/dashboard"
        boolean accessGranted = false;

        if (servletPath.startsWith("/employee/")) {
            accessGranted = ROLE_EMPLOYEE.equalsIgnoreCase(employee.getRole()) ||
                            ROLE_MANAGER.equalsIgnoreCase(employee.getRole());
        } else if (servletPath.startsWith("/manager/")) {
            accessGranted = ROLE_MANAGER.equalsIgnoreCase(employee.getRole());
        }

        if (accessGranted) {
            chain.doFilter(request, response);
        } else {
            // Authenticated but not authorised
            httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpReq.setAttribute("forbiddenRole", employee.getRole());
            httpReq.getRequestDispatcher("/WEB-INF/views/error/403.jsp")
                   .forward(request, response);
        }
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}
