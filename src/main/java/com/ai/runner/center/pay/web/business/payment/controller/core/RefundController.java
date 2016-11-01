package com.ai.runner.center.pay.web.business.payment.controller.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.opt.sdk.util.CollectionUtil;
import com.ai.opt.sdk.util.DateUtil;
import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.api.paycenter.param.TradeModifyReq;
import com.ai.runner.center.pay.api.paycenter.param.TradeReq;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.business.payment.annotation.BackTransService;
import com.ai.runner.center.pay.web.business.payment.model.BatchRefundReqParam;
import com.ai.runner.center.pay.web.business.payment.model.RefundReqParam;
import com.ai.runner.center.pay.web.business.payment.util.core.RefundParser;
import com.ai.runner.center.pay.web.business.payment.util.core.RefundParser.RefundData;
import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.exception.ParseDataException;
import com.ai.runner.center.pay.web.system.util.AmountUtil;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.PaymentSeqUtil;
import com.alibaba.fastjson.JSON;

/**
 * 支付平台退款统一入口控制类
 *
 * Date: 2015年11月6日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
@Controller
@RequestMapping(value = "/refund")
public class RefundController extends TradeBaseController {

    private static final Logger LOG = Logger.getLogger(RefundController.class);
    
    /**
     * 单笔退款功能实现
     * @param request
     * @param refundReqParam
     * @return
     * @author fanpw
     * @throws Exception 
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @RequestMapping(value = "/noPwdRefund", method = RequestMethod.POST)
    public void noPwdRefund(HttpServletRequest request, HttpServletResponse response,
            RefundReqParam refundReqParam) throws Exception {
        LOG.info("接收到无密退款请求： 商户订单号： " + refundReqParam.getOrderId() + "，原订单号： "
                + refundReqParam.getOriOrderId() + ",租户标识：" + refundReqParam.getTenantId());
        LOG.info("接收到无密退款请求详细报文： " + JSON.toJSONString(refundReqParam));
        /* 1.参数验证   */
        this.validateRefundReq(refundReqParam);
        String tenantId = refundReqParam.getTenantId();
        String orderId = refundReqParam.getOrderId();
        String oriOrderId = refundReqParam.getOriOrderId();
        String refundAmount = refundReqParam.getRefundAmount();
        String partnerId = this.getPartnerId(tenantId);
        if(StringUtil.isBlank(partnerId)) {
            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
        }   
        TradeRecord tradeRecord = this.queryTradeRecord(tenantId, oriOrderId);
        if(tradeRecord == null) {
            LOG.error("退款失败，传入的原始订单号有误： 租户标识： " + tenantId + "，订单号： " + oriOrderId);
            throw new BusinessException(ExceptCodeConstants.TRADE_NOT_EXIST, "退款失败，交易不存在！");
        }
        
        double payAmount = AmountUtil.changeLiToYuan(tradeRecord.getPayAmount());
        if(Double.parseDouble(refundReqParam.getRefundAmount()) > payAmount) {
            LOG.error("退款失败：退款金额不能大于原始订单金额！");
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "退款失败：退款金额不能大于原始订单金额！");
        }
        
        if (PayConstants.Status.PAYED_SUCCESS != tradeRecord.getStatus()
                || StringUtil.isBlank(tradeRecord.getPayOrgSerial())) {
            LOG.error("退款失败，传入的原始订单号无效： 租户标识： " + tenantId + "，原始订单号： " + oriOrderId);
            throw new SystemException("退款失败，传入的原始订单号无效!");
        }
        
        /* 2.沉淀退款记录流水   */
        String payOrgId = tradeRecord.getPayOrgId();
        String payOrgSerial = tradeRecord.getPayOrgSerial();
        String requestSource = refundReqParam.getRequestSource();
        String batchNo = this.getRefundBatchNo(partnerId);
        this.createRefundInfo(tenantId, batchNo, orderId, oriOrderId, refundAmount, payOrgId,
                payOrgSerial, requestSource, refundReqParam.getNotifyUrl(), partnerId);
        /* 3.向对应的第三方支付平台发起退款请求 */
        this.doRefund(request, response, tenantId, batchNo, payOrgId, requestSource); 
    }
    
    /**
     * 沉淀退款记录流水
     * @param tenantId
     * @param orderId
     * @param oriOrderId
     * @param refundAmount
     * @param payOrgId
     * @param requestSource
     * @param notifyUrl
     * @param partnerId
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void createRefundInfo(String tenantId, String batchNo, String orderId,
            String oriOrderId, String refundAmount, String payOrgId, String payOrgSerial,
            String requestSource, String notifyUrl, String partnerId) throws BusinessException {
        TradeRecord tradeRecord = queryTradeRecord(tenantId, orderId);
        // 如果退款记录未创建，则新增退款流水记录
        if (tradeRecord == null) {
            this.createRefundRecord(tenantId, batchNo, orderId, oriOrderId, refundAmount, payOrgId,
                    payOrgSerial, requestSource, notifyUrl, partnerId);
            LOG.info("成功创建订单[" + orderId + "]退款流水记录！");
            return;
        }
        
        if (PayConstants.PayRequestType.REFUND != tradeRecord.getPayRequestType()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "重复交易");
        }
        
        if (PayConstants.Status.REFUND_ACCEPT == tradeRecord.getStatus()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "已经接受此订单[" + orderId + "]退款申请，请勿重新退款！");
        }

        if (PayConstants.Status.REFUND_SUCCESS == tradeRecord.getStatus()
                || PayConstants.Status.REFUND_FINISH == tradeRecord.getStatus()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "该订单[" + orderId + "]已经退款成功，请勿重新退款！");
        }

        // 更新批次号
        TradeModifyReq req = new TradeModifyReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        req.setBatchNo(batchNo);
        req.setRequestSource(requestSource);
        this.payCenterSV.modifyTradeRecord(req);
        LOG.info("修改租户[" + tenantId + "]下订单号[" + orderId + "]退款流水记录成功");
    }
    
    /**
     * 记录退款日志
     * @param tenantId
     * @param orderId
     * @param oriOrderId
     * @param refundAmount
     * @param payOrgId
     * @param requestSource
     * @param notifyUrl
     * @param partnerId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private long createRefundRecord(String tenantId, String batchNo, String orderId,
            String oriOrderId, String refundAmount, String payOrgId, String payOrgSerial,
            String requestSource, String notifyUrl, String partnerId) {
        TradeReq req = new TradeReq();
        req.setTenantId(tenantId);
        req.setBatchNo(batchNo);
        req.setOrderId(orderId);
        req.setOriOrderId(oriOrderId);
        String tradeOrderId = this.buildTradeOrderId(partnerId, orderId);
        req.setTradeOrderId(tradeOrderId);
        req.setPayAmount(-AmountUtil.changeYuanToLi(Double.parseDouble(refundAmount)));
        req.setNotifyUrl(notifyUrl);
        req.setPayOrgId(payOrgId);
        req.setPayOrgSerial(payOrgSerial);
        req.setRequestSource(requestSource);
        req.setPayRequestType(PayConstants.PayRequestType.REFUND);
        return payCenterSV.createTradeRecord(req);
    }
    
    /**
     * 获取退款批次号
     * @param orderId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private String getRefundBatchNo(String partnerId){
        StringBuilder sb = new StringBuilder();
        sb.append(DateUtil.getDateString(DateUtil.YYYYMMDD));
        sb.append(partnerId);
        sb.append(PaymentSeqUtil.getSixRandom());
        return sb.toString();
    }
           
    /**
     * 校验无密退款请求合法性
     * @param refundReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validateRefundReq(RefundReqParam refundReqParam) throws BusinessException {
        final String errMsg = "单笔退款传入参数有误：";
        String tenantId = refundReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }
        
        if (StringUtil.isBlank(refundReqParam.getOrderId())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "订单号不能为空");
        }
        
        if (StringUtil.isBlank(refundReqParam.getOriOrderId())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "原订单号不能为空");
        }
        
        if (StringUtil.isBlank(refundReqParam.getRequestSource())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "终端来源不能为空");
        }
                
        if(StringUtil.isBlank(refundReqParam.getRefundAmount())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "退款金额不能为空");
        }
        
        if(!refundReqParam.getRefundAmount().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "退款金额格式有误");
        }
                
        if(StringUtil.isBlank(refundReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        //orderId;oriOrderId;refundAmount;notifyUrl;tenantId
        //(订单号，原始付款订单号，退款金额，服务后台通知路径，租户ID四个关键字段，以英文输入分号分隔;注意最后没有分号)
        String infoStr = refundReqParam.getOrderId() + VerifyUtil.SEPARATOR
                + refundReqParam.getOriOrderId() + VerifyUtil.SEPARATOR
                + refundReqParam.getRefundAmount() + VerifyUtil.SEPARATOR
                + refundReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if(!VerifyUtil.checkParam(infoStr, refundReqParam.getInfoMd5(), key)) {
            LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的无密退款请求参数非法,参数有误或已被篡改！");
        }
    }
    
    /**
     * 批量退款功能实现
     * @param request
     * @param batchRefundReqParam
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    @BackTransService
    @RequestMapping(value = "/batchNoPwdRefund", method = RequestMethod.POST)
    public void batchNoPwdRefund(HttpServletRequest request, HttpServletResponse response,
            BatchRefundReqParam batchRefundReqParam) throws Exception {
        LOG.info("接收到批量无密退款请求： 租户标识： " + batchRefundReqParam.getTenantId() + "，退款数据集： "
                + batchRefundReqParam.getDetailData());
        LOG.info("接收到批量退款请求详细报文： " + JSON.toJSONString(batchRefundReqParam));
        /* 1.参数验证 */
        this.validateBatchRefundReq(batchRefundReqParam);
        String tenantId = batchRefundReqParam.getTenantId();
        String partnerId = this.getPartnerId(tenantId);
        if (StringUtil.isBlank(partnerId)) {
            LOG.error("未识别的合作方身份！租户ID： " + tenantId);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARTNER, "未识别的合作方身份！");
        }

        /* 2.解析退款数据集 */
        List<RefundData> refundDataList = this.parseRefundDetailData(batchRefundReqParam);
        if (CollectionUtil.isEmpty(refundDataList)) {
            throw new SystemException("传入的退款数据集有误");
        }
        /* 3.沉淀退款记录流水 */
        // 生成批次号
        String batchNo = this.getRefundBatchNo(partnerId);
        String payOrgId = batchRefundReqParam.getPayOrgCode();
        String requestSource = batchRefundReqParam.getRequestSource();
        String notifyUrl = batchRefundReqParam.getNotifyUrl();
        for (RefundData refundData : refundDataList) {
            this.createRefundInfo(tenantId, batchNo, refundData.getOrderId(),
                    refundData.getOriOrderId(), refundData.getRefundAmount(), payOrgId,
                    refundData.getPayOrgSerial(), requestSource, notifyUrl, partnerId);
        }

        /* 3.向对应的第三方支付平台发起退款请求 */
        this.doBatchRefund(request, response, tenantId, batchNo, payOrgId, requestSource);

    }
    
    /**
     * 发起单笔退款
     * @param request
     * @param response
     * @param tenantId
     * @param batchNo
     * @param payOrgId
     * @param requestSource
     * @throws ServletException
     * @throws IOException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void doRefund(HttpServletRequest request, HttpServletResponse response,
            String tenantId, String batchNo, String payOrgId, String requestSource)
            throws ServletException, IOException {
        String refundAction = AbstractPayConfigManager.getRefundAction(payOrgId, requestSource);
        LOG.info("对应的退款action: " + refundAction);
        if (refundAction == null) {
            LOG.error("退款出错，无法发起退款操作");
            throw new BusinessException(ExceptCodeConstants.WRONG_ACTION, "退款出错，无法发起退款操作！");
        }

        request.getRequestDispatcher(refundAction).forward(request, response);
    }
    
    /**
     * 发起批量退款
     * @param tenantId
     * @param batchNo
     * @param payOrgId
     * @param requestSource
     * @return
     * @author fanpw
     * @throws IOException 
     * @throws ServletException 
     * @ApiDocMethod
     * @ApiCode
     */
    private void doBatchRefund(HttpServletRequest request, HttpServletResponse response,
            String tenantId, String batchNo, String payOrgId, String requestSource)
            throws ServletException, IOException {
        String batchRefundAction = AbstractPayConfigManager.getBatchRefundAction(payOrgId, requestSource);
        LOG.info("对应的退款action: " + batchRefundAction);
        if (batchRefundAction == null) {
            LOG.error("退款出错，无法发起退款操作");
            throw new BusinessException(ExceptCodeConstants.WRONG_ACTION, "退款出错，无法发起退款操作！");
        }

        String key = AbstractPayConfigManager.getRequestKey();
        String infoStr = batchNo + VerifyUtil.SEPARATOR + tenantId;
        String infoMd5 = VerifyUtil.encodeParam(infoStr, key);
        request.setAttribute("tenantId", tenantId);
        request.setAttribute("batchNo", batchNo);
        request.setAttribute("infoMd5", infoMd5);
        request.getRequestDispatcher(batchRefundAction).forward(request, response);
    }
    
    /**
     * 解析批量退款数据集并验证
     * @param batchRefundReqParam
     * @return
     * @throws ParseDataException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private List<RefundData> parseRefundDetailData(BatchRefundReqParam batchRefundReqParam)
            throws ParseDataException {
        List<RefundData> refundDataList = RefundParser.parseRefundDetails(batchRefundReqParam
                .getDetailData());
        if (CollectionUtil.isEmpty(refundDataList)) {
            throw new SystemException("传入的退款数据集有误");
        }

        if (Integer.parseInt(batchRefundReqParam.getBatchNum()) != refundDataList.size()) {
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                    "传入的批量退款请求参数有误:退款总笔数与退款数据集不匹配！");
        }

        String tenantId = batchRefundReqParam.getTenantId();
        String payOrgId = batchRefundReqParam.getPayOrgCode();
        double batchFee = 0l;
        List<String> orderIdList = new ArrayList<String>();
        for (RefundData refundData : refundDataList) {
            String orderId = refundData.getOrderId();
            if (orderIdList.contains(orderId)) {
                LOG.error("退款失败：退款数据集包含重复的退款订单号！");
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                        "退款失败：退款数据集包含重复的退款订单号！");
            }
            orderIdList.add(orderId);
            
            if(!refundData.getRefundAmount().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
                throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, "退款数据集包含金额格式有误");
            }
            
            String oriOrderId = refundData.getOriOrderId();
            double refundAmount = Double.parseDouble(refundData.getRefundAmount());
            batchFee += refundAmount;
            TradeRecord tradeRecord = this.queryTradeRecord(tenantId, oriOrderId);
            if (tradeRecord == null) {
                LOG.error("退款失败，传入的原始订单号有误： 租户标识： " + tenantId + "，订单号： " + oriOrderId);
                throw new BusinessException(ExceptCodeConstants.TRADE_NOT_EXIST,
                        "退款失败，传入的退款数据集存在无效交易！");
            }

            double payAmount = AmountUtil.changeLiToYuan(tradeRecord.getPayAmount());
            if (refundAmount > payAmount) {
                LOG.error("退款失败：退款金额不能大于原始订单金额！ 租户标识：" + tenantId + "，订单号： " + oriOrderId);
                throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                        "退款失败：退款金额不能大于原始订单金额！");
            }

            if (PayConstants.Status.PAYED_SUCCESS != tradeRecord.getStatus()
                    || StringUtil.isBlank(tradeRecord.getPayOrgSerial())) {
                LOG.error("退款失败，传入的原始订单号无效： 租户标识： " + tenantId + "，原始订单号： " + oriOrderId);
                throw new SystemException("退款失败，传入的退款数据集存在无效原始订单号!");
            }

            if (!payOrgId.equals(tradeRecord.getPayOrgId())) {
                LOG.error("批量退款失败，传入的参数有误： 租户标识： " + tenantId + "，原始订单号： " + oriOrderId);
                throw new SystemException("批量退款失败，传入的参数有误!");
            }
            refundData.setPayOrgSerial(tradeRecord.getPayOrgSerial());
        }

        if (Double.parseDouble(batchRefundReqParam.getBatchFee()) != batchFee) {
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM,
                    "传入的批量退款请求参数有误:退款总金额与退款数据集金额不匹配！");
        }

        return refundDataList;
    }
    
    /**
     * 校验批量无密退款请求合法性
     * @param batchRefundReqParam
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    private void validateBatchRefundReq(BatchRefundReqParam batchRefundReqParam) throws BusinessException {
        final String errMsg = "批量退款传入参数有误：";
        String tenantId = batchRefundReqParam.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "租户ID不能为空");
        }
        
        if (StringUtil.isBlank(batchRefundReqParam.getBatchNo())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "批量退款批次号不能为空");
        }
        
        if (StringUtil.isBlank(batchRefundReqParam.getBatchNum())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "退款总笔数不能为空");
        }
        
        if (StringUtil.isBlank(batchRefundReqParam.getBatchFee())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "退款总金额不能为空");
        }
        
        if(!batchRefundReqParam.getBatchFee().matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$")) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_WRONG, errMsg + "退款金额格式有误");
        }
        
        if(StringUtil.isBlank(batchRefundReqParam.getPayOrgCode())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "支付机构编码不能为空");
        }
        
        if (StringUtil.isBlank(batchRefundReqParam.getDetailData())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "退款数据集不能为空");
        }
                
        if (StringUtil.isBlank(batchRefundReqParam.getRequestSource())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "终端来源不能为空");
        }
                                
        if(StringUtil.isBlank(batchRefundReqParam.getInfoMd5())) {
            throw new BusinessException(ExceptCodeConstants.PARAM_IS_NULL, errMsg + "加密信息不能为空");
        }
        
        //batchNo;batchNum;batchFee;payOrgCode;detailData;notifyUrl;tenantId
        //批量退款批次号，退款总笔数，退款总金额，退款请求支付机构编码，退款数据集，异步通知地址，租户ID字段，以英文输入分号分隔;注意最后没有分号
        String infoStr = batchRefundReqParam.getBatchNo() + VerifyUtil.SEPARATOR
                + batchRefundReqParam.getBatchNum() + VerifyUtil.SEPARATOR
                + batchRefundReqParam.getBatchFee() + VerifyUtil.SEPARATOR
                + batchRefundReqParam.getPayOrgCode() + VerifyUtil.SEPARATOR
                + batchRefundReqParam.getDetailData() + VerifyUtil.SEPARATOR
                + batchRefundReqParam.getNotifyUrl() + VerifyUtil.SEPARATOR
                + tenantId;
        String key = ConfigUtil.getTenantCommonProperty(tenantId, PayConstants.REQUEST_KEY);
        if(!VerifyUtil.checkParam(infoStr, batchRefundReqParam.getInfoMd5(), key)) {
            LOG.error("延签失败：传入的参数已被篡改！" + infoStr);
            throw new BusinessException(ExceptCodeConstants.ILLEGAL_PARAM, "传入的批量退款请求参数非法,参数有误或已被篡改！");
        }
    }

}
