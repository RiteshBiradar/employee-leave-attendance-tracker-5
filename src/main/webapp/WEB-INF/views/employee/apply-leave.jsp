<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Apply for Casual, Sick, or Earned leave.">
    <title>Apply for Leave</title>
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
        <c:if test="${sessionScope.loggedInEmployee.role == 'MANAGER'}">
            <li><a href="${pageContext.request.contextPath}/manager/dashboard">Manager Dashboard</a></li>
        </c:if>
        <li><a href="${pageContext.request.contextPath}/employee/apply-leave" class="active">Apply Leave</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/leave-history">Leave History</a></li>
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
        <h1>&#128221; Apply for Leave</h1>
    </div>

    <div class="card" style="max-width:560px;">
        <p class="card-subtitle">Fill in the details below to submit a new leave request.</p>

        <%-- Validation error from servlet --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" id="apply-leave-error">
                &#9888; <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form id="apply-leave-form"
              method="post"
              action="${pageContext.request.contextPath}/employee/apply-leave">

            <%-- Leave Type --%>
            <div class="form-group">
                <label class="form-label" for="leaveType">Leave Type</label>
                <select id="leaveType" name="leaveType" class="form-control" required>
                    <option value="" disabled selected>Select leave type&hellip;</option>
                    <option value="CASUAL">Casual</option>
                    <option value="SICK">Sick</option>
                    <option value="EARNED">Earned</option>
                </select>
            </div>

            <%-- Start Date --%>
            <div class="form-group">
                <label class="form-label" for="startDate">Start Date</label>
                <input id="startDate"
                       name="startDate"
                       type="date"
                       class="form-control"
                       required>
            </div>

            <%-- End Date --%>
            <div class="form-group">
                <label class="form-label" for="endDate">End Date</label>
                <input id="endDate"
                       name="endDate"
                       type="date"
                       class="form-control"
                       required>
            </div>

            <%-- Reason --%>
            <div class="form-group">
                <label class="form-label" for="reason">Reason</label>
                <textarea id="reason"
                          name="reason"
                          class="form-control"
                          rows="4"
                          placeholder="Briefly describe the reason for your leave&hellip;"
                          required></textarea>
            </div>

            <%-- Actions --%>
            <div style="display:flex; gap:12px; margin-top:8px;">
                <button id="submit-leave-btn" type="submit" class="btn btn-primary" style="width:auto; flex:1;">
                    Submit Request
                </button>
                <a id="cancel-leave-btn"
                   href="${pageContext.request.contextPath}/employee/dashboard"
                   class="btn btn-outline"
                   style="flex:1; text-align:center;">
                    Cancel
                </a>
            </div>

        </form>
    </div>

</main>

<script>
    const holidays = [
        <c:forEach var="holiday" items="${holidays}" varStatus="status">
            "${holiday}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    document.addEventListener("DOMContentLoaded", function() {
        const startDateInput = document.getElementById("startDate");
        const endDateInput = document.getElementById("endDate");
        const leaveTypeInput = document.getElementById("leaveType");
        
        // Set min date to today
        const today = new Date();
        // Adjust for local timezone
        today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
        const todayStr = today.toISOString().split('T')[0];
        
        startDateInput.setAttribute('min', todayStr);
        endDateInput.setAttribute('min', todayStr);

        function validateDate(input) {
            if (!input.value) return;
            // Use local date parsing by appending T00:00:00 to avoid UTC shifting
            const date = new Date(input.value + "T00:00:00");
            const day = date.getDay(); // 0 = Sunday, 6 = Saturday
            const dateString = input.value;
            
            if (day === 0 || day === 6) {
                alert("Weekends cannot be selected.");
                input.value = "";
                return;
            }
            
            if (holidays.includes(dateString)) {
                alert("Selected date is a mandatory holiday.");
                input.value = "";
                return;
            }
            
            if (leaveTypeInput.value === 'CASUAL' && (day === 1 || day === 5)) {
                alert("Casual Leave cannot be applied on restricted weekday dates (Monday, Friday).");
                input.value = "";
                return;
            }
        }
        
        startDateInput.addEventListener('change', function() { validateDate(this); });
        endDateInput.addEventListener('change', function() { validateDate(this); });
        leaveTypeInput.addEventListener('change', function() {
            validateDate(startDateInput);
            validateDate(endDateInput);
        });
    });
</script>

</body>
</html>
