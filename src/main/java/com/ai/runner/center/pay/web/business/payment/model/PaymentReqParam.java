package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 业务系统接入支付平台支付参数封装类
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class PaymentReqParam implements Serializable {

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
     * 订单名称
     */
    private String subject;

    /**
     * 终端来源:<br>
     * 1:web<br>
     * 2:wap<br>
     * 3:app<br>
     * 4:微信<br>
     */
    private String requestSource;

    /**
     * 订单金额
     */
    private String orderAmount;

    /**
     * 币种,1、RMB；2、$;
     */
    private String currencyUnit;
    
    /**
     * 服务器异步通知页面路径
     */
    private String notifyUrl;

    /**
     * 页面跳转同步通知地址
     */
    private String returnUrl;

    /**
     * 操作中断返回地址
     */
    private String merchantUrl;
    /**
     * 支付机构编码
     */
    private String payOrgCode;
    
    private String checkFlag ;
    
    public String getCheckFlag() {
        return checkFlag;
    }

    public void setCheckFlag(String checkFlag) {
        this.checkFlag = checkFlag;
    }

    /**
     * 加密信息
     */
    private String infoMd5;

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRequestSource() {
        return requestSource;
    }

    public void setRequestSource(String requestSource) {
        this.requestSource = requestSource;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getMerchantUrl() {
        return merchantUrl;
    }

    public void setMerchantUrl(String merchantUrl) {
        this.merchantUrl = merchantUrl;
    }

    public String getInfoMd5() {
        return infoMd5;
    }

    public void setInfoMd5(String infoMd5) {
        this.infoMd5 = infoMd5;
    }

	public String getCurrencyUnit() {
		return currencyUnit;
	}

	public void setCurrencyUnit(String currencyUnit) {
		this.currencyUnit = currencyUnit;
	}
    
}
