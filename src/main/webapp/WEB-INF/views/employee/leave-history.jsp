<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Your complete leave request history.">
    <title>Leave History — ${employee.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-wrapper">

<%-- ===== Navigation Bar ===== --%>
<nav class="navbar">
    <span class="navbar-brand">
        <span class="logo-dot"></span>
        Leave Tracker
    </span>
    <ul class="navbar-nav">
        <li><a href="${pageContext.request.contextPath}/employee/dashboard">Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/leave-history" class="active">Leave History</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/apply-leave">Apply Leave</a></li>
        <li>
            <form method="post" action="${pageContext.request.contextPath}/logout" style="display:inline;">
                <button class="btn btn-outline btn-sm" type="submit" id="logout-btn">Logout</button>
            </form>
        </li>
    </ul>
</nav>

<%-- ===== Page Content ===== --%>
<main class="page-content">

    <div class="section-heading">
        <h2>&#128196; Your Leave History</h2>
        <a id="apply-leave-link"
           class="btn btn-primary btn-sm"
           href="${pageContext.request.contextPath}/employee/apply-leave">
            + Apply for Leave
        </a>
    </div>

    <%-- Error from servlet --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">&#9888; <c:out value="${errorMessage}"/></div>
    </c:if>

    <%-- Leave Table or Empty State --%>
    <c:choose>
        <c:when test="${empty leaveHistory}">
            <div class="empty-state">
                <div class="empty-icon">&#128203;</div>
                <h3>No leave requests yet</h3>
                <p>You have not submitted any leave requests. Click &ldquo;Apply for Leave&rdquo; to get started.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table id="leave-history-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Leave Type</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Status</th>
                            <th>Reason</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="req" items="${leaveHistory}" varStatus="loop">
                            <tr>
                                <td><c:out value="${loop.count}"/></td>
                                <td><c:out value="${req.leaveType}"/></td>
                                <td><c:out value="${req.startDate}"/></td>
                                <td><c:out value="${req.endDate}"/></td>
                                <td>
                                    <span class="badge
                                        <c:choose>
                                            <c:when test="${req.status == 'APPROVED'}">badge-approved</c:when>
                                            <c:when test="${req.status == 'REJECTED'}">badge-rejected</c:when>
                                            <c:otherwise>badge-pending</c:otherwise>
                                        </c:choose>">
                                        <c:out value="${req.status}"/>
                                    </span>
                                </td>
                                <td><c:out value="${req.reason}"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

</main>

</body>
</html>
