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
import com.ai.runner.center.pay.api.paycenter.param.TradeModifyReq;
import com.ai.runner.center.pay.api.paycenter.param.TradeReq;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.model.BatchWithdrawReqParam;
import com.ai.runner.center.pay.web.business.payment.model.WithdrawReqParam;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.alibaba.fastjson.JSON;

/**
 * 支付平台提现功能统一入口控制类
 *
 * Date: 2015年12月11日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/pay")
public class WithdrawController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(WithdrawController.class);
    
    /**
     * 单笔提现
     * @param request
     * @param response
     * @param withdrawReqParam
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public void withdraw(HttpServletRequest request, HttpServletResponse response,
            WithdrawReqParam withdrawReqParam) throws Exception {
        LOG.info("接收到单笔提现请求详细报文： " + JSON.toJSONString(withdrawReqParam));
        /* 1.参数验证 */
        this.validateWithdrawReq(withdrawReqParam);
        String tenantId = withdrawReqParam.getTenantId();
        String partnerId = this.getPartnerId(tenantId);
        if (StringUtil.isBlank(partnerId)) {
            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
        }

        String payOrgId = withdrawReqParam.getPayOrgCode();
        String requestSource = withdrawReqParam.getRequestSource();
        /* 2.沉淀提现记录流水 */
        this.createWithDrawInfo(tenantId, payOrgId, withdrawReqParam.getOrderId(),
                withdrawReqParam.getWithDrawFee(), withdrawReqParam.getReceiverAccountInfo(),
                requestSource, withdrawReqParam.getNotifyUrl(), partnerId);

        /* 3.发起提现操作 */
        this.doWithDraw(request, response, payOrgId, requestSource);
    }
    
    /**
     * 批量付款（带界面）
     * @param request
     * @param response
     * @param withdrawReqParam
     * @throws Exception
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @RequestMapping(value = "/batchWithdrawPage", method = RequestMethod.POST)
    public void batchWithdrawPage(HttpServletRequest request, HttpServletResponse response,
            BatchWithdrawReqParam withdrawReqParam) throws Exception {
        LOG.info("接收到提现请求： 租户标识： " + withdrawReqParam.getTenantId() + "，付款详细数据： "
                + withdrawReqParam.getDetailData());
        LOG.info("接收到提现请求详细报文： " + JSON.toJSONString(withdrawReqParam));

        /* 1.参数验证 */
        this.validateBatchWithdrawReq(withdrawReqParam);
        String tenantId = withdrawReqParam.getTenantId();
        String partnerId = this.getPartnerId(tenantId);
        if (StringUtil.isBlank(partnerId)) {
            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
        }

        String payOrgId = withdrawReqParam.getPayOrgCode();
        String requestSource = withdrawReqParam.getRequestSource();
        /* 2.沉淀提现记录流水 */
        this.createWithDrawInfo(tenantId, payOrgId, withdrawReqParam.getBatchNo(),
                withdrawReqParam.getBatchFee(), withdrawReqParam.getDetailData(), requestSource,
                withdrawReqParam.getNotifyUrl(), partnerId);

        /* 3.发起提现操作 */
        this.doBatchTrans(request, response, payOrgId, requestSource);
    }
    
    private void doWithDraw(HttpServletRequest request, HttpServletResponse response,
            String payOrgId, String requestSource) throws ServletException, IOException {
        String withDrawAction = AbstractPayConfigManager.getWithDrawAction(payOrgId, requestSource);
        LOG.info("对应的提现action: " + withDrawAction);
        if (withDrawAction == null) {
            LOG.error("提现出错，无法发起提现操作");
            throw new BusinessException(ExceptCodeConstants.WRONG_ACTION, "提现出错，无法发起提现操作！");
        }

        request.getRequestDispatcher(withDrawAction).forward(request, response);
    }
    
    private void doBatchTrans(HttpServletRequest request, HttpServletResponse response,
            String payOrgId, String requestSource) throws ServletException, IOException {
        String batchTransAction = AbstractPayConfigManager.getBatchTransAction(payOrgId, requestSource);
        LOG.info("对应的批量付款action: " + batchTransAction);
        if (batchTransAction == null) {
            LOG.error("批量提现出错，无法发起提现操作");
            throw new BusinessException(ExceptCodeConstants.WRONG_ACTION, "批量提现出错，无法发起提现操作！");
        }

        request.getRequestDispatcher(batchTransAction).forward(request, response);
    }
    
    /**
     * 
     * @param tenantId
     * @param payOrgId
     * @param orderId
     * @param takeAmount
     * @param detailData
     * @param requestSource
     * @param notifyUrl
     * @param partnerId
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void createWithDrawInfo(String tenantId, String payOrgId, String orderId,
            String takeAmount, String detailData, String requestSource, String notifyUrl,
            String partnerId) throws BusinessException {
        TradeRecord tradeRecord = queryTradeRecord(tenantId, orderId);
        // 如果提现记录未创建，则新增提现流水记录
        if (tradeRecord == null) {
            this.createWithDrawRecord(tenantId, payOrgId, orderId, takeAmount, detailData, requestSource, notifyUrl, partnerId);
            LOG.info("成功创建订单[" + orderId + "]提现流水记录！");
            return;
        }
        
        if (PayConstants.PayRequestType.TAKE != tradeRecord.getPayRequestType()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "重复交易");
        }
        
        if (PayConstants.Status.TAKE_SUCCESS == tradeRecord.getStatus()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "该订单[" + orderId + "]已经提现成功，请勿重新操作！");
        }

        // 更新批次号
        TradeModifyReq req = new TradeModifyReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        req.setDetailData(detailData);
        req.setRequestSource(requestSource);
        this.payCenterSV.modifyTradeRecord(req);
        LOG.info("修改租户[" + tenantId + "]下订单号[" + orderId + "]提现流水记录成功");
    }
    
    /**
     * 创建提现流水记录
     * @param tenantId
     * @param payOrgId
     * @param orderId
     * @param takeAmount
     * @param detailData
     * @param requestSource
     * @param notifyUrl
     * @param partnerId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private long createWithDrawRecord(String tenantId, String payOrgId, String orderId,
            String takeAmount, String detailData, String requestSource, String notifyUrl,
            String partnerId) {
        TradeReq req = new TradeReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        String tradeOrderId = this.buildTradeOrderId(partnerId, orderId);
        req.setTradeOrderId(tradeOrderId);
        req.setPayAmount(AmountUtil.changeYuanToLi(Double.parseDouble(takeAmount)));
        req.setDetailData(detailData);
        req.setNotifyUrl(notifyUrl);
        req.setPayOrgId(payOrgId);
        req.setRequestSource(requestSource);
        req.setPayRequestType(PayConstants.PayRequestType.TAKE);
        return payCenterSV.createTradeRecord(req);
    }
    
    /**
     * 校验提现请求合法性
     * @param withdrawReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validateBatchWithdrawReq(BatchWithdrawReqParam withdrawReqParam) throws BusinessException {
        final String errMsg = "提现传入参数有误：";
        String tenantId = withdrawReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }
             
        if (StringUtil.isBlank(withdrawReqParam.getBatchNo())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "批量付款批次号不能为空");
        }
        
        if (StringUtil.isBlank(withdrawReqParam.getBatchNum())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "付款总笔数不能为空");
        }
        
        if (StringUtil.isBlank(withdrawReqParam.getBatchFee())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "付款总金额不能为空");
        }
        
        if(!withdrawReqParam.getBatchFee().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "付款总金额格式有误");
        }        
        
        if(StringUtil.isBlank(withdrawReqParam.getPayOrgCode())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "支付机构编码不能为空");
        }
        
        if (StringUtil.isBlank(withdrawReqParam.getDetailData())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "付款详细数据不能为空");
        }
                
        if (StringUtil.isBlank(withdrawReqParam.getRequestSource())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "终端来源不能为空");
        }
                                
        if(StringUtil.isBlank(withdrawReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        if(!VerifyUtil.verifyBatchWithDrawParam(withdrawReqParam)) {
            LOG.error("延签失败：传入的参数已被篡改！" + withdrawReqParam.getInfoMd5());
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的提现请求参数非法,参数有误或已被篡改！");
        }
    }
    
    /**
     * 校验单笔提现请求参数是否合法
     * @param withdrawReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validateWithdrawReq(WithdrawReqParam withdrawReqParam) throws BusinessException {
        final String errMsg = "提现请求参数有误：";
        String tenantId = withdrawReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }
             
        if (StringUtil.isBlank(withdrawReqParam.getOrderId())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "提现订单号不能为空");
        }
                
        if (StringUtil.isBlank(withdrawReqParam.getWithDrawFee())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "付款金额不能为空");
        }
        
        if(!withdrawReqParam.getWithDrawFee().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "付款金额格式有误");
        }        
        
        if(StringUtil.isBlank(withdrawReqParam.getPayOrgCode())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "支付机构编码不能为空");
        }
        
        if (StringUtil.isBlank(withdrawReqParam.getReceiverAccountInfo())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "收款方账户信息不能为空");
        }
                
        if (StringUtil.isBlank(withdrawReqParam.getRequestSource())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "终端来源不能为空");
        }
                                
        if(StringUtil.isBlank(withdrawReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        if(!VerifyUtil.verifyWithDrawParam(withdrawReqParam)) {
            LOG.error("延签失败：传入的参数已被篡改！" + withdrawReqParam.getInfoMd5());
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的提现请求参数非法,参数有误或已被篡改！");
        }
    }
}
