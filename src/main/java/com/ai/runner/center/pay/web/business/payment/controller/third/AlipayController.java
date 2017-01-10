package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.opt.sdk.util.CollectionUtil;
import com.ai.opt.sdk.util.DateUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.controller.core.TradeBaseController;
import com.ai.runner.center.pay.web.business.payment.model.BatchRefundRes;
import com.ai.runner.center.pay.web.business.payment.model.BatchWithdrawReqParam;
import com.ai.runner.center.pay.web.business.payment.model.CommonPayRes;
import com.ai.runner.center.pay.web.business.payment.model.RefundRes;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentNotifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.ResponseUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.alipay.AlipayReturnParser;
import com.ai.runner.center.pay.web.business.payment.util.third.alipay.AlipayReturnParser.RefundDealItem;
import com.ai.runner.center.pay.web.business.payment.util.third.alipay.AlipaySubmit;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.AliPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants.PayOrgCode;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigFromFileUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.XMLUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;

/**
 * alipay controller
 *
 * Date: 2015年11月3日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/alipay")
public class AlipayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(AlipayController.class);
    
    /** web支付前台通知地址 **/
    private static final String WEB_RETURN_URL = "/alipay/webReturn";

    /** web支付后台通知地址 **/
    private static final String WEB_NOTIFY_URL = "/alipay/webNotify";

    /** wap支付前台通知地址 **/
    private static final String WAP_RETURN_URL = "/alipay/wapReturn";

    /** wap支付后台通知地址 **/
    private static final String WAP_NOTIFY_URL = "/alipay/wapNotify";
    private static final String WAP_NOTIFY_URL_201611 = "/alipay/wapNotify201611";

    /** app支付后台通知地址 **/
    private static final String APP_NOTIFY_URL = "/alipay/appNotify";

    /** 退款后台通知地址 **/
    private static final String REFUND_NOTIFY_URL = "/alipay/noPwdRefundNotify";
    
    /** 批量退款后台通知地址 **/
    private static final String BATCH_REFUND_NOTIFY_URL = "/alipay/batchNoPwdRefundNotify";
    
    /** 支付宝批量付款到支付宝账户后台通知地址 **/
    private static final String BATCH_TRANS_NOTIFY_URL = "/alipay/batchTransNotify";
    
    /**
     * 支付宝WEB即时到账交易接口
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/webPayment/alipayapi")
    public void alipayWebPay(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            LOG.info("支付宝WEB支付开始:商户订单号[" + orderId + "]" + " ，租户标识： " + tenantId);
            String infoMd5 = (String) request.getAttribute("infoMd5");   
            if(StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if(!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            
            String basePath = AbstractPayConfigManager.getPayUrl();
            LOG.info("项目根路径： " + basePath);
            //组织支付参数            
            String payment_type = "1"; //支付类型,必填，不能修改
            //服务器异步通知页面路径
            String notify_url = basePath + WEB_NOTIFY_URL;
            //需http://格式的完整路径，不能加?id=123这类自定义参数     //页面跳转同步通知页面路径
            String return_url = basePath + WEB_RETURN_URL;
            //需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/       
            String seller_email = ConfigUtil.getProperty(tenantId,
                    AliPayConfigManager.PAY_ORG_NAME, AliPayConfigManager.WEB_SELLER_EMAIL); // 卖家支付宝帐户
            String partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_PID);
            String seller_key = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_KEY);
            String out_trade_no = tradeRecord.getTradeOrderId(); //商户订单号
            LOG.info("支付宝WEB支付开始:交易订单号[" + out_trade_no + "]");
            String subject = "网上支付";
            if(!StringUtil.isBlank(tradeRecord.getSubject())) {
                subject = tradeRecord.getSubject();
            }
            String total_fee = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额      
            String body =  null; //订单描述
            String show_url = null; //商品展示地址
            //需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html      //防钓鱼时间戳
            String anti_phishing_key = "";
            //若要使用请调用类文件submit中的query_timestamp函数       
            //客户端的IP地址
            String exter_invoke_ip = "";
            //非局域网的外网IP地址，如：221.0.0.1           
            
            //把请求参数打包成数组
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("service", "create_direct_pay_by_user");
            sParaTemp.put("partner", partner);
            sParaTemp.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
            sParaTemp.put("payment_type", payment_type);
            sParaTemp.put("notify_url", notify_url);
            sParaTemp.put("return_url", return_url);
            sParaTemp.put("seller_email", seller_email);
            sParaTemp.put("out_trade_no", tenantId + "_" + out_trade_no);
            sParaTemp.put("subject", subject);
            sParaTemp.put("total_fee", total_fee);
            sParaTemp.put("body", body);
            sParaTemp.put("show_url", show_url);
            sParaTemp.put("anti_phishing_key", anti_phishing_key);
            sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
            //建立请求
            String sHtmlText = AlipaySubmit.buildRequest(seller_key,
                    AliPayConfigManager.ALIPAY_GATEWAY_NEW, sParaTemp, "get", "确认");
            LOG.info("向支付宝WEB即时到账交易接口发起支付请求：" + sHtmlText);
            printWriter.println(sHtmlText);
            printWriter.flush();
            printWriter.close();
        } catch(IOException ex) {
            LOG.error("支付宝网页支付发生错误", ex);
            throw ex;
        } catch(Exception ex) {
            LOG.error("支付宝网页支付发生错误", ex);
            throw ex;
        } 
    }
    
    private void showParams(HttpServletRequest request) {  
        Map<String, String> map = new HashMap<String, String>();  
        Enumeration<?> paramNames = request.getParameterNames();  
        while (paramNames.hasMoreElements()) {  
            String paramName = (String) paramNames.nextElement();  
  
            String[] paramValues = request.getParameterValues(paramName);  
            if (paramValues.length == 1) {  
                String paramValue = paramValues[0];  
                if (paramValue.length() != 0) {  
                    map.put(paramName, paramValue);  
                }  
            }  
        }  
  
        Set<Map.Entry<String, String>> set = map.entrySet();  
        LOG.info("------------------------------");  
        for (Map.Entry<String, String> entry : set) {  
            LOG.info(entry.getKey() + ":" + entry.getValue());  
        }  
        LOG.info("------------------------------");  
    }
    
    /**
     * 支付宝WEB即时到账后台通知服务
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/webNotify")
    public void alipayWebNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("支付宝WEB后台通知...");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取支付宝传递过来的参数 */
            String subject = request.getParameter("subject");// 商品名称
            String trade_no = request.getParameter("trade_no"); // 支付宝交易号
            String buyer_email = request.getParameter("buyer_email");// 买家支付宝账号
            String out_trade_no = request.getParameter("out_trade_no");// 商户网站唯一订单号
            String notify_time = request.getParameter("notify_time");// 通知时间
            String trade_status = request.getParameter("trade_status");//
            String seller_email = request.getParameter("seller_email");// 卖家支付宝账号
            String notify_id = request.getParameter("notify_id");// 通知校验ID
                                                                 // 通知校验ID。唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            LOG.info("支付宝WEB后台通知参数：subject[" + subject + "];trade_no[" + trade_no
                    + "];buyer_email[" + buyer_email + "];out_trade_no[" + out_trade_no + "];"
                    + "notify_time[" + notify_time + "];trade_status[" + trade_status
                    + "];seller_email[" + seller_email + "];notify_id[" + notify_id + "];");
            
            /* 2.解析返回状态 */
            String payStates = PayConstants.ReturnCode.FAILD;
            // 支付成功的两个状态
            if (PayConstants.AliPayReturnCode.TRADE_FINISHED.equals(trade_status)
                    || PayConstants.AliPayReturnCode.TRADE_SUCCESS.equals(trade_status)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
            if (!PayConstants.ReturnCode.SUCCESS.equals(payStates)) {
                return;
            } 
            
            String tenantId = out_trade_no.split("_")[0]; 
            String orderId = out_trade_no.split("_")[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("支付宝WEB后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("支付宝WEB后台通知出错，获取订单信息失败!");
            }
            String notifyUrl = tradeRecord.getNotifyUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            subject = tradeRecord.getSubject();
            
            /* 4.更新支付流水信息 */
            this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                    trade_no, notify_id, buyer_email, null, seller_email, PayOrgCode.ZFB);
            
            /* 5.异步通知业务系统订单支付状态 */
            PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                    trade_no, subject, orderAmount, payStates, PayConstants.PayOrgCode.ZFB);
            
            response.getWriter().write("success"); // 支付宝接收不到“success” 就会在24小时内重复调用多次
        } catch(IOException ex) {
            LOG.error("支付宝WEB后台通知失败", ex);
        } catch(Exception ex) {
            LOG.error("支付宝WEB后台通知失败", ex);
        }    
    }
    
    /**
     * 支付宝WEB即时到账前台通知地址
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/webReturn")
    public void alipayWebReturn(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOG.debug("支付宝WEB前台通知...");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取支付宝传递过来的参数 */
            String result = request.getParameter("is_success");
            String out_trade_no = request.getParameter("out_trade_no");
            String trade_no = request.getParameter("trade_no");
            LOG.info("支付宝WEB支付前台通知开始:交易订单号[" + out_trade_no + "]");
            LOG.info("支付宝WEB支付前台通知开始:result[" + result + "]");
            
            String payStates = PayConstants.ReturnCode.FAILD;
            if (PayConstants.AliPayReturnCode.RETURN_URL_T.equals(result)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            
            String tenantId = out_trade_no.split("_")[0]; 
            String orderId = out_trade_no.split("_")[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("支付宝WEB前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("支付宝WEB前台通知出错，获取订单信息失败!");
            }
            String returnUrl = tradeRecord.getReturnUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            
            String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId,
                    orderId, trade_no, tradeRecord.getSubject(), orderAmount, payStates,
                    PayConstants.PayOrgCode.ZFB);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(htmlStr);
        } catch (IOException ex) {
            LOG.error("支付宝WEB前台通知失败", ex);
            throw ex;
        } catch (Exception ex) {
            LOG.error("支付宝WEB前台通知失败", ex);
            throw ex;
        } 
    }
    
    /**
     * 支付宝WAP支付接口
     */
    @RequestMapping(value = "/wapPayment/alipayapi")
    public void alipayWapPay(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            response.setContentType("text/html;charset=utf-8");
            LOG.info("支付宝wap支付开始:商户订单号[" + orderId + "]" + " ，租户标识： " + tenantId);
            String infoMd5 = (String) request.getAttribute("infoMd5");   
            if(StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if(!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            
            String basePath = AbstractPayConfigManager.getPayUrl();
            String subject = "网上支付";
            if(!StringUtil.isBlank(tradeRecord.getSubject())) {
                subject = tradeRecord.getSubject();
            }
            String total_fee = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额    
            String format = "xml"; // 返回格式
            String v = "2.0"; // 必填，不需要修改
            String req_id = DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS); // 请求号
            String notify_url = basePath + WAP_NOTIFY_URL;
            String call_back_url = basePath + WAP_RETURN_URL;
            String merchant_url = tradeRecord.getMerchantUrl() + "?payStates=01";
            String seller_email = ConfigUtil.getProperty(tenantId,
                    AliPayConfigManager.PAY_ORG_NAME, AliPayConfigManager.WAP_SELLER_EMAIL); // 卖家支付宝帐户
            String partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WAP_SELLER_PID);
            String seller_key = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WAP_SELLER_KEY);
            String out_trade_no = tradeRecord.getTradeOrderId(); //商户订单号
            LOG.info("支付宝wap支付开始:交易订单号[" + out_trade_no + "]");
            // 请求业务参数详细
            String req_dataToken = "<direct_trade_create_req>" + "<notify_url>" + notify_url
                    + "</notify_url>" + "<call_back_url>" + call_back_url + "</call_back_url>"
                    + "<seller_account_name>" + seller_email + "</seller_account_name>"
                    + "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<subject>" + subject
                    + "</subject>" + "<total_fee>" + total_fee + "</total_fee>" + "<merchant_url>"
                    + merchant_url + "</merchant_url>" + "</direct_trade_create_req>";
            // 把请求参数打包成数组
            Map<String, String> sParaTempToken = new HashMap<String, String>();
            sParaTempToken.put("service", "alipay.wap.trade.create.direct");
            sParaTempToken.put("partner", partner);
            sParaTempToken.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
            sParaTempToken.put("sec_id", AliPayConfigManager.SIGN_TYPE);
            sParaTempToken.put("format", format);
            sParaTempToken.put("v", v);
            sParaTempToken.put("req_id", req_id);
            sParaTempToken.put("req_data", req_dataToken);
            LOG.info("支付宝获取token参数：" + sParaTempToken);
            // 建立请求
            String sHtmlTextToken = AlipaySubmit.buildRequest(seller_key,
                    AliPayConfigManager.ALIPAY_GATEWAY_NEW_WAP, "", "", sParaTempToken);
            LOG.info("支付宝获取token：" + sHtmlTextToken);
            // URLDECODE返回的信息
            sHtmlTextToken = URLDecoder.decode(sHtmlTextToken, AliPayConfigManager.INPUT_CHARSET);
            // 获取token
            String request_token = AlipaySubmit.getRequestToken(sHtmlTextToken);
            // //////////////////////////////////根据授权码token调用交易接口alipay.wap.auth.authAndExecute//////////////////////////////////////
            // 业务详细
            LOG.info("获取token值：" + request_token);
            String req_data = "<auth_and_execute_req><request_token>" + request_token
                    + "</request_token></auth_and_execute_req>";
            // 必填
            // 把请求参数打包成数组
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("service", "alipay.wap.auth.authAndExecute");
            sParaTemp.put("partner", partner);
            sParaTemp.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
            sParaTemp.put("sec_id", AliPayConfigManager.SIGN_TYPE);
            sParaTemp.put("format", format);
            sParaTemp.put("v", v);
            sParaTemp.put("req_data", req_data);
            // 建立请求
            String sHtmlText = AlipaySubmit.buildRequest(seller_key,
                    AliPayConfigManager.ALIPAY_GATEWAY_NEW_WAP, sParaTemp, "get", "确认");
            LOG.info("支付宝手机网页支付网页报文：" + sHtmlText);
            response.getWriter().println(sHtmlText);
            response.getWriter().flush();
        } catch(IOException ex) {
            LOG.error("支付宝wap支付发生错误", ex);
            throw ex;
        } catch(Exception ex) {
            LOG.error("支付宝wap支付发生错误", ex);
            throw ex;
        } 
    }
    
    /**
     * 支付宝WAP支付接口
     */
    @RequestMapping(value = "/wapPay201611")
    public void wapPay201611(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            response.setContentType("text/html;charset=utf-8");
            LOG.info("支付宝wap支付开始:商户订单号[" + orderId + "]" + " ，租户标识： " + tenantId);
            String infoMd5 = (String) request.getAttribute("infoMd5");   
            if (StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if (!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            
            String basePath = AbstractPayConfigManager.getPayUrl();
            String subject = "网上支付";
            if (!StringUtil.isBlank(tradeRecord.getSubject())) {
                subject = tradeRecord.getSubject();
            }
            String total_fee = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额    
            String notifyUrl = basePath + WAP_NOTIFY_URL_201611;
            String returnUrl = basePath + WAP_RETURN_URL;
            String out_trade_no = tradeRecord.getTradeOrderId(); //商户订单号
            LOG.info("支付宝wap支付开始:交易订单号[" + out_trade_no + "]");
            // 把请求参数打包成数组
            Map<String, String> sParaTemp = new HashMap<String, String>();
            // 公用回传参数, 本参数必须进行UrlEncode之后才可以发送给支付宝
            sParaTemp.put("passback_params", tenantId);
            // 业务参数
            sParaTemp.put("out_trade_no", tenantId + "_" + orderId);
            sParaTemp.put("subject", subject);
            sParaTemp.put("seller_id", AliPayConfigManager.getWapSellerId(tenantId));
            sParaTemp.put("total_amount", total_fee);
            sParaTemp.put("product_code", "QUICK_WAP_PAY");
            
        	// 获得初始化的AlipayClient
            AlipayClient alipayClient = new DefaultAlipayClient(AliPayConfigManager.getWapPayUrl(),
                    AliPayConfigManager.getAppId(tenantId),
                    AliPayConfigManager.getAppPrivateKey(tenantId), "json", "utf-8",
                    AliPayConfigManager.getAlipayPublicKey(tenantId));
        	// 创建API对应的request
        	AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        	// 在公共参数中设置回跳和通知地址
        	alipayRequest.setReturnUrl(returnUrl);
            alipayRequest.setNotifyUrl(notifyUrl);
            // 填充业务参数
            alipayRequest.setBizContent(JSON.toJSONString(sParaTemp));
            LOG.info("支付宝wap支付请求参数:" + JSON.toJSONString(alipayRequest));
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(alipayRequest).getBody(); 
            LOG.info("alipay for wap return form: " + form);
            // 直接将完整的表单html输出到页面
            response.getWriter().write(form);
            response.getWriter().flush();
        } catch(IOException ex) {
            LOG.error("支付宝wap支付发生错误", ex);
            throw ex;
        } catch(Exception ex) {
            LOG.error("支付宝wap支付发生错误", ex);
            throw ex;
        } 
    }
    
    @SuppressWarnings("unused")
	private void assembleCommonParam(Map<String, String> sParaTempToken) {
    	sParaTempToken.put("app_id", "2016110902676346");
    	sParaTempToken.put("method", "alipay.trade.wap.pay.return");
    	sParaTempToken.put("sign_type", "RSA");
    	sParaTempToken.put("sign", "2016110902676346");
    	sParaTempToken.put("charset", "utf-8");
    	sParaTempToken.put("timestamp", DateUtil.getDateString(DateUtil.DATETIME_FORMAT));
    	sParaTempToken.put("version", "1.0");
	}

	/**
     * 支付宝wap支付前台通知地址
     */
    @RequestMapping(value = "/wapReturn")
    public void alipayWapReturn(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOG.info("支付宝wap前台通知...");
        showParams(request);
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.获取支付宝传递过来的参数 */
            String result = request.getParameter("result");
            String out_trade_no = request.getParameter("out_trade_no");
            String trade_no = request.getParameter("trade_no");
            LOG.info("支付宝WAP支付前台通知开始:交易订单号[" + out_trade_no + "]");
            LOG.info("支付宝WAP支付前台通知开始:result[" + result + "]");
            
            String payStates = PayConstants.ReturnCode.SUCCESS;
            
            String tenantId = out_trade_no.split("_")[0];  
            String orderId = out_trade_no.split("_")[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("支付宝wap前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("支付宝wap前台通知出错，获取订单信息失败!");
            }
            String returnUrl = tradeRecord.getReturnUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            
            String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId,
                    orderId, trade_no, tradeRecord.getSubject(), orderAmount, payStates,
                    PayConstants.PayOrgCode.ZFB);
            response.setStatus(HttpServletResponse.SC_OK);
            printWriter.write(htmlStr);
            printWriter.flush();
            printWriter.close();
        } catch (IOException ex) {
            LOG.error("支付宝wap前台通知失败", ex);
            throw ex;
        } catch (Exception ex) {
            LOG.error("支付宝wap前台通知失败", ex);
            throw ex;
        }
    }
    
    /**
     * 支付宝wap支付后台通知服务
     */
    @RequestMapping(value = "/wapNotify")
    public void alipayWapNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("支付宝wap后台通知...");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取支付宝传递过来的参数 */
            String notify_data = request.getParameter("notify_data");// 商品名称
            Map<String, String> eleMap = XMLUtil.readStringXmlOut(notify_data);
            if (notify_data == null || notify_data.length() == 0 || eleMap == null
                    || eleMap.size() == 0) {
                LOG.error("支付宝WAP后台通知失败，获取支付宝参数失败，参数：[" + notify_data + "]");
                throw new SystemException("支付宝wap支付后台通知失败，获取支付宝后台通知参数失败");
            }
            String subject = eleMap.get("subject");// 商品名称
            String trade_no = eleMap.get("trade_no"); // 支付宝交易号
            String buyer_email = eleMap.get("buyer_email");// 买家支付宝账号
            String out_trade_no = eleMap.get("out_trade_no");// 商户网站唯一订单号
            String notify_time = eleMap.get("notify_time");// 通知时间
            String trade_status = eleMap.get("trade_status");//
            String seller_email = eleMap.get("seller_email");// 卖家支付宝账号
            String notify_id = eleMap.get("notify_id");// 通知校验ID
                                                       // 通知校验ID。唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            LOG.info("支付宝WAP后台通知参数：subject[" + subject + "];trade_no[" + trade_no
                    + "];buyer_email[" + buyer_email + "];out_trade_no[" + out_trade_no + "];"
                    + "notify_time[" + notify_time + "];trade_status[" + trade_status
                    + "];seller_email[" + seller_email + "];notify_id[" + notify_id + "];");
            /* 2.解析返回状态 */
            String payStates = PayConstants.ReturnCode.FAILD;
            // 支付成功的两个状态
            if (PayConstants.AliPayReturnCode.TRADE_FINISHED.equals(trade_status)
                    || PayConstants.AliPayReturnCode.TRADE_SUCCESS.equals(trade_status)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
            if (!PayConstants.ReturnCode.SUCCESS.equals(payStates)) {
                return;
            }
                        
            String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
            String orderId = out_trade_no;//orderInfoArray[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("支付宝wap后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("支付宝wap后台通知出错，获取订单信息失败!");
            }
            String notifyUrl = tradeRecord.getNotifyUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
