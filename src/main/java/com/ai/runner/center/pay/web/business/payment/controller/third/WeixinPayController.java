package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.controller.core.TradeBaseController;
import com.ai.runner.center.pay.web.business.payment.model.CommonPayRes;
import com.ai.runner.center.pay.web.business.payment.model.PaymentReqParam;
import com.ai.runner.center.pay.web.business.payment.model.RefundRes;
import com.ai.runner.center.pay.web.business.payment.model.TradeQueryRes;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentNotifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.weixin.CommonUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.weixin.WeixinOauth2Token;
import com.ai.runner.center.pay.web.constants.PayCenterConstants;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.WeixinConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants.PayOrgCode;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigFromFileUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.HTTPSUtil;
import com.ai.runner.center.pay.web.system.util.XMLUtil;
import com.alibaba.fastjson.JSON;

/**
 * 微信支付controller Date: 2015年11月30日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
@Controller
@RequestMapping(value = "/weixin")
public class WeixinPayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(WeixinPayController.class);

    /** web支付前台通知地址 **/
    private static final String WEIXIN_CODE_URL = "/weixin/wxCode";

    /** web支付后台通知地址 **/
    private static final String WEIXIN_NOTIFY_URL = "/weixin/wxNotify";

    /** 授权回调地址 **/
    private static final String WEIXIN_BACK_URL = "/weixin/backUrl";

    /**
     * PC端支付应用，用于生成
     * 
     * @param request
     * @param response
     * @return
     * @author LiangMeng
     */
    @RequestMapping("/wxCode")
    public String wxCode(@RequestParam(value = "tenantId", required = true)
    String tenantId, @RequestParam(value = "orderId", required = true)
    String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("微信扫码支付开始:订单号[" + orderId + " ，租户标识： " + tenantId);
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

        LOG.info("微信扫码支付参数：" + "orderAmount:" + tradeRecord.getPayAmount() + "orderId:" + orderId);
        String xml = this.initRequestInfo(request, tenantId, tradeRecord.getTradeOrderId(),
                tradeRecord.getPayAmount(), PayConstants.WeixinTradeType.NATIVE);
        LOG.info("请求报文:" + xml + "");
        String createOrderURL = WeixinConfigManager.WEIXIN_PAY_GATEWAY;
        String codeUrl = CommonUtil.getCodeUrl(createOrderURL, xml);
        if (StringUtil.isBlank(codeUrl)) {
            request.setAttribute("errorMsg", "统一支付接口获取预支付订单出错");
            throw new SystemException("统一支付接口获取预支付订单出错");
        }
        LOG.info("codeUrl:[" + codeUrl + "]");
        String partnerkey = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        infoMd5 = VerifyUtil.encodeParam(infoStr, partnerkey);
        request.setAttribute("codeurl", codeUrl);
        request.setAttribute("orderId", orderId);
        request.setAttribute("checkFlag", "0");
        request.setAttribute("tenantId", tenantId);
        request.setAttribute("orderAmount", tradeRecord.getPayAmount());
        request.setAttribute("requestSource", tradeRecord.getRequestSource());
        request.setAttribute("infoMd5", infoMd5);
        request.setAttribute("returnUrl", tradeRecord.getReturnUrl());
        request.setAttribute("notifyUrl", tradeRecord.getNotifyUrl());
        return WEIXIN_CODE_URL;
    }

    /**
     * 前台通知请求端
     * 
     * @param request
     * @param response
     * @throws Exception
     * @author LiangMeng
     * @ApiDocMethod
     */
    @RequestMapping(value = "/wxReturn")
    public void wxReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=utf-8");
        /* 1.获取微信传递过来的参数 */
        String data = request.getParameter("data");
        TradeQueryRes res = JSON.parseObject(data, TradeQueryRes.class);
        String orderId = res.getOrderId();
        String tenantId = res.getTenantId();
        String out_trade_no = res.getOutOrderId(); 
        LOG.info("微信支付前台通知开始:订单号[" + orderId + "]"); 

        String payStates = PayConstants.ReturnCode.SUCCESS;
        TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
        if (tradeRecord == null) {
            LOG.error("微信支付前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
            throw new SystemException("微信支付前台通知出错，获取订单信息失败!");
        }
        
        /* 2.取商户的前台通知服务地址 */
        String returnUrl = tradeRecord.getReturnUrl();
        String subject = tradeRecord.getSubject();
        String orderAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
        /* 3.前台通知 */
        String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId, orderId,
                out_trade_no, subject, orderAmount, payStates,
                PayConstants.PayOrgCode.WEIXIN);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(htmlStr);
    }

    /**
     * 后台通知请求端
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author LiangMeng
     */
    @RequestMapping(value = "/wxNotify")
    public void wxNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("微信支付后台通知...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            /* 1.获取微信传递过来的参数 */
            Map<String, String> requestInfo = CommonUtil.reciveNotifyInfo(request);
            if (requestInfo != null) {
                String out_trade_no = requestInfo.get("out_trade_no");
                LOG.info("微信支付回调参数orderId：[" + out_trade_no + "]");
                String return_code = requestInfo.get("return_code");
                LOG.info("微信支付回调参数return_code：[" + return_code + "]");
                String transaction_id = requestInfo.get("transaction_id");
                String result_code = requestInfo.get("result_code");// 业务状态
                LOG.info("微信支付回调参数result_code：[" + result_code + "]");
                /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
                LOG.info("微信后台通知执行回调：[" + out_trade_no + "]");
                LOG.info("微信后台通知结果：["
                        + (PayConstants.WeixinReturnCode.SUCCESS.equals(return_code) && PayConstants.WeixinReturnCode.SUCCESS
                                .equals(result_code)) + "]");
                /* 3.1.取支付流水信息 */
                String payStates = PayConstants.ReturnCode.FAILD;
                if (PayConstants.WeixinReturnCode.SUCCESS.equals(return_code)
                        && PayConstants.WeixinReturnCode.SUCCESS.equals(result_code)) {
                    payStates = PayConstants.ReturnCode.SUCCESS;
                }
                /* 解析第三方平台返回的orderId */
//                String[] orderInfoArray = this.splitTradeOrderId(out_trade_no);
                String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
                String orderId = out_trade_no;//orderInfoArray[1]; 
                TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
                if (tradeRecord == null) {
                    LOG.error("微信扫码支付后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                    throw new SystemException("微信扫码支付后台出错，获取订单信息失败!");
                }
                String notifyUrl = tradeRecord.getNotifyUrl();
                String orderAmount = String.format("%.2f",
                        AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
                String subject = tradeRecord.getSubject();
                /* 4.判断是否已经回调过，如果不是同一个回调更新支付流水信息，否则什么都不做 */
                LOG.info("微信后台通知更新订单状态befor：[" + out_trade_no + ";" + tradeRecord.getStatus() + "]");
                if (tradeRecord.getStatus() != null
                        && PayConstants.Status.APPLY == tradeRecord.getStatus()) {
                    this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                            transaction_id, null, null, null, null, PayOrgCode.WEIXIN);
                    /* 5.异步通知业务系统订单支付状态 */
                    PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId,
                            transaction_id, subject, orderAmount, payStates,
                            PayConstants.PayOrgCode.WEIXIN);
                }

                printWriter.write(PayConstants.WeixinReturnCode.SUCCESS);//
                printWriter.flush();
                printWriter.close();
            }
        } catch (Exception e) {
            LOG.error("微信扫码支付后台通知失败", e);
        }
    }

    /**
     * 微信端支付授权(适用于app和微信浏览器)
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws SDKRuntimeException
     * @author LiangMeng
     */
    @RequestMapping("/payAuthorization")
    public void payAuthorization(HttpServletRequest request, HttpServletResponse response,
            PaymentReqParam paymentReqParam) throws Exception {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        /* 1.首先接收调用端传递过来的参数 */
        this.validatePaymentReq(paymentReqParam);
        String orderId = paymentReqParam.getOrderId();
        LOG.info("跳转到支付平台选择action：订单号[" + orderId + "]");
        String tenantId = paymentReqParam.getTenantId();
        String subject = paymentReqParam.getSubject();
        String notifyUrl = paymentReqParam.getNotifyUrl();
        String orderAmount = paymentReqParam.getOrderAmount();
        String returnUrl = paymentReqParam.getReturnUrl();
        String requestSource = paymentReqParam.getReturnUrl();
        String merchantUrl = paymentReqParam.getMerchantUrl();
        String basePath = AbstractPayConfigManager.getPayUrl();
        String backUri = basePath + WEIXIN_BACK_URL;

        // backUri = "http://mvnowxpay.minshengec.com/MVNO-PAY/weixin/toPay";
        backUri = backUri + "?tenantId=" + tenantId + "&orderId=" + orderId + "&orderAmount="
                + orderAmount + "&notifyUrl=" + notifyUrl + "&returnUrl=" + returnUrl
                + "&requestSource=" + requestSource + "&subject=" + subject + "&merchantUrl="
                + merchantUrl;
        LOG.info("{appid:" + ConfigUtil.getProperty("WEIXIN_APPID") + "}{backUri:" + backUri
                + "}{orderId:" + orderId + "}");
        // URLEncoder.encode 后可以在backUri 的url里面获取传递的所有参数
        backUri = URLEncoder.encode(backUri, "utf-8");
        // scope=snsapi_base 不弹出授权页面直接授权目的只获取统一支付接口的openid
        String url = WeixinConfigManager.WEIXIN_AUTH_GATEWAY
                + "?appid="
                + ConfigUtil.getProperty(tenantId, WeixinConfigManager.PAY_ORG_NAME,
                        WeixinConfigManager.WEIXIN_APPID) + "&redirect_uri=" + backUri
                + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
        LOG.info("授权跳转！！！！");
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            LOG.error("授权失败！！！！", e);
        }
    }

    /**
     * 微信支付统一支付接口
     * 
     * @param request
     * @param response
     * @return
     * @author LiangMeng
     */
    @RequestMapping("/backUrl")
    public String backUrl(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("微信支付微信端支付");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            String code = request.getParameter("code");
            String tenantId = request.getParameter("tenantId");
            WeixinOauth2Token weixinOauth = CommonUtil.refreshOauth2AccessToken(code, tenantId);
            String openId = weixinOauth.getOpenId();
            LOG.info("授权后的openId:" + openId);
            /* 1.首先接收调用端传递过来的参数 */
            String orderId = request.getParameter("orderId");
            LOG.info("微信支付手机端action：订单号[" + orderId + "]");
            String subject = request.getParameter("subject");
            LOG.debug("微信支付手机端subject转换前：[" + subject + "]");
            subject = new String((request.getParameter("subject")).getBytes("ISO-8859-1"), "UTF-8");
            LOG.debug("微信支付手机端subject转换后：[" + subject + "]");
            String notifyUrl = request.getParameter("notifyUrl");
            String orderAmount = request.getParameter("orderAmount");
            String returnUrl = request.getParameter("returnUrl");
            String requestSource = request.getParameter("requestSource");
            String merchantUrl = request.getParameter("merchantUrl");

            String partnerId = this.getPartnerId(tenantId);
            /* 2.沉淀订单信息 */
            /* 先处理不存在的 * */
            this.createPaymentInfo(tenantId, orderId, orderAmount, subject, requestSource,
                    notifyUrl, merchantUrl, returnUrl, partnerId, PayCenterConstants.CurrencyUnit.RMB, "");

            LOG.info("微信支付微信端参数：" + "orderAmount:" + orderAmount + "orderId:" + orderId);
            String xml = this.initRequestInfo(request, tenantId, orderId,
                    AmountUtil.changeYuanToLi(Double.valueOf(orderAmount)),
                    PayConstants.WeixinTradeType.NATIVE);
            LOG.info("请求报文:" + xml + "");
            String createOrderURL = ConfigUtil.getProperty("WEIXIN_PAY_GATEWAY");
            String prepayId = null;
            try {
                JSONObject prepayIdJson = CommonUtil.getPrepayId(createOrderURL, xml);
                prepayId = prepayIdJson.getString("MSG");
                if (!"OK".equals(prepayIdJson.getString("STATE"))) {
                    request.setAttribute("errorMsg", "统一支付接口获取预支付订单出错");
                }
                LOG.info("prepayId:[" + prepayId + "]");
                String nonce_str = CommonUtil.getNonceStr();
                SortedMap<String, String> finalpackage = new TreeMap<String, String>();
                finalpackage.put("appId", ConfigUtil.getProperty("WEIXIN_APPID"));
                finalpackage.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
                finalpackage.put("nonceStr", nonce_str);
                finalpackage.put("package", "prepay_id=" + prepayId);
                finalpackage.put("signType", "MD5");
                String finalSign = CommonUtil.createSign(finalpackage, ConfigUtil.getProperty(
                        tenantId, WeixinConfigManager.PAY_ORG_NAME,
                        WeixinConfigManager.WEIXIN_API_KEY));
                finalpackage.put("sign", finalSign);
                /* 组织跳转密码输入页面参数 */
                request.setAttribute("appId", ConfigUtil.getProperty("WEIXIN_APPID"));
                request.setAttribute("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
                request.setAttribute("nonceStr", nonce_str);
                request.setAttribute("packageValue", "prepay_id=" + prepayId);
                request.setAttribute("signType", "MD5");
                request.setAttribute("sign", finalSign);

            } catch (Exception e) {
                LOG.error("统一支付接口获取预支付订单出错" + e);
            }
        } catch (Exception e) {
            LOG.error("微信支付出错", e);
        }
        return "/weixin/toPay";
    }

    /**
     * PC端支付应用，用于生成
     * 
     * @param request
     * @param response
     * @return
     * @author LiangMeng
     */
    @BackTransService
    @ResponseBody
    @RequestMapping("/wxAppPay")
    public CommonPayRes wxAppPay(@RequestParam(value = "tenantId", required = true)
    String tenantId, @RequestParam(value = "orderId", required = true)
    String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("微信APP支付开始:订单号[" + orderId + " ，租户标识： " + tenantId);
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

        LOG.info("微信APP支付参数：" + "orderAmount:" + tradeRecord.getPayAmount() + "orderId:" + orderId);
        String createOrderURL = WeixinConfigManager.WEIXIN_PAY_GATEWAY;
        String reqXml = this.initRequestInfo(request, tenantId, tradeRecord.getTradeOrderId(),
                tradeRecord.getPayAmount(), PayConstants.WeixinTradeType.APP);
        String xmlResult = HTTPSUtil.postXml(createOrderURL, reqXml, false, null, null);
        LOG.info("微信app支付获取预订单返回报文:" + xmlResult);
        if (StringUtil.isBlank(xmlResult)) {
            throw new SystemException("微信app支付获取预订单失败，可能原因： 网络错误.");
        }
        Map<String,String> map = XMLUtil.readStringXmlOut(xmlResult);
        String return_code = map.get("return_code");
        String result_code = map.get("result_code");
        String basePath = AbstractPayConfigManager.getPayUrl();
        String payCenterNotifyUrl = basePath + WEIXIN_NOTIFY_URL;
        String orderAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
        CommonPayRes commonPayRes = new CommonPayRes();
        commonPayRes.setTenantId(tenantId);
        commonPayRes.setOrderId(orderId);
        commonPayRes.setPayOrgCode(PayConstants.PayOrgCode.WEIXIN);
        commonPayRes.setOrderAmount(orderAmount);
        commonPayRes.setPayCenterNotifyUrl(payCenterNotifyUrl);
        commonPayRes.setPayCenterOrderId(tradeRecord.getTradeOrderId());
        commonPayRes.setRequestSource(tradeRecord.getRequestSource());
        commonPayRes.setSubject(tradeRecord.getSubject());
        if(PayConstants.WeixinReturnCode.SUCCESS.equals(return_code)
                && PayConstants.WeixinReturnCode.SUCCESS.equals(result_code)){
            String prepay_id = map.get("prepay_id");
            LOG.info("微信app支付返回的预订单号： " + prepay_id);
            commonPayRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
            commonPayRes.setPreOrderId(prepay_id);
        } else {
            String return_msg = map.get("return_msg");
            String err_code_des = map.get("err_code_des");
            String message = "微信app支付获取预订单返回信息：" + return_msg + ", 错误信息详情：" + err_code_des;
            commonPayRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            commonPayRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            commonPayRes.setErrMsg(message);
        }
        
        return commonPayRes;
    }

    /**
     * 微信退款接口
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/refund")
    public RefundRes refund(@RequestParam(value = "tenantId", required = true)
            String tenantId, @RequestParam(value = "orderId", required = true)
            String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("微信退款操作开始,退款订单号： " + orderId);
        TradeRecord refundRecord = this.queryTradeRecord(tenantId, orderId);
        if (refundRecord == null) {
            LOG.error("退款失败，查询退款沉淀信息出错： 租户标识： " + tenantId + "，订单号： " + orderId);
            throw new SystemException("退款失败，查询退款沉淀信息出错!");
        }

        String oriOrderId = refundRecord.getOriOrderId();
        TradeRecord originRecord = this.queryTradeRecord(tenantId, oriOrderId);
        if (originRecord == null) {
            LOG.error("退款失败，传入的原始订单号有误： 租户标识： " + tenantId + "，订单号： " + oriOrderId);
            throw new BusinessException(ExceptCodeConstants.TRADE_NOT_EXIST, "退款失败，交易不存在！");
        }

        long refundLiAmount = Math.abs(refundRecord.getPayAmount());
        String refundFee = String.valueOf(AmountUtil.changeLiToFen(refundLiAmount));
        long totalLiAmount = Math.abs(originRecord.getPayAmount());
        String totalFee = String.valueOf(AmountUtil.changeLiToFen(totalLiAmount));
        String refundReqXml = this.initWxRefundReq(tenantId, refundRecord.getTradeOrderId(),
                originRecord.getTradeOrderId(), totalFee, refundFee);
        String refundUrl = WeixinConfigManager.WEIXIN_REFUND_GATEWAY;
        String certLocalPath = ConfigUtil.getProperty(tenantId, WeixinConfigManager.PAY_ORG_NAME,
                WeixinConfigManager.WEIXIN_CER_PATH);
        LOG.info("微信退款证书路径： " + certLocalPath);
        String certPassword = ConfigUtil.getProperty(tenantId, WeixinConfigManager.PAY_ORG_NAME,
                WeixinConfigManager.WEIXIN_MCH_ID);
        String responseStr = HTTPSUtil.postXml(refundUrl, refundReqXml, true, certLocalPath,
                certPassword);
        if (StringUtil.isBlank(responseStr)) {
            throw new SystemException("微信退款失败，可能原因： 网络错误或者证书加载失败.");
        }
        LOG.info("微信退款返回报文：" + responseStr);
        Map<String, String> resmap = XMLUtil.readStringXmlOut(responseStr);
        String return_code = resmap.get("return_code");
        String result_code = resmap.get("result_code");
        int status = PayConstants.Status.REFUND_FAIL;
        RefundRes refundRes = new RefundRes();
        refundRes.setTenantId(tenantId);
        refundRes.setOrderId(orderId);
        refundRes.setOriOrderId(refundRecord.getOriOrderId());
        String refundAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(Math.abs(refundRecord.getPayAmount())));
        refundRes.setRefundAmount(refundAmount);
        if (PayConstants.WeixinReturnCode.SUCCESS.equals(return_code)
                && PayConstants.WeixinReturnCode.SUCCESS.equals(result_code)) {
            LOG.info("微信退款申请已成功受理，订单号： " + orderId);
            status = PayConstants.Status.REFUND_SUCCESS;
            refundRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            String err_code = resmap.get("err_code");
            String err_code_des = resmap.get("err_code_des");
            LOG.error("微信退款申请受理失败，错误： " + err_code + "错误代码描述： " + err_code_des);
            refundRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            refundRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            refundRes.setErrMsg(err_code_des);
        }

        String refund_id = resmap.get("refund_id");
        this.modifyTradeState(tenantId, orderId, status, refund_id, null, null, null, null, null,
                responseStr, PayOrgCode.WEIXIN);
        LOG.debug("修改退款单状态成功");
        return refundRes;
    }

    /**
     * 组装微信退款请求报文
     * 
     * @param tenantId
     * @param tradeOrderId
     * @param oriTradeOrderId
     * @param totalFee
     * @param refundFee
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private String initWxRefundReq(String tenantId, String tradeOrderId, String oriTradeOrderId,
            String totalFee, String refundFee) {
        /* 1.将请求传递过来的参数，放到微信请求里 */
        String nonce_str = CommonUtil.getNonceStr();
        String appId = ConfigUtil.getProperty(tenantId, WeixinConfigManager.PAY_ORG_NAME,
                WeixinConfigManager.WEIXIN_APPID);
        String mchId = ConfigUtil.getProperty(tenantId, WeixinConfigManager.PAY_ORG_NAME,
                WeixinConfigManager.WEIXIN_MCH_ID);
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", appId);
        packageParams.put("mch_id", mchId);
        packageParams.put("nonce_str", nonce_str);// 随机数
        packageParams.put("out_trade_no", oriTradeOrderId);
        packageParams.put("out_refund_no", tradeOrderId);
        packageParams.put("total_fee", totalFee);
        packageParams.put("refund_fee", refundFee);
        packageParams.put("op_user_id", mchId);
        String sign = CommonUtil.createSign(packageParams, ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_API_KEY));
        packageParams.put("sign", sign);

        String xml = null;
        try {
            xml = CommonUtil.getRequestXml(packageParams);
        } catch (UnsupportedEncodingException ex) {
            LOG.error("获取微信退款请求报文出错", ex);
            throw new SystemException("获取微信退款请求报文出错");
        }

        LOG.info("请求报文:" + xml + "");
        return xml;
    }

    /**
     * 构造请求报文
     * 
     * @param request
     * @param tenantId
     * @param tradeRecord
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    private String initRequestInfo(HttpServletRequest request, String tenantId,
            String tradeOrderId, long payAmount, String tradeType) {
        String basePath = AbstractPayConfigManager.getPayUrl();
        String nonce_str = CommonUtil.getNonceStr();
        String local_ip = request.getRemoteAddr();
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_APPID));
        packageParams.put("mch_id", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_MCH_ID));
        packageParams.put("nonce_str", nonce_str);// 随机数
        packageParams.put("body", "微信支付");
        packageParams.put("out_trade_no", tradeOrderId); // 交易订单号
        packageParams.put("total_fee", String.valueOf(AmountUtil.changeLiToFen(payAmount)));
        packageParams.put("spbill_create_ip", local_ip);
        packageParams.put("trade_type", tradeType);
        packageParams.put("notify_url", basePath + WEIXIN_NOTIFY_URL);
        packageParams.put("attach", "att1");
        packageParams.put("device_info", "APP");
        String sign = CommonUtil.createSign(packageParams, ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_API_KEY));
        packageParams.put("sign", sign);

        String xml = null;
        try {
            xml = CommonUtil.getRequestXml(packageParams);
        } catch (UnsupportedEncodingException ex) {
            LOG.error("获取微信支付请求xml报文出错", ex);
        }
        LOG.info("请求报文:" + xml + "");
        return xml;
    }

    /**
     * 业务系统支付传入参数校验
     * 
     * @param paymentReqParam
     * @throws BusinessException
     * @author LiangMeng
     * @ApiDocMethod
     * @ApiCode
     */
    private void validatePaymentReq(PaymentReqParam paymentReqParam) throws BusinessException {
        final String errMsg = "支付传入参数有误：";
        String tenantId = paymentReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getOrderId())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单号不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getRequestSource())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "终端来源不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getOrderAmount())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单金额不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getNotifyUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg
                    + "服务器异步通知页面路径不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }

        // orderId;orderAmount;notifyUrl;tenantId
        // (订单号，订单金额，服务后台通知路径，租户ID四个关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = paymentReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + paymentReqParam.getOrderAmount() + VerifyUtil.SEPARATOR
                + paymentReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if (!VerifyUtil.checkParam(infoStr, paymentReqParam.getInfoMd5(), key)) {
            LOG.error("验签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
        }
    }
}
