<%--
  Created by IntelliJ IDEA.
  User: evgeniy
  Date: 03.01.2026
  Time: 13:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Main</title>
</head>
<body>
Hello (forward) = ${user}
<hr/>
Hi (redirect/forward) = ${user_name}
<hr/>
<hr>
${filter_attr}
<hr/>
<form action="controller">
    <input type="hidden" name="command" value="logOut"/>
    <input type="submit" value="Log out"/>
</form>
</body>
</html>
