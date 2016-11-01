<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String bp = request.getContextPath();
	request.setAttribute("_base", bp);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>电信</title>
<meta http-equiv="pragma" content="no-cache" />
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="expires" content="0" />
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3" />
<meta http-equiv="description" content="This is my page" />
<link href="<%=bp%>/ui/css/global.css?dasdsa" rel="stylesheet" type="text/css" />
<link href="<%=bp%>/ui/css/iframe.css?1232" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=bp%>/js/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="<%=bp%>/js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
<script type="text/javascript" src="<%=bp%>/js/menu.js?xx"></script>
<script type="text/javascript">
</script>
</head>
<body class="cifco_bg" style="overflow-x:hidden;" >
<div class="head">
  <div class="head_menu">
    <div class="companyLogo">
		<a href="#">电信</a>
	</div>
    <div class="headright">
    	<ul class="headOther">
	      <li class="common_win userinfo">
	        <h3><a href="#">${sessionScope.CRM_SESSION_OPER.gnOperVo.operCode }<span></span></a></h3>
	      </li>
	      <li class="notice">
	      	<a href="#">通知</a>
	        <div class="notice_nm"><span>4</span></div>
	      </li>
	      <li class="logout">
	        <a href="${_base}/logout.jsp">退出系统</a>
      	  </li>
    	</ul>
    </div>
  </div>
  <!--二级导航-->
  <div class="navigation">
    <ul id="menuone">
      <!--li class="nav_prev">
      	<h2><a href="#">上一个</a></h2>
      </li-->
	  <li class="common_nav xiaoshou1">
        <h2 class="select"><a href="#">销售</a></h2>
      </li>
      <li class="common_nav service1">
        <h2><a href="#">服务</a></h2>
      </li>
     <!-- 
      <li class="common_nav share1">
        <h2><a href="#">共享用户管理</a></h2>
      </li>
       -->
      <li class="common_nav order1">
        <h2><a href="#">订单管理</a></h2>
      </li>
      <li class="common_nav pay1">
        <h2><a href="#">账务管理</a></h2>
      </li>
    <!--   <li class="common_nav">
        <h2><a href="#">理商费用管理</a></h2>
      </li> -->
      <li class="common_nav yyrb1">
        <h2><a href="#">营业日报</a></h2>
      </li>
      <li class="common_nav system1">
        <h2><a href="#">系统管理</a></h2>
      </li>
    <!--   <li class="common_nav audit1">
        <h2><a href="#">收入归集</a></h2>
      </li>
      <li class="common_nav channel1">
        <h2><a href="#">渠道管理</a></h2>
      </li>-->
      <!--li class="nav_next">
        <h2><a href="#">下一个</a></h2>
      </li-->
    </ul>
  </div>
