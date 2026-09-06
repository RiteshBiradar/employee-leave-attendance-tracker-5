<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Employee Leave & Attendance Tracker — Sign in to manage your leave requests.">
    <title>Login — Employee Leave & Attendance Tracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="login-page">
    <div class="login-card">

        <div class="login-logo">
            <h1>&#128197; Leave Tracker</h1>
            <p>Employee Leave &amp; Attendance System</p>
        </div>

        <%-- Error message from LoginServlet --%>
        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-error" role="alert">
                &#9888; ${errorMessage}
            </div>
        <% } %>

        <form id="login-form"
              method="post"
              action="${pageContext.request.contextPath}/login"
              novalidate>

            <div class="form-group">
                <label class="form-label" for="empId">Employee ID</label>
                <input id="empId"
                       class="form-control"
                       type="number"
                       name="empId"
                       placeholder="e.g. 1"
                       min="1"
                       required
                       autocomplete="username">
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input id="password"
                       class="form-control"
                       type="password"
                       name="password"
                       placeholder="Enter your password"
                       required
                       autocomplete="current-password">
            </div>

            <button id="login-btn" class="btn btn-primary" type="submit">
                Sign In &rarr;
            </button>

        </form>

    </div>
</div>

</body>
</html>