package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.opt.sdk.dubbo.util.HttpClientUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.controller.core.TradeBaseController;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentNotifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.alipay.AlipayCore;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.PpPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
@Controller
@RequestMapping(value = "/paypal")
public class PpPayController extends TradeBaseController {
	private static final Logger LOGGER = Logger.getLogger(PpPayController.class);
	/** web支付后台通知地址 **/
    private static final String WEB_NOTIFY_URL = "/paypal/webNotify";
    /** web支付前台通知地址 **/
    private static final String WEB_RETURN_URL = "/paypal/webReturn";
    
	@RequestMapping(value = "/payapi")
    public void pay(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            LOGGER.info("PayPal-WEB支付开始:商户订单号[" + orderId + "]" + " ，租户标识： " + tenantId);
            String infoMd5 = (String) request.getAttribute("infoMd5");   
            if(StringUtil.isBlank(infoMd5)) {
                throw new SystemException("支付失败，传入的加密信息为空!");
            }
            String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
            String key = AbstractPayConfigManager.getRequestKey();
            if(!VerifyUtil.checkParam(infoStr, infoMd5, key)) {
                LOGGER.error("延签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOGGER.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("发起支付时查询订单信息异常!");
            }
            
            String basePath = AbstractPayConfigManager.getPayUrl();
            LOGGER.info("项目根路径： " + basePath);
            //组织支付参数            
            //服务器异步通知页面路径
            //需http://格式的完整路径，不能加?id=123这类自定义参数     //页面跳转同步通知页面路径
            String notify_url = basePath + WEB_NOTIFY_URL;
            //需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/       
            String returnUrl = basePath + WEB_RETURN_URL;
            String out_trade_no = tradeRecord.getTradeOrderId(); //商户订单号
            LOGGER.info("PayPalWEB支付开始:交易订单号[" + out_trade_no + "]");
            String subject = "网上支付";
            if (!StringUtil.isBlank(tradeRecord.getSubject())) {
                subject = tradeRecord.getSubject();
            }
            String total_fee = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额      
            
            //把请求参数打包成
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("charset", "utf-8");
            sParaTemp.put("rm", "2");
            sParaTemp.put("invoice", tenantId + "#" + orderId);
            sParaTemp.put("return", returnUrl);
            sParaTemp.put("notify_url", notify_url);
            sParaTemp.put("item_name", subject);
            sParaTemp.put("amount", total_fee);
            sParaTemp.put("cmd", "_xclick");
            sParaTemp.put("business", PpPayConfigManager.getMerchantAccountId(tenantId));
            
            //建立请求
            String sHtmlText = buildRequest(PpPayConfigManager.getCheckoutButtonUrl(), sParaTemp, "post", "确认");
            LOGGER.info("向PayPalWEB即时到账交易接口发起支付请求：" + sHtmlText);
            printWriter.println(sHtmlText);
            printWriter.flush();
            printWriter.close();
        } catch(IOException ex) {
            LOGGER.error("PayPal网页支付发生错误", ex);
            throw ex;
        } catch(Exception ex) {
            LOGGER.error("PayPal网页支付发生错误", ex);
            throw ex;
        } 
	}
	
