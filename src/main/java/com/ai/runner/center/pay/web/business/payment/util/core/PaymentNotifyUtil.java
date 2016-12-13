package com.ai.runner.center.pay.web.business.payment.util.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ai.opt.sdk.dubbo.util.HttpClientUtil;
import com.ai.opt.sdk.util.DateUtil;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.HTTPUtil;
import com.alibaba.fastjson.JSON;


/**
 * 支付平台通知业务系统帮助类
 *
 * Date: 2015年11月4日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class PaymentNotifyUtil {
    
    private static final Logger LOG = Logger.getLogger(PaymentNotifyUtil.class);

    private PaymentNotifyUtil() {
        
    }
    
    /**
     * 同步通知
     * @param returnUrl
     * @param tenantId
     * @param merchantOrderId
     * @param outOrderId
     * @param subject
     * @param orderAmount
     * @param payStates
     * @param payType
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String notifyClientImmediately(String returnUrl, String tenantId, String merchantOrderId,
            String outOrderId, String subject, String orderAmount, String payStates, String payOrgCode) {
        LOG.info("开始前台通知第三方业务系统");
        Map<String, String> params = buildRequestPara(tenantId, merchantOrderId, outOrderId,
                subject, orderAmount, payStates, payOrgCode);
        LOG.debug("第三方业务系统同步通知跳转网页地址： " + returnUrl);
        LOG.debug("支付平台同步通知第三方业务系统发送报文： " + params);
        String htmlStr = PaymentUtil.generateAutoSubmitForm(returnUrl, params);
        LOG.info("网页报文：" + htmlStr);
        return htmlStr;
    }
    
    /**
     * 异步通知
     * @param notifyUrl
     * @param tenantId
     * @param merchantOrderId
     * @param outOrderId
     * @param subject
     * @param orderAmount
     * @param payStates
     * @param payType
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static void notifyClientAsync(String notifyUrl, String tenantId, String merchantOrderId,
            String outOrderId, String subject, String orderAmount, String payStates, String payType) {
        LOG.info("开始后台通知第三方业务系统");
        Map<String, String> params = buildRequestPara(tenantId, merchantOrderId, outOrderId,
                subject, orderAmount, payStates, payType);
        LOG.debug("第三方业务系统异步通知接收地址： " + notifyUrl);
        LOG.info("支付平台后台通知第三方业务系统发送报文： " + params);
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
//        String returnStr = HttpClientUtil.sendPost(notifyUrl, JSON.toJSONString(params));
        LOG.info("后台通知成功，接收到的返回报文： " + returnStr);
    }
    
    /**
     * 生成要请求给业务系统的参数数组
     * @param tenantId
     * @param merchantOrderId
     * @param outOrderId
     * @param subject
     * @param orderAmount
     * @param payStates
     * @param payType
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private static Map<String, String> buildRequestPara(String tenantId, String merchantOrderId,
            String outOrderId, String subject, String orderAmount, String payStates, String payOrgCode) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        params.put("outOrderId", outOrderId);
        params.put("orderId", merchantOrderId);
        params.put("subject", subject);
        params.put("orderAmount", orderAmount);
        params.put("payStates", payStates);
        params.put("notifyTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        params.put("payOrgCode", payOrgCode);
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        // outOrderId;orderId;orderAmount;payStates;tenantId
        String infoStr = outOrderId + VerifyUtil.SEPARATOR + merchantOrderId + VerifyUtil.SEPARATOR
                + orderAmount + VerifyUtil.SEPARATOR + payStates + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        params.put("infoMd5", infoMd5);
        return params;
    }
    
    /**
     * 通知外部系统单笔退款处理结果
     * @param tenantId
     * @param orderId
     * @param oriOrderId
     * @param refundAmount
     * @param dealState
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static void notifyClientRefundDealResult(String notifyUrl, String tenantId,
            String orderId, String oriOrderId, String refundAmount, String dealState) {
        LOG.info("单笔退款后台通知外部系统开始...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        params.put("orderId", orderId);
        params.put("oriOrderId", oriOrderId);
        params.put("refundAmount", refundAmount);
        params.put("dealResult", dealState);
        params.put("notifyTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        // orderId;oriOrderId;refundAmount;dealResult;tenantId
        String infoStr = orderId + VerifyUtil.SEPARATOR + oriOrderId + VerifyUtil.SEPARATOR
                + refundAmount + VerifyUtil.SEPARATOR + dealState + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        params.put("infoMd5", infoMd5);
        LOG.info("外部系统单笔退款异步通知接收地址： " + notifyUrl);
        LOG.info("支付平台后台单笔退款异步通知外部系统报文： " + params);
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("单笔退款后台通知成功，接收到的返回报文： " + returnStr);
    }
    
    /**
     * 通知外部系统批量退款处理结果
     * @param notifyUrl
     * @param tenantId
     * @param successNum
     * @param resultDetails
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static void notifyClientBatchRefundResultDetails(String notifyUrl, String tenantId,
            String successNum, String resultDetails) {
        LOG.info("批量退款后台通知外部系统开始...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        params.put("successNum", successNum);
        params.put("resultDetails", resultDetails);
        params.put("notifyTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        // successNum;resultDetails;tenantId
        String infoStr = successNum + VerifyUtil.SEPARATOR + resultDetails + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        params.put("infoMd5", infoMd5);
        LOG.info("外部系统批量退款异步通知接收地址： " + notifyUrl);
        LOG.info("支付平台后台批量退款异步通知外部系统报文： " + params);
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("批量退款后台通知成功，接收到的返回报文： " + returnStr);
    }
    
    /**
     * 通知外部系统单笔提现结果
     * @param notifyUrl
     * @param tenantId
     * @param orderId
     * @param outOrderId
     * @param takeAmount
     * @param dealState
     * @param payOrgCode
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static void notifyClientWithDrawDealResult(String notifyUrl, String tenantId,
            String orderId, String outOrderId, String takeAmount, String dealState, String payOrgCode) {
        LOG.info("单笔提现后台通知外部系统开始...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        params.put("orderId", orderId);
        params.put("outOrderId", outOrderId);
        params.put("takeAmount", takeAmount);
        params.put("dealResult", dealState);
        params.put("notifyTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        params.put("payOrgCode", payOrgCode);
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        // orderId;outOrderId;payOrgCode;takeAmount;dealResult;tenantId
        String infoStr = orderId + VerifyUtil.SEPARATOR + outOrderId + VerifyUtil.SEPARATOR + payOrgCode + VerifyUtil.SEPARATOR
                + takeAmount + VerifyUtil.SEPARATOR + dealState + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        params.put("infoMd5", infoMd5);
        LOG.info("外部系统单笔提现异步通知接收地址： " + notifyUrl);
        LOG.info("支付平台后台单笔提现异步通知外部系统报文： " + params);
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("单笔提现后台通知成功，接收到的返回报文： " + returnStr);
    }
}
