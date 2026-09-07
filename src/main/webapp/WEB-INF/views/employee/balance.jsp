<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Your current leave balance summary.">
    <title>Leave Balance</title>
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
        <li><a href="${pageContext.request.contextPath}/employee/leave-history">Leave History</a></li>
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
        <h1>&#127381; Leave Balance</h1>
        <a id="apply-leave-link"
           class="btn btn-primary btn-sm"
           href="${pageContext.request.contextPath}/employee/apply-leave">
            + Apply for Leave
        </a>
    </div>

    <%-- Balance Table or Empty State --%>
    <c:choose>
        <c:when test="${empty leaveBalances}">
            <div class="empty-state">
                <div class="empty-icon">&#128203;</div>
                <h3>No balance records found</h3>
                <p>Leave balances have not been assigned yet. Please contact HR.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table id="leave-balance-table">
                    <thead>
                        <tr>
                            <th>Leave Type</th>
                            <th>Total</th>
                            <th>Used</th>
                            <th>Remaining</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="bal" items="${leaveBalances}">
                            <tr>
                                <td><c:out value="${bal.leaveType}"/></td>
                                <td><c:out value="${bal.total}"/></td>
                                <td><c:out value="${bal.used}"/></td>
                                <td>
                                    <span class="badge
                                        <c:choose>
                                            <c:when test="${bal.remaining le 0}">badge-rejected</c:when>
                                            <c:when test="${bal.remaining le 3}">badge-pending</c:when>
                                            <c:otherwise>badge-approved</c:otherwise>
                                        </c:choose>">
                                        <c:out value="${bal.remaining}"/>
                                    </span>
                                </td>
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
