package com.ai.runner.center.pay.web.business.payment.controller.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.paycenter.param.TradeModifyReq;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.model.PaymentReqParam;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSON;

/**
 * 支付平台中app支付统一入口控制类
 * 
 * Date: 2015年11月6日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/appPay")
public class AppPayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(AppPayController.class);

    /**
     * APP支付获取预订单
     * 
     * @param request
     * @param response
     * @param paymentReqParam
     * @return
     * @throws PaySDKException
     * @author LiangMeng
     * @ApiDocMethod
     */
    @BackTransService
    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public void pay(HttpServletRequest request, HttpServletResponse response,
            PaymentReqParam paymentReqParam) throws Exception {
        LOG.info("APP端获取预订单，商户订单号： " + paymentReqParam.getOrderId() + " ，租户标识： "
                + paymentReqParam.getTenantId());
        LOG.info("接收到的业务系统请求参数： " + JSON.toJSONString(paymentReqParam));
        /* 1.参数验证 */
        this.validatePaymentReq(paymentReqParam);
        /* 2.沉淀支付交易记录 */
        String tenantId = paymentReqParam.getTenantId();
        String orderId = paymentReqParam.getOrderId();
        String orderAmount = paymentReqParam.getOrderAmount();
        String subject = paymentReqParam.getSubject();
        String requestSource = PayConstants.RequestSource.APP;
        String returnUrl = paymentReqParam.getReturnUrl();
        String payOrgCode = paymentReqParam.getPayOrgCode();
        String partnerId = this.getPartnerId(tenantId);
        if (StringUtil.isBlank(partnerId)) {
            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
        }
        //沉淀支付信息
        this.createPaymentInfo(tenantId, orderId, orderAmount, subject, requestSource,
                paymentReqParam.getNotifyUrl(), paymentReqParam.getMerchantUrl(), returnUrl,
                partnerId, paymentReqParam.getCurrencyUnit(), "");

        this.doAppPay(request, response, tenantId, orderId, payOrgCode, requestSource);
    }

    /**
     * 跳转到具体的APP支付分支
     * 
     * @param tenantId
     * @param orderId
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author LiangMeng
     * @throws IOException
     * @throws ServletException
     * @ApiDocMethod
     * @ApiCode
     */
    private void doAppPay(HttpServletRequest request, HttpServletResponse response,
            String tenantId, String orderId, String payOrgCode, String requestSource)
            throws ServletException, IOException {
        /* 1.修改此订单状态为待支付，并准备向第三方支付发起支付请求 */
        TradeModifyReq tradeModifyReq = new TradeModifyReq();
        tradeModifyReq.setTenantId(tenantId);
        tradeModifyReq.setOrderId(orderId);
        tradeModifyReq.setPayOrgId(payOrgCode);
        tradeModifyReq.setStatus(PayConstants.Status.APPLY);
        this.payCenterSV.modifyTradeRecord(tradeModifyReq);
        LOG.info("成功修改该订单[" + orderId + "]状态为待支付，并准备向第三方支付发起支付请求");
        /* 2.跳转到对应的第三方支付平台发起支付 */
        String payAction = AbstractPayConfigManager.getPayAction(payOrgCode, requestSource);
        LOG.info("跳转支付action: " + payAction);
        if (payAction == null) {
            LOG.error("支付遇到问题,无法获取预订单");
            throw new SystemException("支付遇到问题,无法获取预订单");
        }

        String key = AbstractPayConfigManager.getRequestKey();
        String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        request.setAttribute("infoMd5", infoMd5);
        request.getRequestDispatcher(payAction).forward(request, response);
    }

    /**
     * 业务系统支付传入参数校验
     * 
     * @param paymentReqParam
     * @throws BusinessException
     * @author fanpw
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

        if (StringUtil.isBlank(paymentReqParam.getOrderAmount())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单金额不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getCurrencyUnit())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "币种不能为空");
        }
        
        if (StringUtil.isBlank(paymentReqParam.getPayOrgCode())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "支付机构编码不能为空");
        }
        if (StringUtil.isBlank(paymentReqParam.getNotifyUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg
                    + "服务器异步通知页面路径不能为空");
        }

        if (StringUtil.isBlank(paymentReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }

        //orderId;orderAmount;notifyUrl;tenantId
        //(订单号，订单金额，服务后台通知路径，租户ID四个关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = paymentReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + paymentReqParam.getOrderAmount() + VerifyUtil.SEPARATOR
                + paymentReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if(!VerifyUtil.checkParam(infoStr, paymentReqParam.getInfoMd5(), key)) {
            LOG.error("验签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
        }
    }

}
