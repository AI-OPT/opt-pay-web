package com.ai.runner.center.pay.web.business.payment.controller.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ai.runner.base.exception.BusinessException;
import com.ai.runner.center.pay.api.paycenter.interfaces.IPayCenterSV;
import com.ai.runner.center.pay.api.paycenter.param.TradeModifyReq;
import com.ai.runner.center.pay.api.paycenter.param.TradeReq;
import com.ai.runner.center.pay.api.tenantinfoquery.interfaces.ITenantInfoQuerySV;
import com.ai.runner.center.pay.api.tenantinfoquery.param.PartnerInfoParam;
import com.ai.runner.center.pay.api.tenantinfoquery.param.TenantInfoParam;
import com.ai.runner.center.pay.api.tradequery.interfaces.ITradeQuerySV;
import com.ai.runner.center.pay.api.tradequery.param.BatchNoParam;
import com.ai.runner.center.pay.api.tradequery.param.MerchantOrderIdParam;
import com.ai.runner.center.pay.api.tradequery.param.TradeRecord;
import com.ai.runner.center.pay.web.system.base.BaseController;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.AmountUtil;

/**
 * 支付交易基础controller
 */
public class TradeBaseController extends BaseController {
    
    private static final Logger LOG = Logger.getLogger(TradeBaseController.class);
    
    @Autowired
    protected IPayCenterSV payCenterSV;
    
    @Autowired
    protected ITradeQuerySV tradeQuerySV;
    
    @Autowired
    protected ITenantInfoQuerySV tenantInfoQuerySV;
    
    /**
     * 查询该笔订单信息
     * @param tenantId
     * @param merchantOrderId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected TradeRecord queryTradeRecord(String tenantId, String merchantOrderId) {
        MerchantOrderIdParam param = new MerchantOrderIdParam();
        param.setTenantId(tenantId);
        param.setMerchantOrderId(merchantOrderId);
        return tradeQuerySV.querySingleTradeRecordByMerchantOrderId(param);
    }
    
    
    /**
     * 沉淀初始支付信息
     * @param tenantId
     * @param orderId
     * @param orderAmount
     * @param subject
     * @param requestSource
     * @param notifyUrl
     * @param merchantUrl
     * @param returnUrl
     * @return
     * @throws BusinessException
     * @author fanpw
     * @param currencyUnit 
     * @param payOrgId 
     * @ApiDocMethod
     * @ApiCode
     */
    protected void createPaymentInfo(String tenantId, String orderId, String orderAmount,
            String subject, String requestSource, String notifyUrl,
            String merchantUrl, String returnUrl, String partnerId, String currencyUnit, String payOrgId) throws BusinessException {
        TradeRecord tradeRecord = queryTradeRecord(tenantId, orderId);
        // 如果支付记录未创建，则新增支付流水记录
        if(tradeRecord == null) {
            this.createPaymentRecord(tenantId, " ", orderId, orderAmount, subject, requestSource,
                    notifyUrl, merchantUrl, returnUrl, partnerId, currencyUnit); 
            LOG.info("成功创建订单[" + orderId + "]支付流水记录！");
            return;
        }
        //校验此订单是否可以支付
        this.checkOrderCouldPay(tradeRecord);
        //更新此笔订单交易记录
        this.modifyTrade(tenantId, orderId, orderAmount, subject, requestSource, notifyUrl, merchantUrl, returnUrl, partnerId, currencyUnit, payOrgId);
    }
        
    private void modifyTrade(String tenantId, String orderId, String orderAmount, String subject,
            String requestSource, String notifyUrl, String merchantUrl, String returnUrl,
            String partnerId, String currencyUnit, String payOrgId) {
        TradeModifyReq req = new TradeModifyReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        String tradeOrderId = this.buildTradeOrderId(partnerId, orderId);
        req.setTradeOrderId(tradeOrderId);
        req.setSubject(subject);
        req.setPayAmount(AmountUtil.changeYuanToLi(Double.parseDouble(orderAmount)));
        req.setNotifyUrl(notifyUrl);
        req.setMerchantUrl(merchantUrl);
        req.setReturnUrl(returnUrl);
        req.setPayOrgId(payOrgId);
        req.setRequestSource(requestSource);
        req.setCurrencyUnit(currencyUnit);
        this.payCenterSV.modifyTradeRecord(req);
        LOG.info("成功修改该订单[" + orderId + "]交易状态");
    }


    /**
     * 记录支付日志
     * @param tenantId
     * @param payOrgId
     * @param orderId
     * @param orderAmount
     * @param subject
     * @param requestSource
     * @param notifyUrl
     * @param merchantUrl
     * @param returnUrl
     * @return
     * @author fanpw
     * @param currencyUnit 
     * @ApiDocMethod
     * @ApiCode
     */
    protected long createPaymentRecord(String tenantId, String payOrgId, String orderId,
            String orderAmount, String subject, String requestSource, String notifyUrl,
            String merchantUrl, String returnUrl, String partnerId, String currencyUnit) {
        TradeReq req = new TradeReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        String tradeOrderId = this.buildTradeOrderId(partnerId, orderId);
        req.setTradeOrderId(tradeOrderId);
        req.setSubject(subject);
        req.setPayAmount(AmountUtil.changeYuanToLi(Double.parseDouble(orderAmount)));
        req.setNotifyUrl(notifyUrl);
        req.setMerchantUrl(merchantUrl);
        req.setReturnUrl(returnUrl);
        req.setPayOrgId(payOrgId);
        req.setRequestSource(requestSource);
        req.setPayRequestType(PayConstants.PayRequestType.PAY);
        req.setCurrencyUnit(currencyUnit);
        return payCenterSV.createTradeRecord(req);
    }
    
