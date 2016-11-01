<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	String bp = request.getContextPath();
	request.setAttribute("_base", bp);
	
	response.setHeader("Cache-Control","no-cache");   
	response.setDateHeader("Expires",0);   
	response.setHeader("Pragma","No-cache");
	
%>
	<!-- Bootstrap Core CSS -->
    <link href="<%=bp %>/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="<%=bp %>/bower_components/metisMenu/dist/metisMenu.min.css" rel="stylesheet">

    <!-- Timeline CSS -->
    <link href="<%=bp %>/dist/css/timeline.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="<%=bp %>/dist/css/sb-admin-2.css" rel="stylesheet">

    <!-- Morris Charts CSS -->
    <link href="<%=bp %>/bower_components/morrisjs/morris.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="<%=bp %>/bower_components/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
    
	<link rel="stylesheet" href="${_base }/resources/jsoneditor/jsoneditor.min.css">
	
    <!-- jsoneditor -->
	<script src="${_base }/resources/jsoneditor/jsoneditor.min.js"></script>
	<script src="${_base }/resources/jsoneditor/asset/ace/ace.js"></script>
	<script src="${_base }/resources/jsoneditor/asset/jsonlint/jsonlint.js"></script>
	<!-- jQuery -->

	<script src="<%=bp %>/resources/jquery/jquery-2.1.4.min.js" ></script>
	<script src="<%=bp %>/resources/jquery/jquery.metadata.js" ></script>
    <!-- Bootstrap Core JavaScript -->
    <script src="<%=bp %>/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="<%=bp %>/bower_components/metisMenu/dist/metisMenu.min.js"></script>


    <!-- Custom Theme JavaScript -->
    <script src="<%=bp %>/dist/js/sb-admin-2.js"></script>
    
    <!-- 自定义js -->
    <script type="text/javascript" src="${_base }/js/bootbox/bootbox.js"></script>
	<script type="text/javascript" src="${_base }/js/jquery.form.min.js"></script>
	<script type="text/javascript" src="${_base }/js/jquery.pagController.js"></script>