<!DOCTYPE html>
<html>
<head>
    <title>Employee Case Study Tracker</title>
</head>

<body>

    <h1>Employee Case Study Tracker</h1>

    <h2>Login</h2>

    <form method="post" action="${pageContext.request.contextPath}/login">

        <label>Employee ID:</label>
        <input type="text" name="empId">

        <br><br>

        <button type="submit">Login</button>

    </form>

</body>
</html>