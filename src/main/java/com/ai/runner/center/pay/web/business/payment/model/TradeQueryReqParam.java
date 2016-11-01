package com.ai.runner.center.pay.web.business.payment.model;

import java.io.Serializable;

/**
 * 外部系统调用支付平台交易查询接口请求参数
 *
 * Date: 2015年11月9日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class TradeQueryReqParam implements Serializable {
    
    private static final long serialVersionUID = 1378275493384041697L;

    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 订单号
     */
    private String orderId;

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

    public String getInfoMd5() {
        return infoMd5;
    }

    public void setInfoMd5(String infoMd5) {
        this.infoMd5 = infoMd5;
    }
    
}
