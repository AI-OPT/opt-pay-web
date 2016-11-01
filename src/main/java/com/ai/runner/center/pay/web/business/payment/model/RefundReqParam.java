package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 外部系统调用支付平台单笔退款功能请求参数
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class RefundReqParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;

    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 订单号
     */
    private String orderId;
    
    /**
     * 原订单号
     */
    private String oriOrderId;
        
    /**
     * 退款金额
     */
    private String refundAmount;
     
    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 终端来源:<br>
     * 1:web<br>
     * 2:wap<br>
     * 3:app<br>
     * 4:微信<br>
     */
    private String requestSource;

    /**
     * 服务器异步通知页面路径
     */
    private String notifyUrl;
    
    /**
     * 加密信息
     */
    private String infoMd5;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOriOrderId() {
        return oriOrderId;
    }

    public void setOriOrderId(String oriOrderId) {
        this.oriOrderId = oriOrderId;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getRequestSource() {
        return requestSource;
    }

    public void setRequestSource(String requestSource) {
        this.requestSource = requestSource;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getInfoMd5() {
        return infoMd5;
    }

    public void setInfoMd5(String infoMd5) {
        this.infoMd5 = infoMd5;
    }
    
}
