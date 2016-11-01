package com.ai.runner.center.pay.web.business.payment.model;

import org.json.JSONObject;

public class CommonPayRes extends BaseRes {
    
    private static final long serialVersionUID = 1L;

    /* 租户标识 */
    private String tenantId;
    /* 订单号 */
    private String orderId;

    /* 支付平台订单号 */
    private String payCenterOrderId;

    /* 预订单号 */
    private String preOrderId;

    /* 支付机构编码 */
    private String payOrgCode;

    /* 订单名称 */
    private String subject;

    /* 订单金额 */
    private String orderAmount;

    /* 支付平台异步通知路径 */
    private String payCenterNotifyUrl;

    /* 终端来源 */
    private String requestSource;

    /* 加密信息 */
    private String infoMd5;
    /* 请求报文 */
    private JSONObject requestMessage;

    public JSONObject getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(JSONObject requestMessage) {
        this.requestMessage = requestMessage;
    }

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

    public String getPayCenterOrderId() {
        return payCenterOrderId;
    }

    public void setPayCenterOrderId(String payCenterOrderId) {
        this.payCenterOrderId = payCenterOrderId;
    }

    public String getPreOrderId() {
        return preOrderId;
    }

    public void setPreOrderId(String preOrderId) {
        this.preOrderId = preOrderId;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getPayCenterNotifyUrl() {
        return payCenterNotifyUrl;
    }

    public void setPayCenterNotifyUrl(String payCenterNotifyUrl) {
        this.payCenterNotifyUrl = payCenterNotifyUrl;
    }

    public String getRequestSource() {
        return requestSource;
    }

    public void setRequestSource(String requestSource) {
        this.requestSource = requestSource;
    }

    public String getInfoMd5() {
        return infoMd5;
    }

    public void setInfoMd5(String infoMd5) {
        this.infoMd5 = infoMd5;
    }
    
}
