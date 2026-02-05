 <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login & Registration</title>
</head>
<body>
<h1> "Hello World!" </h1>
<br/>
<form action="controller" method="get">
    <input type="hidden" name="command" value="login" id="command"/>
    <h3>Login Form</h3>
    Login: <input type="text" name="login" value=""/>
    <br/>
    Password: <input type="password" name="pass" value=""/>
    <br/>
    <input type="submit" name="sub" value="Log in"/>
    <br/>
    ${login_msg.toUpperCase()}
</form>
<hr/>
<form action="controller" method="post">
    <input type="hidden" name="command" value="add_user"/>
    <h3>Registration Form</h3>
    Login: <label>
    <input type="text" name="login" value=""/>
</label>
    <br/>
    Password: <input type="password" name="pass" value=""/>
    <br/>
    <input type="submit" value="Register"/>
    <br/>
    ${register_msg.toUpperCase()}
</form>
<br/>
<br/>
Session ID: ${pageContext.session.id}
<br/>
Filter attribute: ${filter_attr}
<br/>
User: ${user_name}


</body>
</html>
<%-- <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1> "Hello World!" </h1>
<br/>
<form action="controller">
    <input type="hidden" name="command" value="login"/>
    Login: <input type="text" name="login" value=""/>
    <br/>
    Password: <input type="password" name="pass" value=""/>
    <br/>
    <input type="submit" name="sub" value="Push"/>
    <br/>
    ${login_msg.toUpperCase()}
    <br/>
    ${pageContext.session.id}
    <br/>
    ${filter_attr}
</form>

</body>
</html> --%>