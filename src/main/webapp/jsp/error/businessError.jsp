<%@page import="com.ai.runner.base.exception.BusinessException"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!doctype html>
<html>
<head>
<meta charset="UTF-8">
<title>出错啦</title>
<link href="<%=request.getContextPath()%>/ui/css/style.css"
	type="text/css" rel="stylesheet" />
</head>

<body>

	<div class="big_box">
		<div class="box_left">
			<div class="box_left_A">
				<img src="<%=request.getContextPath()%>/ui/images/500.png">
			</div>

			<div class="box_left_B">
				<img src="<%=request.getContextPath()%>/ui/images/dit.png">
			</div>

		</div>
		<div class="box_right">
			<ul>
				<%
				    Object exceptionObj = request.getAttribute("ex");
				    if (exceptionObj != null) {
				        if (exceptionObj instanceof BusinessException) {
				%>
						
						<c:if test="${!empty  ex.errorCode}">
							<li>Sorry,<br> 错误编码:${ex.errorCode}</li>
						</c:if>
						<c:if test="${!empty  ex.errorMessage}">
							<li>错误信息:${ex.errorMessage}</li>
						</c:if>
				<%
				    	}else{
				%>
					<li>由于网络或其他原因，请稍候再试~</li>
				<%
						}
				    } else {
				%>
					<li>由于网络或其他原因，请稍候再试~</li>
				<%
				    }
				%>
				<li style="display: none"><a href="#">返回首页</a></li>
			</ul>


		</div>


	</div>

</body>
</html>
