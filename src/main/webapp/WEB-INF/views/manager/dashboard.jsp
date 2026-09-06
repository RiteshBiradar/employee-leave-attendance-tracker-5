<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manager Dashboard</title>
</head>
<body>

<header>
    <h1>Manager Dashboard</h1>
    <p>Pending leave requests from your direct reports</p>
</header>

<main>
    <c:if test="${not empty sessionScope.successMessage}">
        <p role="status"><c:out value="${sessionScope.successMessage}" /></p>
    </c:if>

    <c:if test="${not empty sessionScope.errorMessage}">
        <p role="alert"><c:out value="${sessionScope.errorMessage}" /></p>
    </c:if>

    <c:choose>
        <c:when test="${not empty pendingRequests}">
            <table>
                <caption>Pending Leave Requests</caption>
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
                        <td><c:out value="${leaveRequest.status}" /></td>
                        <td>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/manager/approve">
                                <input type="hidden" name="requestId"
                                       value="${leaveRequest.requestId}">
                                <button type="submit">Approve</button>
                            </form>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/manager/reject">
                                <input type="hidden" name="requestId"
                                       value="${leaveRequest.requestId}">
                                <button type="submit">Reject</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>No pending leave requests from your direct reports.</p>
        </c:otherwise>
    </c:choose>
</main>

</body>
</html>