</div>
<div class="container"   >
	<!--左边菜单-->
	<div class="con_side">
		<div class="con_side_top"></div>
	  	<ul id="menutwo">
	  		<li class="xiaoshou" id="menu1"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/orderNumbers/toMainPage');return false;">号码订购</a></li>
	  		<li class="xiaoshou" id="menu1"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/subsUser/cAccountStep1');return false;">开户</a></li>
	  		<li class="xiaoshou" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/subsUserBatch/tocAccountBatchStep1');return false;">批量开户</a></li>
	  		<li class="xiaoshou" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/subsUserBatch/tocAccountBatchPreStep1');return false;">批量预开户</a></li>
	  		<li class="xiaoshou" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/enterpriseGroup/memberBatch');return false;">集团成员维护</a></li>
	  		<li class="xiaoshou" id="menu2"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/resourceOrder/mainDisplay');return false;">企业资源量批发</a></li>
	  		<li class="xiaoshou" id="menu2"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/person_resourceOrder/mainDisplay');return false;">个人资源量批发</a></li>
	  		<li class="xiaoshou" id="menu3"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/resourceOrder/searchResources');return false;">资源量批发查询</a></li>
	  	    <li class="xiaoshou" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/ordApplyBatch/toBatchList');return false;">批量业务查询</a></li>
	  	    <li class="xiaoshou" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/subsBatchPay/cAccountBatchPay');return false;">批量支付</a></li>
	  		
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/orderList');return false;">订单查询</a></li>
		    <li class="order" ><a href="javascript:void(0)"onclick="javascript:loadMenuUrl('${_base }/order/handleList');return false;">网厅订单处理</a></li>
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/acceptList');return false;">营业厅订单处理</a></li>
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/autoList');return false;">用户自提</a></li>
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/payBackList');return false;">退费处理</a></li>
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/sos/toList');return false;">服务开通查询</a></li>
		   	<li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/ticket/ticketRetypeList');return false;">票据重打</a></li>
		    <li class="order" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/order/logistics/logisticsReconcile');return false;">物流对账</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/query');return false;">缴费</a></li>		    
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/refund');return false;">退费</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/give_prestore_1');return false;">赠送预存</a></li> 
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/prebill_adjust_1');return false;">账前调账</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/billQuery');return false;">账单查询和打印</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/arrearsQuery');return false;">欠费查询</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/paymentLog');return false;">缴费记录查询</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/pay/reverseApplyId');return false;">缴费冲正</a></li>
			 <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/invoice/invoiceReprintBd');return false;">发票补打</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/invoice/invoiceReprintCd');return false;">发票重打</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/billDetail/bill_detail');return false;">详单查询</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/refuseReturn/refuseQuery');return false;">妥投转拒收退费</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/print/center/toTemplateList');return false;">发票模板维护</a></li>
		    <li class="pay"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/checkBill/checkBillQuery');return false;">网付通对账查询</a></li>
		    
		    
		    
		    
		    
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/cust/initCustPersonalReg');return false;">个人客户注册</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/custCompany/initCustCompanyReg');return false;">单位客户注册</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/cust/initCustChg');return false;">客户资料变更</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/sale/orderProducts/toOrderProduct');return false;">产品订购</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/comprehensiveQuery/mainDisplay');return false;">综合查询(中期)</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/comprehensiveQuery_gm/mainDisplay');return false;">综合查询(国美)</a></li>	   
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/product/toProductChange');return false;">产品变更</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/safekeepingopen/toSafeKeepingOpen');return false;">封顶解封</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/contractPostponed/toContractPostponed');return false;">合约顺延</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/changeCard/mainDisplay');return false;">补换卡</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/subsuser/close/toPlanClose');return false;">预销户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/subsuser/close/toCancelPlanClose');return false;">取消预销户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/close/toCancelAccount');return false;">正式销户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/forceCloseAccountController/toCloseAccount');return false;">强制销户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/forceCloseAccountController/toTtCloseAccount');return false;">妥投转拒销户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/cust/transfer/toTransferCust');return false;">过客户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/change/toMainNumberChg');return false;">主号码变更</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/account/transfer/toTransferAccount');return false;">过账户</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/changePe/changeSpecialService/toChangeSpecialService');return false;">呼叫转移</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/chg/changeTrade');return false;">停开机</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/chg/forceChangeTrade');return false;">强停强开</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/chg/changeSubPassWord');return false;">服务密码变更</a></li>	    
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/cust/acct/toAcctChgPage');return false;">账户资料修改</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/cust/initCustChg');return false;">客户资料修改</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/terminalChange/mainDisplay');return false;">终端换机</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/changePe/openAndClose/toOpenAndClosePage');return false;">业务停开</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/globalRoamingDialingController/toGlobalRoamingDialing');return false;">国际漫游国际长途</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/subsCredit/mainDisplay');return false;">免催免停设置</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busichg/subsCredit/cancelMainDisplay');return false;">取消免催免停</a></li>
		    <li class="service"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/custComprehensive/servicePwdChangePager');return false;">服务密码变更</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/demo/hello');return false;">控件示例</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/demo/list');return false;">增删改查示例</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('http://www.qq.com/');return false;">增删改查示例</a></li>
		    
		     <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">员工管理</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">角色管理</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">权限管理</a></li>
		     <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">批量导入员工</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">批量导入角色</a></li>
		    <li class="system"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('#');return false;">批量导入权限</a></li>
		    
		    <li class="audit"><a href="javascript: void(0)" onclick="javascript:loadMenuUrl('${_base }/audit/financialApproval');return false;">财务审批</a></li>
		    <li class="audit"><a href="javascript: void(0)" onclick="javascript:loadMenuUrl('${_base }/audit/auditQuery'); return false;">收入归集查询</a></li>
		    
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/sharedmember/toSharedMember');return false;">共享成员变更</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/productChange/mainDisplay');return false;">共享产品变更</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/sbh/toSbh');return false;">共享业务办理</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/wfsbh/toSbh');return false;">极享家庭套餐办理</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/wHomeProductChange/mainDisplay');return false;">极享家庭套餐产品变更</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/wHomeSharedMember/toSharedMember');return false;">极享家庭套餐成员变更</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/familyBusiCheck/toFamilyBusinessCheck');return false;">极家业务办理</a></li>
		    <li class="share"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/group/familyMemberChange/toFamilyMemberChange');return false;">极家成员变更</a></li>
		    
		    <li class="yyrb"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busiDailyReport/toManageDailyList');return false;">管理类日报</a></li>
		    <li class="yyrb"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busiDailyReport/mgrDetailsQuery');return false;">管理类明细查询</a></li>
		    <li class="yyrb"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busiDailyReconciliation/toDailyReconciliation?queryData=no');return false;">营业员日扎帐</a></li>
		    <li class="yyrb"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/promoteConsume/toPromotionfeeList?queryData=no');return false;">促销费查询</a></li>
		      <li class="yyrb"><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/busiDailyAudit/queryAuditReports');return false;">稽核报表查询</a></li>
	  	
	  		<li class="channel" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/channel/channelBusi/toChannelCreate');return false;">渠道资料录入</a></li>
	  		<li class="channel" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/channel/channelBusi/toChannelQuery?from=2');return false;">渠道资源查询</a></li>
	  		<li class="channel" ><a href="javascript:void(0)" onclick="javascript:loadMenuUrl('${_base }/channel/channelBusi/toChannelQuery?from=1');return false;">发展人维护</a></li>
	  	</ul>
		<div class="con_side_bottom"></div>
	</div>
	
	<div class="con_main">
            <iframe src="" name="main_iframe" allowtransparency="transparent" 
										 height="100%" marginheight="0" align="middle" marginwidth="0" width="100%"
										scrolling="auto" frameborder="0" style="min-height: 460px;"
										id="main_iframe"
										 ></iframe>
	</div>
