<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<ul class="nav nav-tabs">
	<li class="active"><a href="#profile" data-toggle="tab">支付宝</a></li>
	<li><a href="#messages" data-toggle="tab">微信</a></li>
	<li><a href="#settings" data-toggle="tab">银联</a></li>
</ul>

<!-- Tab panes -->
<div class="tab-content">
	
	<div class="tab-pane fade in active" id="profile">
		<div class="tab-pane fade in active" id="home_1">
		<div class="form-group" style="margin-top: 20px"
			name="DIV_REQ_PARAM_SETTING_1">
			<label class="control-label">支付宝：</label>
			<textarea class="form-control" rows="6">${ZFB.configInfo }</textarea>
			
		</div>
		<button type="button" id="submit_btn_1" class="btn btn-default">提交</button>
		<button type="reset" id="reset_btn_1" class="btn btn-default">重置</button>
	</div>
	</div>
	<div class="tab-pane fade" id="messages">
		<div class="tab-pane fade in active" id="home_2">
		<div class="form-group" style="margin-top: 20px"
			name="DIV_REQ_PARAM_SETTING_2">
			<label class="control-label">微信：</label>
			<textarea class="form-control" rows="6">${WEIXIN.configInfo }</textarea>
		</div>
		<button type="button" id="submit_btn_2" class="btn btn-default">提交</button>
		<button type="reset" id="reset_btn_2" class="btn btn-default">重置</button>
	</div>
	</div>
	<div class="tab-pane fade" id="settings">
		<div class="tab-pane fade in active" id="home_3">
		<div class="form-group" style="margin-top: 20px"
			name="DIV_REQ_PARAM_SETTING_3">
			<label class="control-label">银联：</label>
			<textarea class="form-control" rows="6">${YL.configInfo }</textarea>
			
		</div>
		<button type="button" id="submit_btn_3" class="btn btn-default">提交</button>
		<button type="reset" id="reset_btn_3" class="btn btn-default">重置</button>
	</div>
	</div>
</div>
