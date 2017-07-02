package com.ai.runner.center.pay.web.business.payment.controller.third;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ai.opt.sdk.util.DateUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.controller.core.TradeBaseController;
import com.ai.runner.center.pay.web.business.payment.model.CommonPayRes;
import com.ai.runner.center.pay.web.business.payment.model.RefundRes;
import com.ai.runner.center.pay.web.business.payment.model.WithdrawRes;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentNotifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.PaymentUtil;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.WapUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.sdk.SDKConstants;
import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.sdk.SDKUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.configcenter.YlPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants.PayOrgCode;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigFromFileUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSON;

/**
 * 银联支付
 * 
 * Date: 2015年12月7日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/ylpay")
public class YlPayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(YlPayController.class);

    /**
     * wap支付前台通知地址
     */
    private static final String WAP_RETURN_URL = "/ylpay/wapReturn";

    /**
     * wap支付后台通知地址
     */
    private static final String WAP_NOTIFY_URL = "/ylpay/wapNotify";

    /**
     * 退货交易后台通知地址
     */
    private static final String REFUND_NOTIFY_URL = "/ylpay/refundNotify";

    /**
     * 银联单笔代付后台通知地址
     */
    private static final String WITH_DRAW_NOTIFY_URL = "/ylpay/withdrawNotify";

    /**
     * 银联WAP网页支付
     * 
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/wapPay")
    public void wapPay(@RequestParam(value = "tenantId", required = true)
    String tenantId, @RequestParam(value = "orderId", required = true)
    String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter printWriter = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            LOG.info("银联wap支付开始:商户订单号[" + orderId + "]" + " ，租户标识： " + tenantId);
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

            String basePath = AbstractPayConfigManager.getPayUrl();
            // 组装请求报文
            Map<String, String> data = new HashMap<String, String>();
            // 版本号
            data.put("version", "5.0.0");
            // 字符集编码 默认"UTF-8"
            data.put("encoding", "UTF-8");
            // 签名方法 01 RSA
            data.put("signMethod", "01");
            // 交易类型 01-消费
            data.put("txnType", "01");
            // 交易子类型 01:自助消费 02:订购 03:分期付款
            data.put("txnSubType", "01");
            // 业务类型
            data.put("bizType", "000201");
            // 渠道类型，07-PC，08-手机
            data.put("channelType", "08");
            // 前台通知地址 ，控件接入方式无作用
            data.put("frontUrl", basePath + WAP_RETURN_URL);
            // 后台通知地址
            data.put("backUrl", basePath + WAP_NOTIFY_URL);
            // 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
            data.put("accessType", "0");
            // 商户号码，请改成自己的商户号
            data.put("merId", ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                    YlPayConfigManager.MERID));
            // 商户订单号，8-40位数字字母
            data.put("orderId", tradeRecord.getTradeOrderId());
            // 订单发送时间，取系统时间
            data.put("txnTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
            // 交易金额，单位分
            data.put("txnAmt", String.valueOf(AmountUtil.changeLiToFen(tradeRecord.getPayAmount())));
            // 交易币种
            data.put("currencyCode", "156");
            // 请求方保留域，透传字段（可以实现商户自定义参数的追踪）本交易的后台通知
            data.put("reqReserved", tenantId);
            String certPath = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                    YlPayConfigManager.SDK_SIGNCERT_PATH);
            String certPwd = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                    YlPayConfigManager.SDK_SIGNCERT_PWD);
            Map<String, String> wapPayReqData = SDKUtil.signData(data, certPath, certPwd,
                    YlPayConfigManager.DEFAULT_CHARSET);
            // 建立请求
            String requestFrontUrl = ConfigUtil.getProperty(YlPayConfigManager.PAY_ORG_NAME,
                    YlPayConfigManager.FRONT_TRANS_URL);
            String sHtmlText = PaymentUtil.generateAutoSubmitForm(requestFrontUrl, wapPayReqData);
            LOG.info("跳转到银联wap支付页面：" + sHtmlText);
            printWriter.println(sHtmlText);
            printWriter.flush();
        } catch (IOException ex) {
            LOG.error("银联wap支付发生错误", ex);
            throw ex;
        } catch (Exception ex) {
            LOG.error("银联wap支付发生错误", ex);
            throw ex;
        }
    }

    /**
     * 银联wap支付前台通知
     * 
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/wapReturn")
    public void wapReturn(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOG.debug("银联wap支付前台通知...");
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            String encoding = request.getParameter(SDKConstants.param_encoding);
            Map<String, String> params = WapUtil.getAllRequestParam(request);
            LOG.info("银联wap支付前台通知：" + params);
            if (!SDKUtil.validate(params, encoding)) {
                throw new SystemException("银联wap支付前台通知延签失败！");
            }

            String tenantId = params.get("reqReserved"); 
            String orderId = params.get("orderId");
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("银联手机网页支付前台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("银联手机网页支付前台通知出错，获取订单信息失败!");
            }
            String returnUrl = tradeRecord.getReturnUrl();
            String orderAmount = String.format("%.2f",
                    AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
            String payStates = PayConstants.ReturnCode.FAILD;
            String trade_no = "";
            if (PayConstants.ReturnCode.SUCCESS.equals(params.get("respCode"))
                    && String.valueOf(AmountUtil.changeLiToFen(tradeRecord.getPayAmount())).equals(
                            params.get("txnAmt"))) {
                payStates = PayConstants.ReturnCode.SUCCESS;
                trade_no = params.get("queryId");
            }

            String htmlStr = PaymentNotifyUtil.notifyClientImmediately(returnUrl, tenantId,
                    orderId, trade_no, tradeRecord.getSubject(), orderAmount, payStates,
                    PayConstants.PayOrgCode.YL);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(htmlStr);
        } catch (IOException ex) {
            LOG.error("银联wap支付前台通知失败", ex);
            throw ex;
        } catch (Exception ex) {
            LOG.error("银联wap支付前台通知失败", ex);
            throw ex;
        }
    }

    /**
     * 银联移动支付后台通知服务
     * 
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/wapNotify")
    public void wapNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("银联移动支付后台通知...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            String encoding = request.getParameter(SDKConstants.param_encoding);
            Map<String, String> params = WapUtil.getAllRequestParam(request);
            LOG.info("银联移动支付后台通知参数：" + params);
            if (!SDKUtil.validate(params, encoding)) {
                throw new SystemException("银联移动支付后台通知延签失败！");
            }

            String tenantId = params.get("reqReserved"); 
            String orderId = params.get("orderId");
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("银联移动支付后台通知出错，获取订单信息失败： 租户标识： " + tenantId + " ，订单号： " + orderId);
                throw new SystemException("银联移动支付后台通知出错，获取订单信息失败!");
            }
            String payStates = PayConstants.ReturnCode.FAILD;
            String trade_no = "";
            if (PayConstants.ReturnCode.SUCCESS.equals(params.get("respCode"))
                    && String.valueOf(AmountUtil.changeLiToFen(tradeRecord.getPayAmount())).equals(
                            params.get("txnAmt"))) {
                payStates = PayConstants.ReturnCode.SUCCESS;
                trade_no = params.get("queryId");
            }

            /* 3.如果成功，更新支付流水并回调请求端，否则什么也不做 */
            if (!PayConstants.ReturnCode.SUCCESS.equals(payStates)) {
                return;
            }
            String notifyUrl = tradeRecord.getNotifyUrl();
            String subject = tradeRecord.getSubject();
            String orderAmount = String.format("%.2f",
                    AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
            // 更新流水记录
            this.modifyTradeState(tenantId, orderId, PayConstants.Status.PAYED_SUCCESS,
                    trade_no, null, null, null, null, PayOrgCode.YL);
            /*
            查询是否是异常支付,查询支付中心流水表，按订单号查询支付状态是否为申请支付状态，如果是，将此订单支付状态修改为支付完成，
            记录到支付中心流水表；如果不是申请支付状态，对比支付中心流水表中现有记录的第三方平台交易流水号和第三方平台返回的第三方平台交易流水号，
            如果两者都相同则不进行任何操作；如果两者不同，则记录到异常支付单表中
            */
            if (tradeRecord.getStatus() != null
                    && PayConstants.Status.APPLY != tradeRecord.getStatus()){
                LOG.info("如果两者都相同则不进行任何操作；如果两者不同，则记录到异常支付单表中。异常支付订单状态===tradeRecordStatus"+tradeRecord.getStatus()+"PayConstants.Status.APPLY="+PayConstants.Status.APPLY);
                if (!tradeRecord.getTradeOrderId().equals(trade_no)){
                    this.createExceptionTrade(tenantId,orderId,PayConstants.Status.PAYED_SUCCESS,trade_no,PayOrgCode.YL);
                }
                response.getWriter().write("ok");
                return;
            }
            /* 5.异步通知业务系统订单支付状态 */
            PaymentNotifyUtil.notifyClientAsync(notifyUrl, tenantId, orderId, trade_no,
                    subject, orderAmount, payStates, PayConstants.PayOrgCode.YL);
            // 返回给银联服务器http 200 状态码
            printWriter.print("ok");
            printWriter.flush();
        } catch (Exception ex) {
            LOG.error("银联移动支付后台通知失败", ex);
        }

    }

    /**
     * 银联app支付
     * 
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/appPay")
    public CommonPayRes appPay(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId, 
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("银联app支付开始:商户订单号[" + orderId + "]" + "，租户标识： " + tenantId);
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

        Map<String, String> resmap = this.getTn(tenantId, tradeRecord);
        LOG.info("银联app支付获取tn号交易返回报文：" + resmap);
        if (resmap == null) {
            throw new SystemException("获取调起控件的tn号失败！");
        }

        if (!SDKUtil.validate(resmap, YlPayConfigManager.DEFAULT_CHARSET)) {
            throw new SystemException("银联app支付获取tn号交易返回报文延签失败！");
        }

        String basePath = AbstractPayConfigManager.getPayUrl();
        String payCenterNotifyUrl = basePath + WAP_NOTIFY_URL;
        String orderAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(tradeRecord.getPayAmount())); // 付款金额
        CommonPayRes commonPayRes = new CommonPayRes();
        commonPayRes.setTenantId(tenantId);
        commonPayRes.setOrderId(orderId);
        commonPayRes.setPayOrgCode(PayConstants.PayOrgCode.YL);
        commonPayRes.setOrderAmount(orderAmount);
        commonPayRes.setPayCenterNotifyUrl(payCenterNotifyUrl);
        commonPayRes.setPayCenterOrderId(tradeRecord.getTradeOrderId());
        commonPayRes.setRequestSource(tradeRecord.getRequestSource());
        commonPayRes.setSubject(tradeRecord.getSubject());
        if (PayConstants.ReturnCode.SUCCESS.equals(resmap.get("respCode"))) {
            String tn = resmap.get("tn");
            LOG.info("银联app支付返回的tn号： " + tn);
            commonPayRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
            commonPayRes.setPreOrderId(tn);
        } else {
            String message = "银联app支付返回错误码： " + resmap.get("respCode") + ", 错误详情： "
                    + resmap.get("respMsg");
            LOG.error(message);
            commonPayRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            commonPayRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            commonPayRes.setErrMsg(message);
        }
        
        return commonPayRes;
    }

    /**
     * 获取调起控件的tn号
     * 
     * @param tenantId
     * @param tradeRecord
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private Map<String, String> getTn(String tenantId, TradeRecord tradeRecord) {
        String basePath = AbstractPayConfigManager.getPayUrl();
        // 组装请求报文
        Map<String, String> data = new HashMap<String, String>();
        // 版本号
        data.put("version", "5.0.0");
        // 字符集编码 默认"UTF-8"
        data.put("encoding", "UTF-8");
        // 签名方法 01 RSA
        data.put("signMethod", "01");
        // 交易类型 01-消费
        data.put("txnType", "01");
        // 交易子类型 01:自助消费 02:订购 03:分期付款
        data.put("txnSubType", "01");
        // 业务类型
        data.put("bizType", "000201");
        // 渠道类型，07-PC，08-手机
        data.put("channelType", "08");
        // 后台通知地址
        data.put("backUrl", basePath + WAP_NOTIFY_URL);
        // 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
        data.put("accessType", "0");
        // 商户号码，请改成自己的商户号
        data.put("merId", ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.MERID));
        // 商户订单号，8-40位数字字母
        data.put("orderId", tradeRecord.getTradeOrderId());
        // 订单发送时间，取系统时间
        data.put("txnTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        // 交易金额，单位分
        data.put("txnAmt", String.valueOf(AmountUtil.changeLiToFen(tradeRecord.getPayAmount())));
        // 交易币种
        data.put("currencyCode", "156");
        // 请求方保留域，透传字段（可以实现商户自定义参数的追踪）本交易的后台通知
        data.put("reqReserved", tenantId);
        String certPath = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PATH);
        String certPwd = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PWD);
        Map<String, String> submitFromData = SDKUtil.signData(data, certPath, certPwd,
                YlPayConfigManager.DEFAULT_CHARSET);
        // 建立请求
        String requestAppUrl = ConfigUtil.getProperty(YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.APP_TRANS_URL);
        return WapUtil.submitUrl(submitFromData, requestAppUrl, YlPayConfigManager.DEFAULT_CHARSET);
    }

    /**
     * 银联退货交易
     * @param tenantId
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/refund")
    public RefundRes refund(@RequestParam(value = "tenantId", required = true) String tenantId, 
            @RequestParam(value = "orderId", required = true) String orderId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("银联退款操作开始,退款订单号： " + orderId);
        TradeRecord refundRecord = this.queryTradeRecord(tenantId, orderId);
        if (refundRecord == null) {
            LOG.error("退款失败，查询退款沉淀信息出错： 租户标识： " + tenantId + "，订单号： " + orderId);
            throw new SystemException("退款失败，查询退款沉淀信息出错!");
        }

        Map<String, String> resmap = this.doRefund(tenantId, refundRecord);
        LOG.info("银联退货交易返回报文：" + resmap);
        if (resmap == null) {
            throw new SystemException("银联退款失败，可能原因：网络异常！");
        }

        if (!SDKUtil.validate(resmap, YlPayConfigManager.DEFAULT_CHARSET)) {
            throw new SystemException("银联退货交易同步返回报文延签失败！");
        }

        RefundRes refundRes = new RefundRes();
        refundRes.setTenantId(tenantId);
        refundRes.setOrderId(orderId);
        refundRes.setOriOrderId(refundRecord.getOriOrderId());
        String refundAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(Math.abs(refundRecord.getPayAmount())));
        refundRes.setRefundAmount(refundAmount);
        int status = PayConstants.Status.REFUND_FAIL;
        String respMsg = resmap.get("respMsg");
        if (PayConstants.ReturnCode.SUCCESS.equals(resmap.get("respCode"))) {
            LOG.info("银联退款申请已成功受理，订单号： " + orderId);
            status = PayConstants.Status.REFUND_ACCEPT;
            refundRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            LOG.error("银联退款申请受理失败，错误码： " + resmap.get("respCode") + "错误详情： " + respMsg);
            refundRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            refundRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            refundRes.setErrMsg(respMsg);
        }
        
        this.modifyTradeState(tenantId, orderId, status);
        return refundRes;
    }

    /**
     * 银联退货交易
     * 
     * @param tenantId
     * @param tradeRecord
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private Map<String, String> doRefund(String tenantId, TradeRecord tradeRecord) {
        String payUrl = AbstractPayConfigManager.getPayUrl();
        LOG.info("支付平台外网地址： " + payUrl);
        // 组装请求报文
        Map<String, String> data = new HashMap<String, String>();
        // 版本号
        data.put("version", "5.0.0");
        // 字符集编码 默认"UTF-8"
        data.put("encoding", "UTF-8");
        // 签名方法 01 RSA
        data.put("signMethod", "01");
        // 交易类型
        data.put("txnType", "04");
        // 交易子类型
        data.put("txnSubType", "00");
        // 业务类型
        data.put("bizType", "000201");
        // 渠道类型，07-PC，08-手机
        data.put("channelType", "08");
        // 后台通知地址
        data.put("backUrl", payUrl + REFUND_NOTIFY_URL);
        // 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
        data.put("accessType", "0");
        // 商户号码，请改成自己的商户号
        data.put("merId", ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.MERID));
        // 原消费的queryId，可以从查询接口或者通知接口中获取
        data.put("origQryId", tradeRecord.getPayOrgSerial());
        // 商户订单号，8-40位数字字母，重新产生，不同于原消费
        data.put("orderId", tradeRecord.getTradeOrderId());
        // 订单发送时间，取系统时间
        data.put("txnTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        // 交易金额，单位分
        long refundLiAmount = Math.abs(tradeRecord.getPayAmount());
        data.put("txnAmt", String.valueOf(AmountUtil.changeLiToFen(refundLiAmount)));
        // 交易币种
        data.put("currencyCode", "156");
        // 请求方保留域，透传字段（可以实现商户自定义参数的追踪）本交易的后台通知
        data.put("reqReserved", tenantId);
        String certPath = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PATH);
        String certPwd = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PWD);
        Map<String, String> submitFromData = SDKUtil.signData(data, certPath, certPwd,
                YlPayConfigManager.DEFAULT_CHARSET);
        // 建立请求
        String backTransUrl = ConfigUtil.getProperty(YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.BACK_TRANS_URL);
        return WapUtil.submitUrl(submitFromData, backTransUrl, YlPayConfigManager.DEFAULT_CHARSET);
    }

    /**
     * 银联退货交易后台通知
     * 
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/refundNotify")
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("银联退货交易后台通知...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            String encoding = request.getParameter(SDKConstants.param_encoding);
            Map<String, String> params = WapUtil.getAllRequestParam(request);
            LOG.info("银联退货交易后台通知参数：" + params);
            if (!SDKUtil.validate(params, encoding)) {
                throw new SystemException("银联退货交易后台通知延签失败！");
            }

            String tradeOrderId = params.get("orderId");
            String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
            String orderId = tradeOrderId;//orderInfoArray[1]; 
            // String tenantId = params.get("reqReserved");
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("银联退货交易后台通知出错，获取订单信息失败： 租户标识： " + tenantId + "，订单号： " + orderId);
                throw new SystemException("银联退货交易后台通知出错，获取订单信息失败!");
            }
            int status = PayConstants.Status.REFUND_FAIL;
            String dealState = PayConstants.ReturnCode.FAILD;
            String refund_trade_no = params.get("queryId");
            if (PayConstants.ReturnCode.SUCCESS.equals(params.get("respCode"))
                    && !StringUtil.isBlank(refund_trade_no)) {
                status = PayConstants.Status.REFUND_FINISH;
                dealState = PayConstants.ReturnCode.SUCCESS;
                this.modifyTradeState(tenantId, tradeRecord.getOrderId(), status, refund_trade_no,
                        null, null, null, null, null, JSON.toJSONString(params), PayOrgCode.YL);

                /* 4.异步通知外部系统退款结果 */
                String notifyUrl = tradeRecord.getNotifyUrl();
                String refundAmount = String.format("%.2f",
                        AmountUtil.changeLiToYuan(Math.abs(tradeRecord.getPayAmount())));
                if (!StringUtil.isBlank(notifyUrl)) {
                    PaymentNotifyUtil.notifyClientRefundDealResult(notifyUrl, tenantId, orderId,
                            tradeRecord.getOriOrderId(), refundAmount, dealState);
                }
            }

            // 返回给银联服务器http 200 状态码
            printWriter.print("ok");
            printWriter.flush();
        } catch (Exception ex) {
            LOG.error("银联退货交易后台通知失败", ex);
        }

    }

    /**
     * 银联代付操作
     * 
     * @param tenantId
     * @param orderId
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @ResponseBody
    @RequestMapping(value = "/withdraw")
    public WithdrawRes withdraw(@RequestParam(value = "tenantId", required = true)
        String tenantId, @RequestParam(value = "orderId", required = true)
        String orderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("银联单笔代付操作开始,订单号： " + orderId);
        TradeRecord takeRecord = this.queryTradeRecord(tenantId, orderId);
        if (takeRecord == null) {
            LOG.error("单笔代付失败，查询提现沉淀信息出错： 租户标识： " + tenantId + "，订单号： " + orderId);
            throw new SystemException("提现失败，查询提现沉淀信息出错!");
        }

        Map<String, String> resmap = this.unionPayDF(tenantId, takeRecord);
        LOG.info("银联单笔代付返回报文：" + resmap);
        if (resmap == null) {
            throw new SystemException("银联单笔代付失败，可能原因：网络异常！");
        }

        if (!SDKUtil.validate(resmap, YlPayConfigManager.DEFAULT_CHARSET)) {
            throw new SystemException("银联单笔代付交易同步返回报文延签失败！");
        }

        WithdrawRes withdrawRes = new WithdrawRes();
        withdrawRes.setTenantId(tenantId);
        withdrawRes.setOrderId(orderId);
        withdrawRes.setPayOrgCode(PayConstants.PayOrgCode.YL);
        String takeAmount = String.format("%.2f",
                AmountUtil.changeLiToYuan(takeRecord.getPayAmount()));
        withdrawRes.setTakeAmount(takeAmount);
        int status = PayConstants.Status.TAKE_FAIL;
        String respMsg = resmap.get("respMsg");
        if (PayConstants.ReturnCode.SUCCESS.equals(resmap.get("respCode"))) {
            LOG.info("银联代付已成功受理，订单号： " + orderId);
            status = PayConstants.Status.TAKE_SUCCESS;
            withdrawRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            LOG.error("银联代付申请受理失败，错误码： " + resmap.get("respCode") + "错误详情： " + respMsg);
            withdrawRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            withdrawRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            withdrawRes.setErrMsg(respMsg);
        }

        this.modifyTradeState(tenantId, orderId, status);
        return withdrawRes;
    }

    /**
     * 银联单笔代付
     * 
     * @param tenantId
     * @param tradeRecord
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private Map<String, String> unionPayDF(String tenantId, TradeRecord takeRecord) {
        String payUrl = AbstractPayConfigManager.getPayUrl();
        LOG.info("支付平台外网地址： " + payUrl);
        // 组装请求报文
        Map<String, String> data = new HashMap<String, String>();
        // 版本号
        data.put("version", "5.0.0");
        // 字符集编码 默认"UTF-8"
        data.put("encoding", "UTF-8");
        // 签名方法 目前只支持01：RSA方式证书加密
        data.put("signMethod", "01");
        // 交易类型 12：代付
        data.put("txnType", "12");
        // 交易子类型，默认填写00
        data.put("txnSubType", "00");
        // 业务类型 000401：代付
        data.put("bizType", "000401");
        // 渠道类型，07-PC，08-手机
        data.put("channelType", "07");
        // 后台通知地址
        data.put("backUrl", payUrl + WITH_DRAW_NOTIFY_URL);

        /*** 商户接入参数 ***/
        // 商户号码，请改成自己的商户号
        data.put("merId", ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.MERID));
        // 接入类型，商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
        data.put("accessType", "0");
        // 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
        data.put("orderId", takeRecord.getTradeOrderId());
        // 订单发送时间，取系统时间，格式为YYYYMMDDhhmmss
        data.put("txnTime", DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS));
        // 账号类型 01：银行卡02：存折03：IC卡帐号类型(卡介质)
        data.put("accType", "01");
        // 收款方账户信息，格式账号^账户名称
        String receiverAccountInfo = takeRecord.getDetailData();
        String[] cardInfoArr = receiverAccountInfo.split("\\^");
        if (cardInfoArr.length != 2) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, "转账收款方信息格式有误，账号^账户名称");
        }

        String acctNo = cardInfoArr[0];
        String acctName = cardInfoArr[1];
        data.put("accNo", acctNo);
        // 代收交易的上送的卡验证要素为：姓名或者证件类型+证件号码
        Map<String, String> customerInfoMap = new HashMap<String, String>();
        // customerInfoMap.put("certifTp", "01"); //证件类型
        // customerInfoMap.put("certifId", "341126197709218366"); //证件号码
        customerInfoMap.put("customerNm", acctName); // 姓名
        String customerInfoStr = SDKUtil.getCustomerInfo(customerInfoMap, acctNo,
                YlPayConfigManager.DEFAULT_CHARSET);
        data.put("customerInfo", customerInfoStr);
        // 交易金额 单位为分，不能带小数点
        data.put("txnAmt", String.valueOf(AmountUtil.changeLiToFen(takeRecord.getPayAmount())));
        data.put("currencyCode", "156"); // 境内商户固定 156 人民币
        data.put("reqReserved", tenantId); // 商户自定义保留域，交易应答时会原样返回

        String certPath = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PATH);
        String certPwd = ConfigUtil.getProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.SDK_SIGNCERT_PWD);
        Map<String, String> submitFromData = SDKUtil.signData(data, certPath, certPwd,
                YlPayConfigManager.DEFAULT_CHARSET);
        // 建立请求
        String backTransUrl = ConfigUtil.getProperty(YlPayConfigManager.PAY_ORG_NAME,
                YlPayConfigManager.BACK_TRANS_URL);
        return WapUtil.submitUrl(submitFromData, backTransUrl, YlPayConfigManager.DEFAULT_CHARSET);
    }

    /**
     * 银联单笔代付后台通知服务
     * 
     * @param request
     * @param response
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/withdrawNotify")
    public void withdrawNotify(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("银联单笔代付交易后台通知...");
        PrintWriter printWriter = null;
        try {
            request.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            printWriter = response.getWriter();
            String encoding = request.getParameter(SDKConstants.param_encoding);
            Map<String, String> params = WapUtil.getAllRequestParam(request);
            LOG.info("银联单笔代付交易后台通知参数：" + params);
            if (!SDKUtil.validate(params, encoding)) {
                throw new SystemException("银联单笔代付交易后台通知延签失败！");
            }

            String tradeOrderId = params.get("orderId");
            String tenantId = ConfigFromFileUtil.getProperty("TENANT_ID");//orderInfoArray[0]; 
            String orderId = tradeOrderId;//orderInfoArray[1]; 
            // String tenantId = params.get("reqReserved");
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
            if (tradeRecord == null) {
                LOG.error("银联单笔代付交易后台通知出错，获取订单信息失败： 租户标识： " + tenantId + "，订单号： " + orderId);
                throw new SystemException("银联单笔代付交易后台通知出错，获取订单信息失败!");
            }
            int status = PayConstants.Status.TAKE_FAIL;
            String dealState = PayConstants.ReturnCode.FAILD;
            String out_trade_no = params.get("queryId");
            if (PayConstants.ReturnCode.SUCCESS.equals(params.get("respCode"))
                    && !StringUtil.isBlank(out_trade_no)) {
                status = PayConstants.Status.TAKE_SUCCESS;
                dealState = PayConstants.ReturnCode.SUCCESS;
                this.modifyTradeState(tenantId, tradeRecord.getOrderId(), status, out_trade_no,
                        null, null, null, null, null, JSON.toJSONString(params), PayOrgCode.YL);

                /* 4.异步通知外部系统退款结果 */
                String notifyUrl = tradeRecord.getNotifyUrl();
                String takeAmount = String.format("%.2f",
                        AmountUtil.changeLiToYuan(tradeRecord.getPayAmount()));
                if (!StringUtil.isBlank(notifyUrl)) {
                    PaymentNotifyUtil.notifyClientWithDrawDealResult(notifyUrl, tenantId, orderId,
                            out_trade_no, takeAmount, dealState, PayConstants.PayOrgCode.YL);
                }
            }

            // 返回给银联服务器http 200 状态码
            printWriter.print("ok");
            printWriter.flush();
        } catch (Exception ex) {
            LOG.error("银联单笔代付交易后台通知失败", ex);
        }

    }

}
