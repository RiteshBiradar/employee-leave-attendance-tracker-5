<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Manager dashboard — review team leaves.">
    <title>Manager Dashboard — ${employee.name}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="page-wrapper">

<%-- ===== Navigation Bar ===== --%>
<nav class="navbar">
    <span class="navbar-brand">
        <span class="logo-dot"></span>
        Leave Tracker <span style="font-size: 0.8rem; opacity: 0.8; margin-left: 5px;">(Manager)</span>
    </span>
    <ul class="navbar-nav">
        <li><a href="${pageContext.request.contextPath}/manager/dashboard" class="active">Dashboard</a></li>
        <li><a href="#" style="opacity: 0.6; cursor: not-allowed;" title="Implemented by teammate">Team Leaves</a></li>
        <li><a href="#" style="opacity: 0.6; cursor: not-allowed;" title="Implemented by teammate">Reports</a></li>
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
        <p>Here&rsquo;s an overview of your team's leave requests.</p>
    </div>

    <%-- Profile Strip --%>
    <div class="profile-strip">
        <div class="profile-avatar" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%);">
            <c:out value="${employee.name.substring(0,1).toUpperCase()}"/>
        </div>
        <div class="profile-info">
            <h3><c:out value="${employee.name}"/></h3>
            <p>
                <c:out value="${employee.email}"/> &nbsp;&bull;&nbsp;
                <span class="badge badge-manager">
                    <c:out value="${employee.role}"/>
                </span>
            </p>
        </div>
    </div>

    <%-- Team Stats Placeholder --%>
    <div class="section-heading">
        <h2>&#128101; Team Overview</h2>
    </div>
    <div class="stats-grid" style="margin-bottom:32px;">
        <div class="stat-card">
            <div class="stat-label">Pending Approvals</div>
            <div class="stat-value warning">
                0
            </div>
            <p style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 5px;">(Module by teammate)</p>
        </div>
        <div class="stat-card">
            <div class="stat-label">On Leave Today</div>
            <div class="stat-value accent">
                0
            </div>
             <p style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 5px;">(Module by teammate)</p>
        </div>
        <div class="stat-card">
            <div class="stat-label">Total Team Members</div>
            <div class="stat-value">
                -
            </div>
        </div>
    </div>

    <%-- Quick Action Cards --%>
    <div class="section-heading">
        <h2>Quick Actions</h2>
    </div>
    <div class="action-grid">

        <a class="action-card" href="#" style="opacity: 0.7;">
            <div class="action-icon">&#9989;</div>
            <div class="action-title">Review Requests</div>
            <div class="action-desc">Approve or reject pending leave requests from your team. <br><em>(Teammate's Module)</em></div>
        </a>

        <a class="action-card" href="#" style="opacity: 0.7;">
            <div class="action-icon">&#128202;</div>
            <div class="action-title">Team Reports</div>
            <div class="action-desc">Generate monthly leave and attendance reports. <br><em>(Teammate's Module)</em></div>
        </a>

    </div>

</main>

</body>
</html>
