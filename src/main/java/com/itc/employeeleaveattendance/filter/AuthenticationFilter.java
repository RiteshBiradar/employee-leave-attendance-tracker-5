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
 * Authentication gate for all protected URL patterns.
 *
 * <p>Intercepts every request to {@code /employee/*} and {@code /manager/*}.
 * If the request carries a valid session with a logged-in employee, the
 * filter chain continues normally. Otherwise the user is redirected to
 * the login page.
 *
 * <p>This filter runs BEFORE {@link AuthorizationFilter} (declared first
 * in {@code web.xml} or via annotation ordering).
 */
@WebFilter(urlPatterns = {"/employee/*", "/manager/*"})
public class AuthenticationFilter implements Filter {

    /** Session attribute key — must match what LoginServlet sets. */
    public static final String SESSION_ATTR_EMPLOYEE = "loggedInEmployee";

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

        // Retrieve existing session — do NOT create a new one here
        HttpSession session = httpReq.getSession(false);

        Employee employee = null;
        if (session != null) {
            employee = (Employee) session.getAttribute(SESSION_ATTR_EMPLOYEE);
        }

        if (employee == null) {
            // No valid authenticated session → redirect to login
            String loginUrl = httpReq.getContextPath() + "/login";
            httpResp.sendRedirect(loginUrl);
            return;
        }

        // Session is valid — proceed down the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}
