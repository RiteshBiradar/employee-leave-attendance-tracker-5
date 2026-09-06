<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Access denied — you do not have permission to view this page.">
    <title>403 Forbidden — Leave Tracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="error-page">
    <div class="card error-card">
        <div class="error-code">403</div>
        <h1 class="error-title">Access Denied</h1>
        <p class="error-message">
            You do not have permission to access this page.
            <% if (request.getAttribute("forbiddenRole") != null) { %>
                Your current role is <strong>${forbiddenRole}</strong>.
            <% } %>
        </p>
        <a id="back-to-dashboard" class="btn btn-primary" href="${pageContext.request.contextPath}/employee/dashboard">
            &larr; Back to Dashboard
        </a>
    </div>
</div>

</body>
</html>