//            String notifyIdDB = tradeRecord.getNotifyId();
            subject = tradeRecord.getSubject();
            
            /* 4.更新支付流水信息 */
            this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                    trade_no, notify_id, buyer_email, null, seller_email, PayOrgCode.ZFB);
            
            /* 5.异步通知业务系统订单支付状态 */
            PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                    trade_no, subject, orderAmount, payStates, PayConstants.PayOrgCode.ZFB);
            
            response.getWriter().write("success"); // 支付宝接收不到“success” 就会在24小时内重复调用多次
        } catch(IOException ex) {
            LOG.error("支付宝wap后台通知失败", ex);
        } catch(Exception ex) {
            LOG.error("支付宝wap后台通知失败", ex);
        }   
    }
    
    @RequestMapping(value = "/wapNotify201611")
    public void wapNotify201611(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("支付宝wap后台通知...");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取支付宝传递过来的参数 */
//            String notify_data = request.getParameter("notify_data");// 商品名称
//            Map<String, String> eleMap = XMLUtil.readStringXmlOut(notify_data);
//            if (notify_data == null || notify_data.length() == 0 || eleMap == null
//                    || eleMap.size() == 0) {
//                LOG.error("支付宝WAP后台通知失败，获取支付宝参数失败，参数：[" + notify_data + "]");
//                throw new SystemException("支付宝wap支付后台通知失败，获取支付宝后台通知参数失败");
//            }
            String subject = request.getParameter("subject");// 商品名称
            String trade_no = request.getParameter("trade_no"); // 支付宝交易号
            String buyer_email = request.getParameter("buyer_logon_id");// 买家支付宝账号
            String out_trade_no = request.getParameter("out_trade_no");// 商户网站唯一订单号
            @SuppressWarnings("unused")
			String notify_time = request.getParameter("notify_time");// 通知时间
            String trade_status = request.getParameter("trade_status");//
            String seller_email = request.getParameter("seller_email");// 卖家支付宝账号
            String notify_id = request.getParameter("notify_id");// 通知校验ID
                                                       // 通知校验ID。唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            LOG.info("支付宝WAP后台通知参数：trade_no[" + trade_no + "];;out_trade_no[" + out_trade_no + "];");
            /* 2.解析返回状态 */
            String payStates = PayConstants.ReturnCode.FAILD;
            // 支付成功的两个状态
            if (PayConstants.AliPayReturnCode.TRADE_FINISHED.equals(trade_status)
                    || PayConstants.AliPayReturnCode.TRADE_SUCCESS.equals(trade_status)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
            if (!PayConstants.ReturnCode.SUCCESS.equals(payStates)) {
                return;
            }
                        
            String tenantId = out_trade_no.split("_")[0]; 
            String orderId = out_trade_no.split("_")[1];
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOG.error("支付宝wap后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("支付宝wap后台通知出错，获取订单信息失败!");
            }
            String notifyUrl = tradeRecord.getNotifyUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            subject = tradeRecord.getSubject();
            
            /* 4.更新支付流水信息 */
            this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                    trade_no, notify_id, buyer_email, null, seller_email, PayOrgCode.ZFB);
            
            /* 5.异步通知业务系统订单支付状态 */
            PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                    trade_no, subject, orderAmount, payStates, PayConstants.PayOrgCode.ZFB);
            
            response.getWriter().write("success"); // 支付宝接收不到“success” 就会在24小时内重复调用多次
        } catch(IOException ex) {
            LOG.error("支付宝wap后台通知失败", ex);
        } catch(Exception ex) {
            LOG.error("支付宝wap后台通知失败", ex);
        }   
    }
    
    /**
     * 单笔无密退款
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author fanpw
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/noPwdRefund")
    public RefundRes noPwdRefund(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("支付宝无密退款操作开始,退款订单号： " + orderId);
        TradeRecord refundRecord = this.queryTradeRecord(tenantId, orderId);
        if (refundRecord == null) {
            LOG.error("退款失败，查询退款沉淀信息出错： 租户标识： " + tenantId + "，订单号： " + orderId);
            throw new SystemException("退款失败，查询退款沉淀信息出错!");
        }
        
        String basePath = AbstractPayConfigManager.getPayUrl();
        LOG.info("项目根路径： " + basePath);   
        
        //组织支付宝所需退款详细数据
        StringBuilder detail_data = new StringBuilder();
        int recordNum = 1;
        String oriOrderId = refundRecord.getOriOrderId();                
        TradeRecord originRecord = this.queryTradeRecord(tenantId, oriOrderId);
        if(originRecord == null) {
            LOG.error("无密退款失败，查询订单信息出错： 租户标识： " + tenantId + "，订单号： " + oriOrderId);
            throw new SystemException("无密退款失败，查询订单信息出错!");
        }
        String requestSource = originRecord.getRequestSource();
        long refundLiAmount = Math.abs(refundRecord.getPayAmount());
        String refundAmount = String.format("%.2f", AmountUtil.changeLiToYuan(refundLiAmount));
        String out_trade_no = refundRecord.getPayOrgSerial();
        String item_data = out_trade_no + "^" + refundAmount + "^" + "return";
        detail_data.append(item_data);
        //退款通知地址
        String notify_url = basePath + REFUND_NOTIFY_URL;
        String batchNo = refundRecord.getBatchNo();
        Map<String, String> xmlMap = this.alipayNoPwdRefund(tenantId, batchNo, recordNum,
                requestSource, detail_data.toString(), notify_url);
        String isSuccess = xmlMap.get("is_success");
        String error = xmlMap.get("error");
        int status = PayConstants.Status.REFUND_FAIL;
        RefundRes refundRes = new RefundRes();
        refundRes.setTenantId(tenantId);
        refundRes.setOrderId(orderId);
        refundRes.setOriOrderId(refundRecord.getOriOrderId());
        refundRes.setRefundAmount(refundAmount);    
        if(PayConstants.AliPayReturnCode.RETURN_URL_T.equals(isSuccess)) {
            LOG.info("支付宝无密退款成功，批次号：" + batchNo);
            status = PayConstants.Status.REFUND_ACCEPT;
            refundRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            LOG.error("支付宝无密退款失败，批次号：" + batchNo + ",失败原因：" + error);
            refundRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            refundRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            refundRes.setErrMsg(error);
        }
       
        this.modifyTradeState(tenantId, orderId, status);        
        return refundRes;
    }
    
    /**
     * 无密退款（批量退款）
     * @param tenantId
     * @param orderId
     * @param oriOrderId
     * @param request
     * @param response
     * @author fanpw
     * @throws Exception 
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/batchNoPwdRefund")
    public BatchRefundRes batchNoPwdRefund(@RequestParam(value = "tenantId", required = true) String tenantId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        String batchNo = (String) request.getAttribute("batchNo");
        String infoMd5 = (String) request.getAttribute("infoMd5");
        if(StringUtil.isBlank(batchNo)) {
            throw new SystemException("退款失败，传入的批次号为空!");
        }
        if(StringUtil.isBlank(infoMd5)) {
            throw new SystemException("退款失败，传入的加密信息为空!");
        }
        LOG.info("支付宝无密退款操作开始,退款批次号： " + batchNo);   
        String infoStr = batchNo + VerifyUtil.SEPARATOR + tenantId;
        String key = AbstractPayConfigManager.getRequestKey();
        if(!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
            LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的无密退款请求参数非法,参数有误或已被篡改！");
        }
        
        List<TradeRecord> refundRecordList = this.queryTradeRecords(tenantId, batchNo, null);
        if(CollectionUtil.isEmpty(refundRecordList)) {
            LOG.error("无密退款失败，查询退款沉淀信息出错： 租户标识： " + tenantId + "，退款批次号： " + batchNo);
            throw new SystemException("无密退款失败，查询退款沉淀信息出错!");
        }
        
        String basePath = AbstractPayConfigManager.getPayUrl();
        LOG.info("项目根路径： " + basePath);   
        
        //组织支付宝所需退款详细数据
        StringBuilder detail_data = new StringBuilder();
        int recordNum = refundRecordList.size();
        String oriOrderId = "";
        String requestSource = "";
        for (int i = 0; i < recordNum; i++) {
            TradeRecord refundRecord = refundRecordList.get(i);
            oriOrderId = refundRecord.getOriOrderId();                
            TradeRecord originRecord = this.queryTradeRecord(tenantId, oriOrderId);
            if(originRecord == null) {
                LOG.error("无密退款失败，查询订单信息出错： 租户标识： " + tenantId + "，订单号： " + oriOrderId);
                throw new SystemException("无密退款失败，查询订单信息出错!");
            }
            requestSource = originRecord.getRequestSource();
            long refundLiAmount = Math.abs(refundRecord.getPayAmount());
            String refundAmount = String.format("%.2f", AmountUtil.changeLiToYuan(refundLiAmount));
            String out_trade_no = refundRecord.getPayOrgSerial();
            String item_data = out_trade_no + "^" + refundAmount + "^" + "return";
            detail_data.append(item_data);
            if(i != recordNum - 1) {
                detail_data.append("#");
            }
        }
        
        //退款通知地址
        String notify_url = basePath + BATCH_REFUND_NOTIFY_URL;
        Map<String, String> xmlMap = this.alipayNoPwdRefund(tenantId, batchNo, recordNum,
                requestSource, detail_data.toString(), notify_url);
        String isSuccess = xmlMap.get("is_success");
        String error = xmlMap.get("error");
        int status = PayConstants.Status.REFUND_FAIL;
        BatchRefundRes refundRes = new BatchRefundRes();
        refundRes.setTenantId(tenantId);
        refundRes.setBatchNo(batchNo);
        refundRes.setPayOrgCode(PayConstants.PayOrgCode.ZFB);        
        if(PayConstants.AliPayReturnCode.RETURN_URL_T.equals(isSuccess)) {
            LOG.info("支付宝无密退款成功，批次号：" + batchNo);
            status = PayConstants.Status.REFUND_ACCEPT;
            refundRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            LOG.error("支付宝无密退款失败，批次号：" + batchNo + ",失败原因：" + error);
            refundRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            refundRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            refundRes.setErrMsg(error);
        }
       
        for(TradeRecord refundRecord : refundRecordList) {
            this.modifyTradeState(tenantId, refundRecord.getOrderId(), status);
        }
        
        return refundRes;
    }
    
    /**
     * 支付宝无密退款接口调用
     * @param tenantId
     * @param batchNo
     * @param batchNum
     * @param requestSource
     * @param detail_data
     * @param notify_url
     * @return
     * @throws Exception
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private Map<String, String> alipayNoPwdRefund(String tenantId, String batchNo, int batchNum,
            String requestSource, String detail_data, String notify_url) throws Exception {
        //退款当天日期
        //必填，格式：年[4位]-月[2位]-日[2位] 小时[2位 24小时制]:分[2位]:秒[2位]，如：2007-10-01 13:13:13
        String refund_date = DateUtil.getCurrentTime();
        //退款笔数
        //必填，参数detail_data的值中，“#”字符出现的数量加1，最大支持1000笔（即“#”字符出现的数量999个）
        String batch_num = String.valueOf(batchNum);
        String partner = "";
        String seller_key = "";
        if(PayConstants.RequestSource.WEB.equals(requestSource)) {
            partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_PID);
            seller_key = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_KEY);
        } else {
            partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WAP_SELLER_PID);
            seller_key = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WAP_SELLER_KEY);
        }
        /* 1.将请求传递过来的参数，放到支付宝请求里 */
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
        sParaTemp.put("batch_no", batchNo); //批次号, 必填，格式：当天日期[8位]+序列号[3至24位]，如：201008010000001           
        sParaTemp.put("batch_num", batch_num);
        sParaTemp.put("detail_data", detail_data.toString());
        sParaTemp.put("notify_url", notify_url);
        sParaTemp.put("partner", partner);
        sParaTemp.put("refund_date", refund_date);
        sParaTemp.put("service", "refund_fastpay_by_platform_nopwd");
        LOG.info("支付宝无密退款请求参数： " + sParaTemp);
        /* 2.发送请求给支付宝，获取返回值 */
        String sHtmlText = AlipaySubmit.buildRequest(seller_key,
                AliPayConfigManager.ALIPAY_GATEWAY_NEW, "", "", sParaTemp);
        LOG.info("alipay无密接口返回：[" + sHtmlText + "]");
        /* 3.解析返回值,并翻译成返回状态，更新支付平台流水状态 */
        if (StringUtil.isBlank(sHtmlText)) {
            throw new SystemException("alipay无密接口返回空");
        }
        
        return XMLUtil.readStringXmlOut(sHtmlText);
    }
    
    /**
     * 支付宝无密退款异步通知处理方法(单笔退款)
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/noPwdRefundNotify")
    public void noPwdRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("支付宝无密退款后台通知开始...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.获取支付宝传递过来的参数 */
            String notify_time = request.getParameter("notify_time");
            String notify_type = request.getParameter("notify_type");
            String notify_id = request.getParameter("notify_id");// 支付宝通知校验id,唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            String batch_no = request.getParameter("batch_no");// 批次号 对应日止表订单号
            String success_num = request.getParameter("success_num");
            String result_details = request.getParameter("result_details");// 详细结果
            String unfreezed_details = request.getParameter("unfreezed_details");// 解冻结果明细

            LOG.info("支付宝无密退款后台通知参数：notify_time[" + notify_time + "];notify_type[" + notify_type
                    + "];notify_id[" + notify_id + "];batch_no[" + batch_no + "];result_details["
                    + result_details + "];unfreezed_details[" + unfreezed_details + "];"
                    + "success_num[" + success_num + "];");

            /* 2. 解析支付宝返回报文 */
            List<RefundDealItem> refundDealItemList = AlipayReturnParser
                    .parseRefundNotifyDetails(result_details);
            if (CollectionUtil.isEmpty(refundDealItemList)) {
                throw new SystemException("支付宝无密退款后台通知参数解析出错！");
            }

            RefundDealItem item = refundDealItemList.get(0);
            String dealResult = item.getDealResult();
            int status = PayConstants.Status.REFUND_FAIL;
            String dealState = PayConstants.ReturnCode.FAILD;
            if ("1".equals(success_num) && "SUCCESS".equals(dealResult)) {
                status = PayConstants.Status.REFUND_FINISH;
                dealState = PayConstants.ReturnCode.SUCCESS;
            }

            String partnerId = batch_no.substring(8, 13);
            LOG.debug("从批次号中分解出的合作方编码： " + partnerId);
            String tenantId = this.getTenantId(partnerId);
            String payOrgSerial = item.getTradeNo();
            List<TradeRecord> tradeRecords = this.queryTradeRecords(tenantId, batch_no,
                    payOrgSerial);
            if (CollectionUtil.isEmpty(tradeRecords)) {
                LOG.error("支付宝无密退款后台通知失败， 获取退款信息记录失败! 租户标识：" + tenantId + "，批次号： " + batch_no
                        + ",原付款支付宝交易号: " + payOrgSerial);
                throw new SystemException("支付宝无密退款后台通知失败， 获取退款信息记录失败!");
            }
            /* 3.判断是否已经回调过，如果不是同一个回调更新支付流水信息，否则什么都不做 */
            TradeRecord tradeRecord = tradeRecords.get(0);
            if (!notify_id.equals(tradeRecord.getNotifyId())) {
                this.modifyTradeState(tenantId, tradeRecord.getOrderId(), status, payOrgSerial,
                        notify_id, null, null, null, null, result_details, PayOrgCode.ZFB);
                
                /* 4.异步通知外部系统退款结果 */
                String notifyUrl = tradeRecord.getNotifyUrl();
                if (!StringUtil.isBlank(notifyUrl)) {
                    PaymentNotifyUtil.notifyClientRefundDealResult(notifyUrl, tenantId,
                            tradeRecord.getOrderId(), tradeRecord.getOriOrderId(),
                            item.getRefundAmount(), dealState);
                }
            }

            printWriter.write("success"); // 支付宝接收不到“success” 就会在24小时内重复调用多次
            printWriter.flush();
            printWriter.close();
        } catch (IOException ex) {
            LOG.error("支付宝无密退款后台通知失败", ex);
        } catch (Exception ex) {
            LOG.error("支付宝无密退款后台通知失败", ex);
        } 
    }

    /**
     * 支付宝无密退款异步通知处理方法(批量退款)
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/batchNoPwdRefundNotify")
    public void batchNoPwdRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("支付宝批量无密退款后台通知开始...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.获取支付宝传递过来的参数 */
            String notify_time = request.getParameter("notify_time");
            String notify_type = request.getParameter("notify_type");
            String notify_id = request.getParameter("notify_id");// 支付宝通知校验id,唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            String batch_no = request.getParameter("batch_no");// 批次号 对应日止表订单号
            String success_num = request.getParameter("success_num");
            String result_details = request.getParameter("result_details");// 详细结果
            String unfreezed_details = request.getParameter("unfreezed_details");// 解冻结果明细

            LOG.info("支付宝批量无密退款后台通知参数：notify_time[" + notify_time + "];notify_type[" + notify_type
                    + "];notify_id[" + notify_id + "];batch_no[" + batch_no + "];result_details["
                    + result_details + "];unfreezed_details[" + unfreezed_details + "];"
                    + "success_num[" + success_num + "];");

            /* 2. 解析支付宝返回报文 */
            List<RefundDealItem> refundDealItemList = AlipayReturnParser
                    .parseRefundNotifyDetails(result_details);
            if (CollectionUtil.isEmpty(refundDealItemList)) {
                throw new SystemException("支付宝批量无密退款后台通知参数解析出错！");
            }

            String partnerId = batch_no.substring(8, 13);
            LOG.debug("从批次号中分解出的合作方编码： " + partnerId);
            String tenantId = this.getTenantId(partnerId);

            StringBuilder resultDetails = new StringBuilder();
            int size = refundDealItemList.size();
            String notifyUrl = "";
            for (int i = 0; i < size; i++) {
                RefundDealItem item = refundDealItemList.get(i);
                String dealResult = item.getDealResult();
                int status = PayConstants.Status.REFUND_FAIL;
                String dealState = PayConstants.ReturnCode.FAILD;
                if ("SUCCESS".equals(dealResult)) {
                    status = PayConstants.Status.REFUND_FINISH;
                    dealState = PayConstants.ReturnCode.SUCCESS;
                }
                String payOrgSerial = item.getTradeNo();
                List<TradeRecord> tradeRecords = this.queryTradeRecords(tenantId, batch_no,
                        payOrgSerial);
                if (CollectionUtil.isEmpty(tradeRecords)) {
                    LOG.error("支付宝批量无密退款后台通知失败， 获取退款信息记录失败! 租户标识：" + tenantId + "，批次号： " + batch_no
                            + ",原付款支付宝交易号: " + payOrgSerial);
                    throw new SystemException("支付宝批量无密退款后台通知失败， 获取退款信息记录失败!");
                }
                /* 3.判断是否已经回调过，如果不是同一个回调更新支付流水信息，否则什么都不做 */
                TradeRecord tradeRecord = tradeRecords.get(0);
                String item_data = tradeRecord.getOrderId() + "^" + tradeRecord.getOriOrderId()
                        + "^" + item.getRefundAmount() + "^" + dealState;
                resultDetails.append(item_data);
                if (i != size - 1) {
                    resultDetails.append("#");
                }
                notifyUrl = tradeRecord.getNotifyUrl();
                if (!notify_id.equals(tradeRecord.getNotifyId())) {
                    this.modifyTradeState(tenantId, tradeRecord.getOrderId(), status, null,
                            notify_id, null, null, null, null, result_details, PayOrgCode.ZFB);
                }
            }

            /* 4.异步通知外部系统退款结果 */
            if (!StringUtil.isBlank(notifyUrl)) {
                PaymentNotifyUtil.notifyClientBatchRefundResultDetails(notifyUrl, tenantId,
                        success_num, resultDetails.toString());
            }
            printWriter.write("success"); // 支付宝接收不到“success” 就会在24小时内重复调用多次
            printWriter.flush();
            printWriter.close();
        } catch (IOException ex) {
            LOG.error("支付宝批量无密退款后台通知失败", ex);
        } catch (Exception ex) {
            LOG.error("支付宝批量无密退款后台通知失败", ex);
        } 
    }
    
    /**
     * 支付宝批量付款到支付宝账户
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @throws Exception
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/batchTrans/alipayapi")
    public void alipayBatchTrans(HttpServletRequest request, HttpServletResponse response,
            BatchWithdrawReqParam withdrawReqParam) throws Exception {
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            String orderId = withdrawReqParam.getBatchNo();
            String tenantId = withdrawReqParam.getTenantId();
            LOG.info("支付宝批量付款到支付宝账户支付开始:商户批次号[" + orderId + "]" + "，租户标识： " + tenantId);
            if(!VerifyUtil.verifyBatchWithDrawParam(withdrawReqParam)) {
                LOG.error("延签失败：传入的参数已被篡改！" + withdrawReqParam.getInfoMd5());
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的提现请求参数非法,参数有误或已被篡改！");
            }
            
            String payUrl = AbstractPayConfigManager.getPayUrl();
            LOG.info("支付平台外网地址： " + payUrl);    
            String seller_email = ConfigUtil.getProperty(tenantId,
                    AliPayConfigManager.PAY_ORG_NAME, AliPayConfigManager.BATCH_TRANS_SELLER_EMAIL); // 卖家支付宝帐户
            String partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.BATCH_TRANS_SELLER_PID);
            String sellerKey = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.BATCH_TRANS_SELLER_KEY);
            //付款账户名 ,必填，个人支付宝账号是真实姓名公司支付宝账号是公司名称
            String account_name = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.BATCH_TRANS_ACCT_NAME);
            //付款总金额，必填，即参数detail_data的值中所有金额的总和
            String batch_fee = withdrawReqParam.getBatchFee();
            //付款笔数，必填，即参数detail_data的值中，“|”字符出现的数量加1，最大支持1000笔（即“|”字符出现的数量999个）
            String batch_num = withdrawReqParam.getBatchNum(); 
            //付款详细数据必填，格式：流水号1^收款方帐号1^真实姓名^付款金额1^备注说明1|流水号2^收款方帐号2^真实姓名^付款金额2^备注说明2....
            String detail_data = withdrawReqParam.getDetailData();
            String notify_url = payUrl + BATCH_TRANS_NOTIFY_URL;
            //把请求参数打包成数组
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("service", "batch_trans_notify");
            sParaTemp.put("partner", partner);
            sParaTemp.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
            sParaTemp.put("notify_url", notify_url);
            sParaTemp.put("email", seller_email);
            sParaTemp.put("account_name", account_name);
            sParaTemp.put("pay_date", DateUtil.getDateString(DateUtil.YYYYMMDD)); //付款当天日期
            sParaTemp.put("batch_no", orderId);
            sParaTemp.put("batch_fee", batch_fee); 
            sParaTemp.put("batch_num", batch_num); 
            sParaTemp.put("detail_data", detail_data); 

            //建立请求
            String sHtmlText = AlipaySubmit.buildRequest(sellerKey,
                    AliPayConfigManager.ALIPAY_GATEWAY_NEW, sParaTemp, "get", "确认");
            LOG.info("支付宝批量付款到支付宝账户：" + sHtmlText);
            printWriter.println(sHtmlText);
            printWriter.flush();
            printWriter.close();
        } catch(IOException ex) {
            LOG.error("支付宝批量付款到支付宝账户发生错误", ex);
            throw ex;
        } catch(Exception ex) {
            LOG.error("支付宝批量付款到支付宝账户发生错误", ex);
            throw ex;
        } 
    }
    /**
     * 支付宝APP支付
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/appPayment/alipayapi")
    public void alipayAppPay(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("支付宝APP支付...");
        PrintWriter printWriter = null;
        CommonPayRes commonPayRes = new CommonPayRes();
        commonPayRes.setTenantId(tenantId);
        commonPayRes.setOrderId(orderId);
        commonPayRes.setPayOrgCode(AliPayConfigManager.PAY_ORG_NAME);
        commonPayRes.setReturnCode(PayConstants.ReturnCode.FAILD);
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.接收调用端传过来的信息 */
            String infoMd5 = (String) request.getAttribute("infoMd5");
            if (StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if (!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                        "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            /* 2.组织返回信息给调用端 */
            /* 2.1返回业务数据给调用端*/
            String basePath = AbstractPayConfigManager.getPayUrl();
            String payCenterNotifyUrl = basePath + APP_NOTIFY_URL;
            commonPayRes.setOrderAmount(String.valueOf(tradeRecord.getPayAmount()));
            commonPayRes.setOrderId(orderId);
            commonPayRes.setPayCenterNotifyUrl(payCenterNotifyUrl);
            commonPayRes.setPayCenterOrderId(tradeRecord.getTradeOrderId());
            commonPayRes.setRequestSource(tradeRecord.getRequestSource());
            commonPayRes.setSubject(tradeRecord.getSubject());
            commonPayRes.setTenantId(tenantId);
            /* 2.2返回给sdk的报文，用于对支付宝发起请求*/
            JSONObject requestMessage = new JSONObject();
            
            commonPayRes.setRequestMessage(requestMessage);
            /* 2.3返回成功标志*/
            commonPayRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } catch (BusinessException ex) {
            LOG.error(ex.getErrorMessage(), ex);
            commonPayRes.setErrCode(ex.getErrorCode());
            commonPayRes.setErrMsg(ex.getErrorMessage());
        } catch (Exception ex) {
            LOG.error("兴业APP支付失败", ex);
            commonPayRes.setErrMsg("兴业APP支付失败:" + ex.getMessage());
        } 
        String returnStr = ResponseUtil.getCommonPayResponse(commonPayRes);
        LOG.info("支付宝APP支付返回报文：[" + returnStr + "]");
        printWriter.write(returnStr);
        printWriter.flush();
    }
    /**
     * SDK端数据加密
     * @param sParaTemp
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    @SuppressWarnings("unused")
	private String signForApp(Map<String, String> sParaTemp ){
        return null;
    }
}
