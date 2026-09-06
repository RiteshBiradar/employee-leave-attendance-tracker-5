<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Employee dashboard — manage your leaves and view your profile.">
    <title>Dashboard — ${employee.name}</title>
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
        <li><a href="${pageContext.request.contextPath}/employee/dashboard" class="active">Dashboard</a></li>
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

    <%-- Welcome Header --%>
    <div class="dashboard-header">
        <h2>Welcome back, <c:out value="${employee.name}"/> &#128075;</h2>
        <p>Here&rsquo;s an overview of your leave activity.</p>
    </div>

    <%-- Profile Strip --%>
    <div class="profile-strip">
        <div class="profile-avatar">
            <c:out value="${employee.name.substring(0,1).toUpperCase()}"/>
        </div>
        <div class="profile-info">
            <h3><c:out value="${employee.name}"/></h3>
            <p>
                <c:out value="${employee.email}"/> &nbsp;&bull;&nbsp;
                <span class="badge badge-employee">
                    <c:out value="${employee.role}"/>
                </span>
            </p>
        </div>
    </div>

    <%-- Leave Balance Stats --%>
    <div class="section-heading">
        <h2>&#127381; Leave Balances</h2>
    </div>
    <c:choose>
        <c:when test="${not empty leaveBalance}">
            <div class="stats-grid" style="margin-bottom:32px;">
                <div class="stat-card">
                    <div class="stat-label">Casual Leave</div>
                    <div class="stat-value accent">
                        <c:out value="${leaveBalance.casualBalance}"/>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Sick Leave</div>
                    <div class="stat-value warning">
                        <c:out value="${leaveBalance.sickBalance}"/>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Earned Leave</div>
                    <div class="stat-value">
                        <c:out value="${leaveBalance.earnedBalance}"/>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <p style="color:var(--color-text-muted);font-size:0.85rem;margin-bottom:28px;">
                Leave balance not yet assigned. Please contact HR.
            </p>
        </c:otherwise>
    </c:choose>

    <%-- Quick Action Cards --%>
    <div class="section-heading">
        <h2>Quick Actions</h2>
    </div>
    <div class="action-grid">

        <a id="action-leave-history"
           class="action-card"
           href="${pageContext.request.contextPath}/employee/leave-history">
            <div class="action-icon">&#128196;</div>
            <div class="action-title">Leave History</div>
            <div class="action-desc">View all your submitted leave requests and their status.</div>
        </a>

        <a id="action-apply-leave"
           class="action-card"
           href="${pageContext.request.contextPath}/employee/apply-leave">
            <div class="action-icon">&#128221;</div>
            <div class="action-title">Apply for Leave</div>
            <div class="action-desc">Submit a new Casual, Sick, or Earned leave request.</div>
        </a>

    </div>

</main>

</body>
</html>
