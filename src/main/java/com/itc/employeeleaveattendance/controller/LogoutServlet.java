package com.itc.employeeleaveattendance.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles employee logout.
 *
 * <p>POST /logout → invalidates the session and redirects to the login page.
 * GET is also supported for convenience (e.g., a plain anchor logout link).
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        performLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        performLogout(request, response);
    }

    private void performLogout(HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        // Retrieve the session WITHOUT creating a new one
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // clears all session attributes and invalidates the token
        }

        // Prevent caching of the post-logout page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
