<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport"	content="width=device-width; initial-scale=0.8; maximum-scale=10; user-scalable=0;" />
<link href="${_base}/ui/css/style.css" rel="stylesheet">
<link rel="icon" href="/ui/images/favicon.ico" type="image/x-icon" />
<meta http-equiv="Expires" CONTENT="0"> 
<meta http-equiv="Cache-Control" CONTENT="no-cache"> 
<meta http-equiv="Pragma" CONTENT="no-cache"> 
</head>

<script type="text/javascript">  
  
//处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外  
function banBackSpace(e){     
    var ev = e || window.event;//获取event对象     
    var obj = ev.target || ev.srcElement;//获取事件源     
      
    var t = obj.type || obj.getAttribute('type');//获取事件源类型    
      
    //获取作为判断条件的事件类型  
    var vReadOnly = obj.getAttribute('readonly');  
    var vEnabled = obj.getAttribute('enabled');  
    //处理null值情况  
    vReadOnly = (vReadOnly == null) ? false : vReadOnly;  
    vEnabled = (vEnabled == null) ? true : vEnabled;  
      
    //当敲Backspace键时，事件源类型为密码或单行、多行文本的，  
    //并且readonly属性为true或enabled属性为false的，则退格键失效  
    var flag1=(ev.keyCode == 8 && (t=="password" || t=="text" || t=="textarea")   
                && (vReadOnly==true || vEnabled!=true))?true:false;  
     
    //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效  
    var flag2=(ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea")  
                ?true:false;          
      
    //判断  
    if(flag2){  
        return false;  
    }  
    if(flag1){     
        return false;     
    }     
}  
  
//禁止后退键 作用于Firefox、Opera  
document.onkeypress=banBackSpace;  
//禁止后退键  作用于IE、Chrome  
document.onkeydown=banBackSpace;  
  
</script> 
<body>
	<div class="main_bj">
		<div class="main_center">
			<div class="main_center_list">
				<ul>
					<li class="dag">
						<p>
							<img src="${_base }/ui/images/icon-succ.png">
						</p>
						<p>订单提交成功，请您在跳转的支付平台的页面完成付款!</p>
					</li>
					<li>
						<p>&nbsp;&nbsp;${title }</p>
					</li>
					<li>
						<p>&nbsp;&nbsp;&nbsp;${errorMsg }<p>
					</li>
					<li class="xiaog">
						<p class="mar_zhif">
							<span>订单号：</span><span class="zhif_je">${orderId}</span>
						</p>
						<p>
							<span>应付金额：</span><span class="zhif_je">￥${orderAmount}元</span>
						</p>
					</li>
				</ul>
			</div>
			<div class="tiji_cg_zhif">
				<ul>
					
					<li><c:forEach items="${terminalOrgRelList}" var="org"
							varStatus="i">
							<p>
								<span><input name="psRadio" type="radio"
									value="${org.payOrgCode }"
									<c:if test="${i.index==0}">checked</c:if> class="un_radio">
								</span> <span> <img
									src="${_base }/ui/images/${org.payOrgCode }.png" />
								</span>
							</p>
						</c:forEach></li>
				</ul>
			</div>

			<div class="main_center_dingd_button">
				<ul>
					<!-- 
					<li><A href="#.html">上一步</A></li>
				 -->
					<li><A href="javaScript:popPay();">确认支付</A></li>

				</ul>
			</div>

		</div>


	</div>
</body>
<script type="text/javascript">
	//提交支付
	function popPay() {
		var paytype = $('input:radio[name="psRadio"]:checked').val();
		if (paytype == null) {
			$.dialog.alert("请选择支付方式！！");
			return;
		}

		$('#payOrgCode').val(paytype);
		var url = "${_base}/pay/gotoPay";
		$('#payform').attr("target", "_self");
		$('#payform').attr("action", url);
		$('#payform').submit();
	}
	
</script>
<form action="" name="payform" id="payform" method="post">
	<input type="hidden" name="tenantId" value="${tenantId}" />
	<input type="hidden" name="orderId" value="${orderId}" />
	<input type="hidden" name="payOrgCode" id="payOrgCode" value="" />
</form>
</html>
