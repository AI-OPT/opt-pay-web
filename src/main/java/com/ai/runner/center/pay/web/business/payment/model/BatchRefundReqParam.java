package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 外部系统调用支付平台批量退款功能请求参数
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class BatchRefundReqParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;

    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 批量退款批次号
     */
    private String batchNo;

    /**
     * 退款总笔数
     */
    private String batchNum;

    /**
     * 退款总金额
     */
    private String batchFee;

    /**
     * 退款请求支付机构编码
     */
    private String payOrgCode;
        
    /**
     * 退款数据集:第一笔退款数据#第二笔退款数据#第三笔退款数据...#第N笔退款数据<br>
     * 单笔退款数据格式：订单号^原订单号^退款金额^退款理由
     */
    private String detailData;

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

    public String getDetailData() {
        return detailData;
    }

    public void setDetailData(String detailData) {
        this.detailData = detailData;
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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }

    public String getBatchFee() {
        return batchFee;
    }

    public void setBatchFee(String batchFee) {
        this.batchFee = batchFee;
    }

    public String getPayOrgCode() {
        return payOrgCode;
    }

    public void setPayOrgCode(String payOrgCode) {
        this.payOrgCode = payOrgCode;
    }
    
}