	public static String buildRequest(final String payGateway, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuilder sbHtml = new StringBuilder();

        sbHtml.append("<form id=\"ppsubmit\" name=\"ppsubmit\" action=\"" + payGateway + "\" method=\"" + strMethod + "\">");
        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) sPara.get(name);
            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }
        //submit按钮控件请不要含有name属性
        sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['ppsubmit'].submit();</script>");

        return sbHtml.toString();
    }
	
	private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        return AlipayCore.paraFilter(sParaTemp);
    }
	
	@RequestMapping(value = "/webNotify")
    public void ppWebNotify(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("paypalWEB后台通知...");
        showParams(request);
        try {
        	verifyAuthentication(request);
        	
        	request.setCharacterEncoding("utf-8");
            /* 1.获取paypal传递过来的参数 */
            String subject = request.getParameter("item_name");// 商品名称
            String trade_no = request.getParameter("trade_no"); // paypal交易号
            String buyer_email = request.getParameter("buyer_email");// 买家paypal账号
            String out_trade_no = request.getParameter("invoice");// 商户网站唯一订单号
            String notify_time = request.getParameter("notify_time");// 通知时间
            String paymentStatus = request.getParameter("payment_status");//
            String seller_email = request.getParameter("seller_email");// 卖家paypal账号
            String notify_id = request.getParameter("ipn_track_id");// 通知校验ID
                                                                 // 通知校验ID。唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            LOGGER.info("paypalWEB后台通知参数：subject[" + subject + "];trade_no[" + trade_no
                    + "];buyer_email[" + buyer_email + "];out_trade_no[" + out_trade_no + "];"
                    + "notify_time[" + notify_time + "];payment_status[" + paymentStatus
                    + "];seller_email[" + seller_email + "];notify_id[" + notify_id + "];");
            
            /* 2.解析返回状态 */
            String payStates = PayConstants.ReturnCode.FAILD;
            // 支付成功的两个状态
            if (PayConstants.PAYPAL_TRANSACTION_COMPLETED.equals(paymentStatus)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
            if (!PayConstants.ReturnCode.SUCCESS.equals(payStates)) {
                return;
            } 
            
            String tenantId = out_trade_no.split("#")[0]; 
            String orderId = out_trade_no.split("#")[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOGGER.error("paypalWEB后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("paypalWEB后台通知出错，获取订单信息失败!");
            }
            String notifyUrl = tradeRecord.getNotifyUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            String notifyIdDB = tradeRecord.getNotifyId();
            subject = tradeRecord.getSubject();
            
            /* 4.判断是否已经回调过，如果不是同一个回调更新支付流水信息，否则什么都不做 */
            if (!notify_id.equals(notifyIdDB) && tradeRecord.getStatus() != null
                    && PayConstants.Status.APPLY == tradeRecord.getStatus()) {
                this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                        trade_no, notify_id, buyer_email, null, seller_email);
                
                /* 5.异步通知业务系统订单支付状态 */
                PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                        trade_no, subject, orderAmount, payStates, PayConstants.PayOrgCode.PP);
            }
            
        } catch(IOException ex) {
            LOGGER.error("paypalWEB后台通知失败", ex);
        } catch(Exception ex) {
            LOGGER.error("paypalWEB后台通知失败", ex);
        }    
    }

    private void verifyAuthentication(HttpServletRequest request)
            throws UnsupportedEncodingException {
        // 从 PayPal 出读取 POST 信息同时添加变量„cmd‟ 
        Enumeration<String> en = request.getParameterNames(); 
        String str = "cmd=_notify-validate"; 
        while (en.hasMoreElements()) { 
        	String paramName = (String) en.nextElement(); 
        	String paramValue = request.getParameter(paramName); 
        	str = str + "&" +paramName + "=" + URLEncoder.encode(paramValue, request.getParameter("charset")); 
        } 
        LOGGER.info("paypal支付请求验证参数，验证是否来自paypal消息：" + str); 
        // 将信息 POST 回给 PayPal 进行验证    测试环境先省略这一步 //HTTPWEB是我自己的类 网上有很多HTTP请求的方法 
        String result = HttpClientUtil.sendPost(PpPayConfigManager.getIpnUrl(), str); 
        LOGGER.info("paypal支付确认结果result="+result);
        JSONObject jsonObject = JSON.parseObject(result);
        if (!"VERIFIED".equals(jsonObject.getString("data"))) {
//            throw new com.ai.opt.base.exception.BusinessException("authentication failed");
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
        LOGGER.info("------------------------------");  
        for (Map.Entry<String, String> entry : set) {  
            System.out.println(entry.getKey() + ":" + entry.getValue());  
        }  
        LOGGER.info("------------------------------");  
    }
	
	/**
     * paypalWEB即时到账前台通知地址
     */
    @RequestMapping(value = "/webReturn")
    public void ppWebReturn(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOGGER.info("paypalWEB前台通知...");
        showParams(request);
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取paypal传递过来的参数 */
            String invoice = request.getParameter("invoice");
            String trade_no = request.getParameter("trade_no");
            LOGGER.info("paypalWEB支付前台通知开始:交易订单号[" + invoice + "]");
            
            String payStates = PayConstants.ReturnCode.SUCCESS;
            
            String tenantId = invoice.split("#")[0]; 
            String orderId = invoice.split("#")[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOGGER.error("paypalWEB前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("paypalWEB前台通知出错，获取订单信息失败!");
            }
            String returnUrl = tradeRecord.getReturnUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            
            String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId,
                    orderId, trade_no, tradeRecord.getSubject(), orderAmount, payStates,
                    PayConstants.PayOrgCode.PP);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(htmlStr);
        } catch (IOException ex) {
            LOGGER.error("paypalWEB前台通知失败", ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("paypalWEB前台通知失败", ex);
            throw ex;
        } 
    }
}
