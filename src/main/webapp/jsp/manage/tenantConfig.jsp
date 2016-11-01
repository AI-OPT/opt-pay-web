<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<%@include file="/jsp/common/common.jsp"%>
<title>支付中心管理后台</title>
<script>
	var pageController;
	$(document).ready(function() {
		pageController = new $.PageController();
	});

	(function($) {

		$.PageController = function() {
			this.settings = $.extend(true, {}, $.PageController.defaults);
			this.bindEvents();
			//this.initJSONEditors();
		}

		$.extend($.PageController, {
			defaults : {

			},
			prototype : {

				bindEvents : function() {
					var _this = this;
					
				},
				validate : function() {
					
				},
				initJSONEditors : function(num,paramjson) {
					var options = {
						mode : 'code',
						modes : [ 'code', 'form', 'text', 'tree', 'view' ],
						error : function(err) {
							alert(err.toString());
						}
					};
					//输入参数编辑器
					var reqEditors = new Array();
					$("[name='DIV_REQ_PARAM_SETTING_"+num+"']").each(
							function(index, div) {
								var obj = $(div).find("#REQ_JSONEDITOR_"+num);
								if (!obj) {
									return;
								}

								var jsoneditorId = obj.attr("id");
								if (jsoneditorId == undefined) {
									return;
								}
								//数据库中保存的json字符串
								//alert(paramjson);
								//var paramjson = obj.attr("paramjson");
								var container = document.getElementById(jsoneditorId);
								var editor = new JSONEditor(container, options,paramjson);
								//editor.set(paramjson);
								reqEditors.push(editor);
							});

					this.reqEditors = reqEditors;
				},
				showJSONContent : function(tenantId) {
					//alert(tenantId);
					var _this = this;
					$.ajax({
						async : false,
						type : "POST",
						url : "${_base}/manage/tenantConfigFORM",
						modal : true,
						showBusi : false,
						data : "tenantId="+ tenantId,
						success : function(data) {
							$("#content").html(data);
						},
						error:function(data){
							messageController.alert("error");
						}
					});
				},
				modifyAlipayTenantConfigFORM : function() {
					var _this = this;
					
					var param = $("#ZFBForm").serialize();
					$.ajax({
						async : false,
						type : "POST",
						url : "${_base}/manage/modifyAlipayTenantConfigFORM",
						modal : true,
						showBusi : false,
						data : param,
						success : function(data) {
							if("SUCCESS"==data.trim()){
								messageController.alert("修改成功");
							}else{
								messageController.alert("修改失败");
							}
						},
						error:function(data){
							messageController.alert("error");
						}
					});
	
				},
				modifyWeixinTenantConfigFORM : function() {
					var _this = this;
					
					var param = $("#WEIXINForm").serialize();
					$.ajax({
						async : false,
						type : "POST",
						url : "${_base}/manage/modifyWeixinTenantConfigFORM",
						modal : true,
						showBusi : false,
						data : param,
						success : function(data) {
							if("SUCCESS"==data.trim()){
								messageController.alert("修改成功");
							}else{
								messageController.alert("修改失败");
							}
						},
						error:function(data){
							messageController.alert("error");
						}
					});
	
				},
				modifyYlTenantConfigFORM : function() {
					var _this = this;
					
					var param = $("#YLForm").serialize();
					$.ajax({
						async : false,
						type : "POST",
						url : "${_base}/manage/modifyYlTenantConfigFORM",
						modal : true,
						showBusi : false,
						data : param,
						success : function(data) {
							if("SUCCESS"==data.trim()){
								messageController.alert("修改成功");
							}else{
								messageController.alert("修改失败");
							}
						},
						error:function(data){
							messageController.alert("error");
						}
					});
	
				},
				modifyCommonTenantConfigFORM : function() {
					var _this = this;
					
					var param = $("#CommonForm").serialize();
					$.ajax({
						async : false,
						type : "POST",
						url : "${_base}/manage/modifyCommonTenantConfigFORM",
						modal : true,
						showBusi : false,
						data : param,
						success : function(data) {
							if("SUCCESS"==data.trim()){
								messageController.alert("修改成功");
							}else{
								messageController.alert("修改失败");
							}
						},
						error:function(data){
							messageController.alert("error");
						}
					});
	
				}
			}

		});

	})(jQuery);
</script>

</head>

<body>

	<div id="wrapper">

		<%@include file="/jsp/common/nav.jsp"%>

		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h2 class="page-header">Config</h2>
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->

			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-info">
						<div class="panel-heading">
							<p class="fa fa-wrench">&nbsp;租户信息配置</p>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="form-group" style="margin-top: 5px">
								<label class=" control-label">请选择租户：</label> <select
									id="tenantList" class="form-control" style="width: 20%"
									onchange="pageController.showJSONContent(this.value);">
									<c:forEach items="${tenantList}" var="tenant">
										<option value="${tenant.tenantId}">${tenant.tenantName}</option>
									</c:forEach>
								</select>
							</div>
							<div class="form-group" style="display:none">
                                <label>请选择配置类型:</label>
                                <div class="radio">
                                    <label>
                                        <input type="radio" name="formType" id="formType1" value="${_base}/manage/tenantConfigFORM" checked> 表单
                                    </label>
                                </div>
                                <div class="radio">
                                    <label>
                                        <input type="radio" name="formType" id="formType2" value="${_base}/manage/tenantConfigJSON">JSON串
                                    </label>
                                </div>
                                
                            </div>
							<!-- Nav tabs -->
							<div id="content"></div>
						</div>
						<!-- /.panel-body -->
					</div>
					<!-- /.panel -->
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
		</div>
		<!-- /#page-wrapper -->

	</div>
	<!-- /#wrapper -->

</body>

</html>
