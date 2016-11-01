package com.ai.runner.center.pay.web.business.manage.model;

import java.io.Serializable;

/**
 * 支付宝配置模型 Date: 2015年12月16日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class AlipayConfigParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 支付宝商户账号（WEB）
     */
    private String webSellerEmail;

    /**
     * 支付宝商户PID（WEB）
     */
    private String webSellerPID;

    /**
     * 支付宝商户秘钥（WEB）
     */
    private String webSellerKey;

    /**
     * 支付宝商户账号（WAP）
     */
    private String wapSellerEmail;

    /**
     * 支付宝商户PID（WAP）
     */
    private String wapSellerPID;

    /**
     * 支付宝商户秘钥（WAP）
     */
    private String wapSellerKey;
    /**
     * 支付宝商户账号（批量付款）
     */
    private String batchTransSellerEmail;

    /**
     * 付款账户名（批量付款）
     */
    private String batchTransAcctName;

    /**
     * 支付宝商户PID（批量付款）
     */
    private String batchTransSellerPID;

    /**
     * 支付宝商户秘钥（批量付款）
     */
    private String batchTransSellerKey;

    public String getBatchTransAcctName() {
        return batchTransAcctName;
    }

    public void setBatchTransAcctName(String batchTransAcctName) {
        this.batchTransAcctName = batchTransAcctName;
    }

    public String getBatchTransSellerEmail() {
        return batchTransSellerEmail;
    }

    public void setBatchTransSellerEmail(String batchTransSellerEmail) {
        this.batchTransSellerEmail = batchTransSellerEmail;
    }

    public String getBatchTransSellerPID() {
        return batchTransSellerPID;
    }

    public void setBatchTransSellerPID(String batchTransSellerPID) {
        this.batchTransSellerPID = batchTransSellerPID;
    }

    public String getBatchTransSellerKey() {
        return batchTransSellerKey;
    }

    public void setBatchTransSellerKey(String batchTransSellerKey) {
        this.batchTransSellerKey = batchTransSellerKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getWebSellerEmail() {
        return webSellerEmail;
    }

    public void setWebSellerEmail(String webSellerEmail) {
        this.webSellerEmail = webSellerEmail;
    }

    public String getWebSellerPID() {
        return webSellerPID;
    }

    public void setWebSellerPID(String webSellerPID) {
        this.webSellerPID = webSellerPID;
    }

    public String getWebSellerKey() {
        return webSellerKey;
    }

    public void setWebSellerKey(String webSellerKey) {
        this.webSellerKey = webSellerKey;
    }

    public String getWapSellerEmail() {
        return wapSellerEmail;
    }

    public void setWapSellerEmail(String wapSellerEmail) {
        this.wapSellerEmail = wapSellerEmail;
    }

    public String getWapSellerPID() {
        return wapSellerPID;
    }

    public void setWapSellerPID(String wapSellerPID) {
        this.wapSellerPID = wapSellerPID;
    }

    public String getWapSellerKey() {
        return wapSellerKey;
    }

    public void setWapSellerKey(String wapSellerKey) {
        this.wapSellerKey = wapSellerKey;
    }

}
