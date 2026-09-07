<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Manager dashboard - review team leave requests.">
    <title>Manager Dashboard - ${employee.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-wrapper">

<nav class="navbar">
    <span class="navbar-brand">
        <span class="logo-dot"></span>
        Leave Tracker <span class="role-label">(Manager)</span>
    </span>
    <ul class="navbar-nav">
        <li><a href="${pageContext.request.contextPath}/manager/dashboard" class="active">Dashboard</a></li>
        <li>
            <form method="post" action="${pageContext.request.contextPath}/logout" class="inline-form">
                <button class="btn btn-outline btn-sm" type="submit" id="logout-btn">Logout</button>
            </form>
        </li>
    </ul>
</nav>

<main class="page-content">
    <div class="dashboard-header">
        <h1>Welcome back, <c:out value="${employee.name}" /></h1>
        <p>Review pending leave requests from your direct reports.</p>
    </div>

    <div class="profile-strip">
        <div class="profile-info">
            <h2><c:out value="${employee.name}" /></h2>
            <p>
                <c:out value="${employee.email}" />
                <span class="badge badge-manager"><c:out value="${employee.role}" /></span>
            </p>
        </div>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success" role="status">
            <c:out value="${sessionScope.successMessage}" />
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${sessionScope.errorMessage}" />
        </div>
    </c:if>

    <div class="section-heading">
        <h2>Pending Leave Requests</h2>
        <span class="badge badge-pending">
            <c:out value="${pendingRequests.size()}" />
        </span>
    </div>

    <div class="card">
        <c:choose>
            <c:when test="${not empty pendingRequests}">
                <div class="table-container">
                    <table id="pending-requests-table">
                        <thead>
                        <tr>
                            <th>Employee</th>
                            <th>Leave Type</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Working Days</th>
                            <th>Reason</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="leaveRequest" items="${pendingRequests}">
                            <tr>
                                <td><c:out value="${leaveRequest.employeeName}" /></td>
                                <td><c:out value="${leaveRequest.leaveType}" /></td>
                                <td><c:out value="${leaveRequest.startDate}" /></td>
                                <td><c:out value="${leaveRequest.endDate}" /></td>
                                <td><c:out value="${leaveRequest.workingDays}" /></td>
                                <td><c:out value="${leaveRequest.reason}" /></td>
                                <td>
                                    <span class="badge badge-pending">
                                        <c:out value="${leaveRequest.status}" />
                                    </span>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/manager/approve">
                                            <input type="hidden" name="requestId"
                                                   value="${leaveRequest.requestId}">
                                            <button type="submit" class="btn btn-success btn-sm">
                                                Approve
                                            </button>
                                        </form>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/manager/reject">
                                            <input type="hidden" name="requestId"
                                                   value="${leaveRequest.requestId}">
                                            <button type="submit" class="btn btn-danger btn-sm">
                                                Reject
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>No pending leave requests from your direct reports.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

</body>
</html>
