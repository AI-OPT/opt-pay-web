<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>支付平台</title>
</head>
 <%
 String basePath = request.getScheme() + "://" + request.getServerName() + ":"
         + request.getServerPort() + request.getContextPath();
 String spReturn = basePath+"/nettenpay/spReturn"; //前台通知
 String spNotify = basePath+"/nettenpay/spNotify"; //后台通知
 %>
<body>
服务已启动成功！！
<br><br>
该环境下，一般配置为外网地址
<br><br>
电脑、手机浏览器 <br>
支付地址：<%=basePath%>/pay/choosePlatform
</body>
</html>