</div>
</body>
<script language="javascript" type="text/javascript"> 

function loadMenuUrl(url){
	dynaHeight(url);
	//$("#main_iframe").attr("src",url);
}
function dyniframesize(down) { 
	
	 // var iframeHeight = $("#"+down).contents().height();
	  //alert(iframeHeight);
var pTar = null; 
if (document.getElementById){ 
pTar = document.getElementById(down); 
} 
else{ 
eval('pTar = ' + down + ';'); 
} 
var heigth = 600;
if (pTar && !window.opera){ 
//begin resizing iframe 
pTar.style.display="block" 
if (pTar.contentDocument && pTar.contentDocument.body.offsetHeight){ 
//ns6 syntax 
heigth = pTar.contentDocument.body.offsetHeight +20; 
//pTar.width = pTar.contentDocument.body.scrollWidth+20; 
} 
else if (pTar.Document && pTar.Document.body.scrollHeight){ 
//ie5+ syntax 
heigth = pTar.Document.body.scrollHeight; 
//pTar.width = pTar.Document.body.scrollWidth; 
}
}
alert(heigth);
if(iframeHeight<1000)
	iframeHeight = 1000;
pTar.height = iframeHeight;
} 
$(document).ready(function(){
	$("#menutwo").find("li").each(function(){
		$(this).hide();
	});
	$(".xiaoshou").show();
});



//iframe高度
function dynaHeight(url){
	//alert("123:||"+(navigator.appName == "Microsoft Internet Explorer")+"  "+navigator.appName );
	var　iframe　=　$('#main_iframe');
	iframe.attr('_name_',iframe.attr('name'))　 　　　//备份原来的　name
	.attr('src',url)　　　　　　　　　　　 　　　　　　　　　　　　　　　　　//设置URL
	.one ('load',function　()　{
		var　msg 　=　this.contentWindow.name;　　　　　　　　　　　　//得到值　这个值就是高度了
		try
		{
		　　　　var 　height　=　eval(msg);　　　　　　　　　　　　　　　　　　　　//得到并设置高度
			 if(height<600)
				 height = 1200;
		　　　 　iframe.css('height',　height　+　'px');
		}
		catch(e)
		{
		　　　　alert('目标页面没有设置高度到　 window.name');
		}　　　　
		
　　　　	})
}
</script> 
</html>