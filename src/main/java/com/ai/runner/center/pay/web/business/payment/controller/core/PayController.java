package com.ai.runner.center.pay.web.business.payment.controller.core;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ai.opt.sdk.util.CollectionUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.paycenter.param.TradeModifyReq;
import com.ai.runner.center.pay.api.terminalorgrelquery.interfaces.ITerminalOrgRelQuerySV;
import com.ai.runner.center.pay.api.terminalorgrelquery.param.TerminalOrgRelQryParam;
import com.ai.runner.center.pay.api.terminalorgrelquery.param.TerminalOrgRelVo;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.model.PaymentReqParam;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSON;

/**
 * 支付平台支付统一入口控制类
 *
 * Date: 2015年11月6日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/pay")
public class PayController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(PayController.class);
    
    @Autowired
    private ITerminalOrgRelQuerySV terminalOrgRelQuerySV;
    
    @RequestMapping(value = "/gotoPayByOrg", method = RequestMethod.POST)
    public ModelAndView gotoPayNew(HttpServletRequest request, HttpServletResponse response,
            PaymentReqParam paymentReqParam) throws Exception {
        request.setCharacterEncoding("utf-8");
    	/* 参数校验 */
    	this.checkGotoPayByOrgParam(paymentReqParam);
    	/* 2.沉淀支付交易记录   */
        String tenantId = paymentReqParam.getTenantId();
        String orderId = paymentReqParam.getOrderId();
        String orderAmount = paymentReqParam.getOrderAmount();
        String subject = paymentReqParam.getSubject();
        String requestSource = paymentReqParam.getRequestSource();
        String returnUrl = paymentReqParam.getReturnUrl();
        String partnerId = "";
        String serverType =  ConfigUtil.getProperty(PayConstants.SERVER_TYPE);
        LOG.info("接收到业务系统支付请求, orderId = " + orderId + ", subject = " + subject);
        if ("ISTEST".equals(serverType)) {
            orderAmount = "0.01";
        }
        this.createPaymentInfo(tenantId, orderId, orderAmount, subject, requestSource,
                paymentReqParam.getNotifyUrl(), paymentReqParam.getMerchantUrl(), returnUrl,
                partnerId, paymentReqParam.getCurrencyUnit());
    	String payOrgCode = paymentReqParam.getPayOrgCode();
    	TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
        if (tradeRecord == null) {
            LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
            throw new SystemException("发起支付时查询订单信息异常!");
        }
        
        /* 3.校验此订单是否可以支付 */
        this.checkOrderCouldPay(tradeRecord);
        /* 4.跳转到对应的支付方式支付前准备工作 */
        return this.prepareToPay(tenantId, orderId, payOrgCode, tradeRecord.getRequestSource());
    }
    
    private void checkGotoPayByOrgParam(PaymentReqParam paymentReqParam) {
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
        
        if (!paymentReqParam.getOrderAmount().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "订单金额格式有误");
        }
        
        if (StringUtil.isBlank(paymentReqParam.getCurrencyUnit())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "币种不能为空");
        }
        
        if (StringUtil.isBlank(paymentReqParam.getReturnUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "页面跳转同步通知地址不能为空");
        }
        
        if (StringUtil.isBlank(paymentReqParam.getNotifyUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "服务器异步通知页面路径不能为空");
        }
        
        if(StringUtil.isBlank(paymentReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        //orderId;orderAmount;notifyUrl;tenantId
        //(订单号，订单金额，服务后台通知路径，租户ID四个关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = paymentReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + paymentReqParam.getOrderAmount() + VerifyUtil.SEPARATOR
                + paymentReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if (!"0".equals(paymentReqParam.getCheckFlag())) {
            if(!VerifyUtil.checkParam(infoStr, paymentReqParam.getInfoMd5(), key)) {
                LOG.error("验签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
        }
        if (StringUtil.isBlank(paymentReqParam.getPayOrgCode())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "支付机构不能为空");
        }
    }

	@RequestMapping(value = "/choosePlatform", method = RequestMethod.POST)
    public ModelAndView choosePlatform(HttpServletRequest request, HttpServletResponse response,
            PaymentReqParam paymentReqParam) throws Exception {
        LOG.info("跳转到支付平台选择界面，商户订单号： " + paymentReqParam.getOrderId() + " ，租户标识： " + paymentReqParam.getTenantId());
        LOG.info("接收到的业务系统请求参数： " + JSON.toJSONString(paymentReqParam));
        ModelAndView mv = new ModelAndView("payment/choosePlatform");
        String errorMsg = "";
        /* 1.参数验证   */
        this.validatePaymentReq(paymentReqParam);
        /* 2.沉淀支付交易记录   */
        String tenantId = paymentReqParam.getTenantId();
        String orderId = paymentReqParam.getOrderId();
        String orderAmount = paymentReqParam.getOrderAmount();
        String subject = paymentReqParam.getSubject();
        String requestSource = paymentReqParam.getRequestSource();
        String returnUrl = paymentReqParam.getReturnUrl();
        String partnerId = "";//this.getPartnerId(tenantId);
        String serverType =  ConfigUtil.getProperty(PayConstants.SERVER_TYPE);
        if("ISTEST".equals(serverType)){
            orderAmount = "0.01";
        }
        this.createPaymentInfo(tenantId, orderId, orderAmount, subject, requestSource,
                paymentReqParam.getNotifyUrl(), paymentReqParam.getMerchantUrl(), returnUrl,
                partnerId, paymentReqParam.getCurrencyUnit());
        /* 3.根据调用端信息，查询支持哪种支付方式 */
        List<TerminalOrgRelVo> terminalOrgRelList = this.getTerminalOrgRelVos(tenantId, requestSource);
        /**
         * 如果租户终端下对应的支付方式只有一种，则跳过支付平台选择界面，直接跳转到对应的支付方式进行支付操作
         */
        if(!CollectionUtil.isEmpty(terminalOrgRelList) && terminalOrgRelList.size() == 1) {
            String payOrgCode = terminalOrgRelList.get(0).getPayOrgCode();
            return this.prepareToPay(tenantId, orderId, payOrgCode, requestSource);
        }
        
        mv.addObject("tenantId", tenantId);
        mv.addObject("orderId", orderId);
        mv.addObject("orderAmount", orderAmount);
        mv.addObject("subject", subject);
        mv.addObject("returnUrl", returnUrl);
        mv.addObject("terminalOrgRelList", terminalOrgRelList);
        mv.addObject("errorMsg", errorMsg);
        return mv;
    }
    
    @RequestMapping(value = "/gotoPay", method = RequestMethod.POST)
    public ModelAndView gotoPay(@RequestParam(value = "tenantId", required = true) String tenantId, 
                          @RequestParam(value = "orderId", required = true) String orderId,
                          @RequestParam(value = "payOrgCode", required = true) String payOrgCode, 
                          HttpServletRequest request, HttpServletResponse response) throws Exception {
        TradeRecord tradeRecord = this.queryTradeRecord(tenantId, orderId);
        if(tradeRecord == null) {
            LOG.error("发起支付时查询不到此订单支付请求数据： 租户标识： " + tenantId + " ，订单号： " + orderId);
            throw new SystemException("发起支付时查询订单信息异常!");
        }
        
        /* 1.校验此订单是否可以支付 */
        this.checkOrderCouldPay(tradeRecord);
        /* 2.跳转到对应的支付方式支付前准备工作 */
        return this.prepareToPay(tenantId, orderId, payOrgCode, tradeRecord.getRequestSource());
    }
    
    /**
     * 跳转到对应的支付方式支付前准备工作
     * @param tenantId
     * @param orderId
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private ModelAndView prepareToPay(String tenantId, String orderId, String payOrgCode,
            String requestSource) throws BusinessException {
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
        if(payAction == null) {
            LOG.error("支付遇到问题,跳转不到对应的支付网页进行支付操作！");
            throw new BusinessException(ExceptCodeConstants.WRONG_ACTION, "支付遇到问题,跳转不到对应的支付网页进行支付操作！");
        }
        
        String key = AbstractPayConfigManager.getRequestKey();
        String infoStr = orderId + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("forward:" + payAction);
        mv.addObject("tenantId", tenantId);
        mv.addObject("orderId", orderId);
        mv.addObject("infoMd5", infoMd5);
        return mv;
    }
    
    /**
     * 根据调用端信息，查询支持哪种支付方式
     * @param tenantId
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private List<TerminalOrgRelVo> getTerminalOrgRelVos(String tenantId, String requestSource) {
        TerminalOrgRelQryParam qryOrgParam = new TerminalOrgRelQryParam();
        qryOrgParam.setTenantId(tenantId);
        qryOrgParam.setRequestSource(requestSource);
        return this.terminalOrgRelQuerySV.queryTerminalOrgRels(qryOrgParam);
    }
    
    /**
     * 业务系统支付传入参数校验
     * @param paymentReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validatePaymentReq(PaymentReqParam paymentReqParam) throws BusinessException{
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
        
        if(StringUtil.isBlank(paymentReqParam.getOrderAmount())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单金额不能为空");
        }
        
        if (StringUtil.isBlank(paymentReqParam.getCurrencyUnit())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "币种不能为空");
        }
        
        if(!paymentReqParam.getOrderAmount().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "订单金额格式有误");
        }
        
        if(StringUtil.isBlank(paymentReqParam.getReturnUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "页面跳转同步通知地址不能为空");
        }
        
        if(StringUtil.isBlank(paymentReqParam.getNotifyUrl())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "服务器异步通知页面路径不能为空");
        }
        
        if(StringUtil.isBlank(paymentReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        //orderId;orderAmount;notifyUrl;tenantId
        //(订单号，订单金额，服务后台通知路径，租户ID四个关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = paymentReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + paymentReqParam.getOrderAmount() + VerifyUtil.SEPARATOR
                + paymentReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if(!"0".equals(paymentReqParam.getCheckFlag())){
            if(!VerifyUtil.checkParam(infoStr, paymentReqParam.getInfoMd5(), key)) {
                LOG.error("验签失败：传入的参数已被篡改！" + infoStr);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的支付请求参数非法,参数有误或已被篡改！");
            }
        }
    }
    
}
