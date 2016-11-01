<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<ul class="nav nav-tabs">
	<li class="active"><a href="#profile" data-toggle="tab">支付宝</a></li>
	<li><a href="#messages" data-toggle="tab">微信</a></li>
	<li><a href="#settings" data-toggle="tab">银联</a></li>
	<li><a href="#common" data-toggle="tab">基础配置</a></li>
</ul>

<!-- Tab panes -->
<div class="tab-content">

	<div class="tab-pane fade in active" id="profile">
		<div class="tab-pane fade in active" id="home_1">
			<form name="ZFBForm" id="ZFBForm" method="post">
				<div class="form-group" style="margin-top: 20px">
					<input type="hidden" name="tenantId" id="tenantId"
						value="${tenantId}" /> <label class="control-label">支付宝账号（WEB）</label>
					<input class="form-control" id="webSellerEmail"
						name="webSellerEmail" value="${ZFB.web_seller_email}" /> <label
						class="control-label">商家PID（WEB）</label> <input
						class="form-control" id="webSellerPID" name="webSellerPID"
						value="${ZFB.web_seller_PID}" /> <label class="control-label">商家秘钥（WEB）</label>
					<input class="form-control" id="webSellerKey" name="webSellerKey"
						value="${ZFB.web_seller_key}" /> <label class="control-label">支付宝账号（WAP）</label>
					<input class="form-control" id="wapSellerEmail"
						name="wapSellerEmail" value="${ZFB.wap_seller_email}" /> <label
						class="control-label">商家PID（WAP）</label> <input
						class="form-control" id="wapSellerPID" name="wapSellerPID"
						value="${ZFB.wap_seller_PID}" /> <label class="control-label">商家秘钥（WAP）</label>
					<input class="form-control" id="wapSellerKey" name="wapSellerKey"
						value="${ZFB.wap_seller_key}" /><label class="control-label">支付宝账号（批量付款）</label>
					<input class="form-control" id="batchTransSellerEmail"
						name="batchTransSellerEmail" value="${ZFB.batch_trans_seller_email}" />
						<label class="control-label">付款账户名称（批量付款）</label>
					<input class="form-control" id="batchTransAcctName"
						name="batchTransAcctName" value="${ZFB.batch_trans_acct_name}" /> <label
						class="control-label">商家PID（批量付款）</label> <input
						class="form-control" id="batchTransSellerPID" name="batchTransSellerPID"
						value="${ZFB.batch_trans_seller_PID}" /> <label class="control-label">商家秘钥（批量付款）</label>
					<input class="form-control" id="batchTransSellerKey" name="batchTransSellerKey"
						value="${ZFB.batch_trans_seller_key}" />
				</div>
				<button type="button" id="submit_btn_1"
					onclick="javascript:pageController.modifyAlipayTenantConfigFORM();"
					class="btn btn-default">提交</button>
				<button style="display: none" type="reset" id="reset_btn_1"
					class="btn btn-default">重置</button>
			</form>
		</div>
	</div>
	<div class="tab-pane fade" id="messages">
		<div class="tab-pane fade in active" id="home_2">
			<form name="WEIXINForm" id="WEIXINForm" method="post">
				<div class="form-group" style="margin-top: 20px"
					name="DIV_REQ_PARAM_SETTING_2">
					<input type="hidden" name="tenantId" id="tenantId"
						value="${tenantId}" /> <label class="control-label">APPID</label>
					<input class="form-control" id="weixinAppid" name="weixinAppid"
						value="${WEIXIN.WEIXIN_APPID}" /> <label class="control-label">MCH_ID</label>
					<input class="form-control" id="weixinMchId" name="weixinMchId"
						value="${WEIXIN.WEIXIN_MCH_ID}" /> <label class="control-label">APPSECRET</label>
					<input class="form-control" id="weixinAppsecret"
						name="weixinAppsecret" value="${WEIXIN.WEIXIN_APPSECRET}" /> <label
						class="control-label">API_KEY</label> <input class="form-control"
						id="weixinApiKey" name="weixinApiKey"
						value="${WEIXIN.WEIXIN_API_KEY}" /> <label class="control-label">CER_PATH</label>
					<input class="form-control" id="weixinCerPath" name="weixinCerPath"
						value="${WEIXIN.WEIXIN_CER_PATH}" />
				</div>
				<button type="button" id="submit_btn_2"
					onclick="javascript:pageController.modifyWeixinTenantConfigFORM();"
					class="btn btn-default">提交</button>
				<button style="display: none" type="reset" id="reset_btn_2"
					class="btn btn-default">重置</button>
			</form>
		</div>
	</div>
	<div class="tab-pane fade" id="settings">
		<div class="tab-pane fade in active" id="home_3">
			<form name="YLForm" id="YLForm" method="post">
				<div class="form-group" style="margin-top: 20px"
					name="DIV_REQ_PARAM_SETTING_3">
					<input type="hidden" name="tenantId" id="tenantId"
						value="${tenantId}" /> <label class="control-label">商户ID</label> <input
						class="form-control" id="merId" name="merId" value="${YL.merId}" /> 
						<label class="control-label">签名证书路径</label> 
						<input class="form-control" id="signCertPath" name="signCertPath" value="${YL.signCertPath}" />
						<label class="control-label">签名证书密码</label> 
						<input class="form-control" id="signCertPwd" name="signCertPwd" value="${YL.signCertPwd}" />						
				</div>
				<button type="button" id="submit_btn_3"
					onclick="javascript:pageController.modifyYlTenantConfigFORM();"
					class="btn btn-default">提交</button>
				<button style="display: none" type="reset" id="reset_btn_3"
					class="btn btn-default">重置</button>
			</form>
		</div>
	</div>
	<div class="tab-pane fade" id="common">
		<div class="tab-pane fade in active" id="home_4">
			<form name="CommonForm" id="CommonForm" method="post">
				<div class="form-group" style="margin-top: 20px"
					name="DIV_REQ_PARAM_SETTING_4">
					<input type="hidden" name="tenantId" id="tenantId"value="${tenantId}" /> 
					<label class="control-label">商户秘钥</label> 
					<input class="form-control" id=""requestKey"" name="requestKey" value="${common.request_key}" />

				</div>
				<button type="button" id="submit_btn_4"
					onclick="javascript:pageController.modifyCommonTenantConfigFORM();"
					class="btn btn-default">提交</button>
				<button style="display: none" type="reset" id="reset_btn_4"
					class="btn btn-default">重置</button>
			</form>
		</div>
	</div>
</div>
