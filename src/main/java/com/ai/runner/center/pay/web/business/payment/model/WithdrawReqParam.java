package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 外部系统调用支付平台单笔提现功能请求参数
 * 
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author fanpw
 */
public class WithdrawReqParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;

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
    private String withDrawFee;

    /**
     * 提现请求支付机构编码
     */
    private String payOrgCode;

    /**
     * 收款方账户信息
     */
    private String receiverAccountInfo;

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

    public String getWithDrawFee() {
        return withDrawFee;
    }

    public void setWithDrawFee(String withDrawFee) {
        this.withDrawFee = withDrawFee;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }

    public String getReceiverAccountInfo() {
        return receiverAccountInfo;
    }

    public void setReceiverAccountInfo(String receiverAccountInfo) {
        this.receiverAccountInfo = receiverAccountInfo;
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
