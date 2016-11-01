package com.ai.runner.center.pay.web.business.payment.model;

/**
 * 支付平台退款接口同步返回报文
 *
 * Date: 2016年1月5日 <br>
 * Copyright (c) 2016 asiainfo.com <br>
 * @author fanpw
 */
public class RefundRes extends BaseRes {

    private static final long serialVersionUID = 1L;

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
}
