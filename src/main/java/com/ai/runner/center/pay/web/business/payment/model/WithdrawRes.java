package com.ai.runner.center.pay.web.business.payment.model;

/**
 * 支付平台提现接口同步返回报文
 *
 * Date: 2016年1月5日 <br>
 * Copyright (c) 2016 asiainfo.com <br>
 * @author fanpw
 */
public class WithdrawRes extends BaseRes {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 提现订单号
     */
    private String orderId;
    
    /**
     * 提现金额
     */
    private String takeAmount;

    /**
     * 提现请求支付机构编码
     */
    private String payOrgCode;

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

    public String getTakeAmount() {
        return takeAmount;
    }

    public void setTakeAmount(String takeAmount) {
        this.takeAmount = takeAmount;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }

}
