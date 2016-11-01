package com.ai.runner.center.pay.web.business.payment.model;

/**
 * 支付平台批量退款接口同步返回报文
 *
 * Date: 2016年1月5日 <br>
 * Copyright (c) 2016 asiainfo.com <br>
 * @author fanpw
 */
public class BatchRefundRes extends BaseRes {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 支付中心批量退款批次号
     */
    private String batchNo;

    /**
     * 退款请求支付机构编码
     */
    private String payOrgCode;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }    
}
