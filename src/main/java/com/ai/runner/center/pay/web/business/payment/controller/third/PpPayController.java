package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.ai.runner.center.pay.web.business.payment.util.third.alipay.AlipaySubmit;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.AliPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.PpPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigFromFileUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.MD5;
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
            String payment_type = "1"; //支付类型,必填，不能修改
            //服务器异步通知页面路径
            String notify_url = basePath + WEB_NOTIFY_URL;
            //需http://格式的完整路径，不能加?id=123这类自定义参数     //页面跳转同步通知页面路径
            String returnUrl = basePath + WEB_RETURN_URL;
            //需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/       
            String seller_email = ConfigUtil.getProperty(tenantId,
                    AliPayConfigManager.PAY_ORG_NAME, AliPayConfigManager.WEB_SELLER_EMAIL); // 卖家PayPal帐户
            String partner = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_PID);
            String seller_key = ConfigUtil.getProperty(tenantId, AliPayConfigManager.PAY_ORG_NAME,
                    AliPayConfigManager.WEB_SELLER_KEY);
            String out_trade_no = tradeRecord.getTradeOrderId(); //商户订单号
            LOGGER.info("PayPalWEB支付开始:交易订单号[" + out_trade_no + "]");
            String subject = "网上支付";
            if (!StringUtil.isBlank(tradeRecord.getSubject())) {
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
            sParaTemp.put("charset", "utf-8");
            sParaTemp.put("rm", "2");
//            sParaTemp.put("RETURNURL", return_url);
//            sParaTemp.put("CANCELURL", notify_url);
//            sParaTemp.put("CALLBACK", notify_url);
            sParaTemp.put("return", returnUrl);
            sParaTemp.put("callback_url", "callback_url");
            sParaTemp.put("cancel_return ", "cancel_return");
            sParaTemp.put("shopping_url ", "shopping_url");
            sParaTemp.put("notify_url ", notify_url);
            sParaTemp.put("return", "http://www.baidu.com");
            sParaTemp.put("item_name", subject);
            sParaTemp.put("amount", total_fee);
            sParaTemp.put("cmd", "_xclick");
            sParaTemp.put("business", PpPayConfigManager.getMerchantAccountId(tenantId));
            
            //建立请求
            String sHtmlText = buildRequest(seller_key,
                    PpPayConfigManager.getCheckoutButtonUrl(), sParaTemp, "post", "确认");
//            LOG.info("向PayPalWEB即时到账交易接口发起支付请求：" + sHtmlText);
//            sHtmlText = "<form id='ppsubmit' name='ppsubmit' action='https://www.sandbox.paypal.com/cgi-bin/webscr' method='post' _input_charset='utf-8'><input type='hidden' name='cmd' value='_xclick'/><input type='hidden' name='business' value='Test-#@gmail.com'/><input type='hidden' name='custom' value='98E1235253A497T678678NBOFH0EBM7O7H5'/><input type='hidden' name='amount' value='21.30'/><input type='hidden' name='currency_code' value='USD'/><input type='hidden' name='on0' value='User Account'/><input type='hidden' name='os0' value='vip@sbs.cn'/><input type='hidden' name='on1' value='Description'/><input type='hidden' name='os1' value='Test Gif - Silicon Scraper($0.01*1), 12-pack Mini Oval Silicone Reusable Baking Cup($6.29*1); deliovery cost:$15.00; '/><input type='hidden' name='notify_url' value='http://test/paypal.htm'/><input type='hidden' name='return' value='http://test/paypal.htm'/><input type='hidden' name='cancel_return' value='http://test/cart.htm'/><input type='hidden' name='cs' value='1'/><input type='hidden' name='address1' value='USA-NewYork-SBCL 12431'/><input type='hidden' name='address2' value='USA-NewYork-SBCL werw'/><input type='submit' value='Go to paypal' style='display:none;'></form><script>document.forms['ppsubmit'].submit();</script>";
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
	
	public static String buildRequest(String key, final String payGateway, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
        //待请求参数数组
        Map<String, String> sPara = buildRequestPara(key, sParaTemp);
        List<String> keys = new ArrayList<String>(sPara.keySet());

        StringBuilder sbHtml = new StringBuilder();

        sbHtml.append("<form id=\"ppsubmit\" name=\"ppsubmit\" action=\"" + payGateway
                       + "\" method=\"" + strMethod
                      + "\">");

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
	
	private static Map<String, String> buildRequestPara(String key, Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
//        String mysign = buildRequestMysign(key, sPara);

        //签名结果与签名方式加入请求提交参数组中
//        sPara.put("sign", mysign);
//        String service = sPara.get("service");
//        if (!"alipay.wap.trade.create.direct".equals(service)
//                && !"alipay.wap.auth.authAndExecute".equals(service)) {
//            sPara.put("sign_type", AliPayConfigManager.SIGN_TYPE);
//        }
        return sPara;
    }
	
	public static String buildRequestMysign(String key, Map<String, String> sPara) {
    	String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        if("MD5".equals(AliPayConfigManager.SIGN_TYPE) ) {
        	mysign = MD5.sign(prestr, key, AliPayConfigManager.INPUT_CHARSET);
        }
        return mysign;
    }
	
	@RequestMapping(value = "/webNotify")
    public void ppWebNotify(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("paypalWEB后台通知...");
        showParams(request);
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            /* 1.获取paypal传递过来的参数 */
            String subject = request.getParameter("subject");// 商品名称
            String trade_no = request.getParameter("trade_no"); // paypal交易号
            String buyer_email = request.getParameter("buyer_email");// 买家paypal账号
            String out_trade_no = request.getParameter("out_trade_no");// 商户网站唯一订单号
            String notify_time = request.getParameter("notify_time");// 通知时间
            String trade_status = request.getParameter("trade_status");//
            String seller_email = request.getParameter("seller_email");// 卖家paypal账号
            String notify_id = request.getParameter("notify_id");// 通知校验ID
                                                                 // 通知校验ID。唯一识别通知内容。重发相同内容的通知时，该值不变。(如果已经成功，则统一个id不处理)
            LOGGER.info("paypalWEB后台通知参数：subject[" + subject + "];trade_no[" + trade_no
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
            
//            String[] orderInfoArray = this.splitTradeOrderId(out_trade_no);
            String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
            String orderId = out_trade_no;//orderInfoArray[1]; 
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
                        trade_no, subject, orderAmount, payStates, PayConstants.PayOrgCode.ZFB);
            }
            
            response.getWriter().write("success"); // paypal接收不到“success” 就会在24小时内重复调用多次
        } catch(IOException ex) {
            LOGGER.error("paypalWEB后台通知失败", ex);
        } catch(Exception ex) {
            LOGGER.error("paypalWEB后台通知失败", ex);
        }    
    }
	
	private void showParams(HttpServletRequest request) {  
        Map map = new HashMap();  
        Enumeration paramNames = request.getParameterNames();  
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
        System.out.println("------------------------------");  
        for (Map.Entry entry : set) {  
            System.out.println(entry.getKey() + ":" + entry.getValue());  
        }  
        System.out.println("------------------------------");  
    }
	
	/**
     * paypalWEB即时到账前台通知地址
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
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
            String result = request.getParameter("is_success");
            String out_trade_no = request.getParameter("out_trade_no");
            String trade_no = request.getParameter("trade_no");
            LOGGER.info("paypalWEB支付前台通知开始:交易订单号[" + out_trade_no + "]");
            LOGGER.info("paypalWEB支付前台通知开始:result[" + result + "]");
            
            String payStates = PayConstants.ReturnCode.FAILD;
            if (PayConstants.AliPayReturnCode.RETURN_URL_T.equals(result)) {
                payStates = PayConstants.ReturnCode.SUCCESS;
            }
            
//            String[] orderInfoArray = this.splitTradeOrderId(out_trade_no);
            String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
            String orderId = out_trade_no;//orderInfoArray[1]; 
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if(tradeRecord == null) {
                LOGGER.error("paypalWEB前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("paypalWEB前台通知出错，获取订单信息失败!");
            }
            String returnUrl = tradeRecord.getReturnUrl();
            String orderAmount = String.format("%.2f", AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); //付款金额 
            
            String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId,
                    orderId, trade_no, tradeRecord.getSubject(), orderAmount, payStates,
                    PayConstants.PayOrgCode.ZFB);
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