    /**
     * 查询同一批次的交易记录
     * @param tenantId
     * @param batchNo
     * @param payOrgSerial
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected List<TradeRecord> queryTradeRecords(String tenantId, String batchNo,
            String payOrgSerial) {
        BatchNoParam qryParam = new BatchNoParam();
        qryParam.setTenantId(tenantId);
        qryParam.setBatchNo(batchNo);
        qryParam.setPayOrgSerial(payOrgSerial);
        return tradeQuerySV.queryTradeRecordsByBatchNo(qryParam);
    }
    
    /**
     * 更新交易终端来源
     * 
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected void modifyTradeRequestSource(String tenantId, String orderId, String requestSource) {
        TradeModifyReq req = new TradeModifyReq();
        req.setTenantId(tenantId);
        req.setOrderId(orderId);
        req.setRequestSource(requestSource);
        this.payCenterSV.modifyTradeRecord(req);
        LOG.info("成功修改租户[" + tenantId + "]下订单号[" + orderId + "]支付流水记录的终端来源");
    }
    
    /**
     * 校验此订单是否可以支付
     * @throws BusinessException
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected void checkOrderCouldPay(TradeRecord tradeRecord) throws BusinessException {
        // 防止重复支付
        if (PayConstants.Status.PAYED_SUCCESS == tradeRecord.getStatus()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "该订单已经支付成功，请勿重新支付！！！");
        }
        
        if (PayConstants.PayRequestType.PAY != tradeRecord.getPayRequestType()) {
            throw new BusinessException(ExceptCodeConstants.REPEATABLE_TRADE, "重复交易");
        }
    }
    
    /**
     * 更新交易状态
     * @param tenantId
     * @param merchantOrderId
     * @param status
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected void modifyTradeState(String tenantId, String merchantOrderId, int status) {
        this.modifyTradeState(tenantId, merchantOrderId, status, null, null, null, null, null, null);
    }
    
    /**
     * 后台通知修改订单交易状态
     * @param tenantId
     * @param merchantOrderId
     * @param status
     * @param payOrgSerial
     * @param notifyId
     * @param buyerEmail
     * @param returnEmail
     * @param drawEmail
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected void modifyTradeState(String tenantId, String merchantOrderId, int status,
            String payOrgSerial, String notifyId, String buyerEmail, String returnEmail,
            String drawEmail, String payOrgId) {
        this.modifyTradeState(tenantId, merchantOrderId, status, payOrgSerial, notifyId,
                buyerEmail, returnEmail, drawEmail, null, null, payOrgId);
    }
    
    /**
     * 修改交易记录
     * @param tenantId
     * @param merchantOrderId
     * @param status
     * @param payOrgSerial
     * @param notifyId
     * @param buyerEmail
     * @param returnEmail
     * @param drawEmail
     * @param sendDetailData
     * @param receiveDetailData
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected void modifyTradeState(String tenantId, String merchantOrderId, int status,
            String payOrgSerial, String notifyId, String buyerEmail, String returnEmail,
            String drawEmail, String sendDetailData, String receiveDetailData, String payOrgId) {
        TradeModifyReq req = new TradeModifyReq();
        req.setTenantId(tenantId);
        req.setOrderId(merchantOrderId);
        req.setStatus(status);
        req.setPayOrgSerial(payOrgSerial);
        req.setNotifyId(notifyId);
        req.setBuyerEmail(buyerEmail);
        req.setReturnEmail(returnEmail);
        req.setDrawEmail(drawEmail);
        req.setSendDetailData(sendDetailData);
        req.setReceiveDetailData(receiveDetailData);
        req.setPayOrgId(payOrgId);
        this.payCenterSV.modifyTradeRecord(req);
        LOG.info("成功修改该订单[" + merchantOrderId + "]交易状态");
    }
    
    /**
     * 获取合作方信息
     * @param tenantId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected String getPartnerId(String tenantId) {
        TenantInfoParam param = new TenantInfoParam();
        param.setTenantId(tenantId);
        String partnerId = this.tenantInfoQuerySV.getPartnerId(param);
        LOG.info("返回的合作方编码： " + partnerId);
        return partnerId;
    }
    
    /**
     * 构造内部交易订单号
     * @param partnerId
     * @param merchantOrderId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    protected String buildTradeOrderId(String partnerId, String merchantOrderId) {
        return partnerId + merchantOrderId;
    }
    
    /**
     * 拆分内部交易订单号获取租户ID和商户订单号
     * @param tradeOrderId
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
//    protected String[] splitTradeOrderId(String tradeOrderId) {
//        String partnerId = tradeOrderId.substring(0, 5);
//        String merchantOrderId = tradeOrderId.substring(5);
//        String tenantId = getTenantId(partnerId);
//        String[] result = new String[2];
//        result[0] = tenantId;
//        result[1] = merchantOrderId;
//        return result;
//    }
    
    protected String getTenantId(String partnerId) {
        PartnerInfoParam param = new PartnerInfoParam();
        param.setPartnerId(partnerId);
        String tenantId = this.tenantInfoQuerySV.getTenantId(param);
        LOG.info("返回的租户ID： " + tenantId);
        return tenantId;
    }
}
