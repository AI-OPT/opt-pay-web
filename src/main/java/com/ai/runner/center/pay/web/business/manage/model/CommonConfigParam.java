package com.ai.runner.center.pay.web.business.manage.model;

import java.io.Serializable;

/**
 * 租户基础信息配置模型 
 * Date: 2015年12月22日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class CommonConfigParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户秘钥
     */
    private String requestKey;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    

    
}
