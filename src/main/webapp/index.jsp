<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Redirect the root URL to the login page --%>
<% response.sendRedirect(request.getContextPath() + "/login"); %>
