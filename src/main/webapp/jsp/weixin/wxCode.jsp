<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String _base = request.getContextPath();
	
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>微信支付</title>
	<script type="text/javascript" src="<%=_base%>/js/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript" src="<%=_base%>/js/jquery/lhgdialog/lhgdialog.min.js?self=true&skin=chrome"></script>
	<script type="text/javascript" src="<%=_base%>/js/qrcode.js"></script>
    <link href="<%=_base%>/ui/css/style.css" rel="stylesheet"> 
    <script type="text/javascript" charset="utf-8">
 	$(document).ready(function() {
 		//开启定时器，扫订单状态，成功则直接回调
	 	var t1 = window.setInterval("queryOrderState()",5000);  
	 	//显示二维码
	 	var qrcode_c = document.getElementById("qrcode_c");  
	 	if(qrcode_c){    
	 	    var qrcode = new QRCode(qrcode_c, {  
	 	        width : '300',  
	 	        height : '300'  
	 	    });  
	 	    qrcode.makeCode("${codeurl}");  
	 	}  
 	});
 	function queryOrderState(){
 		$.ajax({
			async : false,
			type : "POST",
			url : "<%=_base%>/query/tradeQuery",
			modal : true,
			showBusi : false,
			data : {
				orderId:'${orderId}',
				tenantId:'${tenantId}',
				infoMd5:'${infoMd5}'
			},
			dataType: "text",
			success : function(data) {
				var json = jQuery.parseJSON(data);
				if(json.tradeStatus == "00"){
					window.location.href="<%=_base%>/weixin/wxReturn?data=" + data;
				}
			},
			error:function(data){
				$.dialog.alert("数据请求失败！");
			}
		});
    }
 	function history(){
 		$("#returnForm").submit();
    }
 	
 	</script>
</head>

<body style="background:#F1F2F7;">
 <div class="box">
  <div class="weixin">
   <div class="weixin_title">微信支付</div>
   <div class="wx_left">
    <div class="wx_tp" id="qrcode_c"></div>
    <div class="wx_bt">
     <p>请使用微信扫一扫</p>
     <p>扫描二维码支付</p>
    </div>
   </div>
   <div class="wx_right"></div>
   <div class="wx_more"><a href="javascript:history()">< 选择其它支付方式</a></div>
   <form name="returnForm" id="returnForm" method="post" action="<%=_base%>/pay/choosePlatform">
    <input type="hidden" name="tenantId" value="${tenantId }"/>
   	<input type="hidden" name="orderId" value="${orderId }"/>
   	<input type="hidden" name="subject" value="${subject }"/>
   	<input type="hidden" name="notifyUrl" value="${notifyUrl}"/>
   	<input type="hidden" name="orderAmount" value="${orderAmount/1000 }"/>
   	<input type="hidden" name="returnUrl" value="${returnUrl }"/>
   	<input type="hidden" name="checkFlag" value="${checkFlag }"/>
   	<input type="hidden" name="requestSource" value="${requestSource }"/>
   	<input type="hidden" name="merchantUrl" value="${merchantUrl }"/>
   	<input type="hidden" name="payChannel" value="${payChannel }"/>
   	<input type="hidden" name="infoMd5" value="${infoMd5 }"/>
   </form>
  </div>
 </div>
</body>

</html>
