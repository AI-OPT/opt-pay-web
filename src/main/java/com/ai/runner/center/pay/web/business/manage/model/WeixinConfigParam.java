package com.ai.runner.center.pay.web.business.manage.model;

import java.io.Serializable;

/**
 * 微信配置模型 Date: 2015年12月16日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class WeixinConfigParam implements Serializable {

    private static final long serialVersionUID = -3540719229412709575L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 
     */
    private String weixinAppid;

    /**
     * 
     */
    private String weixinMchId;

    /**
     *
     */
    private String weixinAppsecret;

    /**
     * 
     */
    private String weixinApiKey;

    /**
     *  
     */
    private String weixinCerPath;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getWeixinAppid() {
        return weixinAppid;
    }

    public void setWeixinAppid(String weixinAppid) {
        this.weixinAppid = weixinAppid;
    }

    public String getWeixinMchId() {
        return weixinMchId;
    }

    public void setWeixinMchId(String weixinMchId) {
        this.weixinMchId = weixinMchId;
    }

    public String getWeixinAppsecret() {
        return weixinAppsecret;
    }

    public void setWeixinAppsecret(String weixinAppsecret) {
        this.weixinAppsecret = weixinAppsecret;
    }

    public String getWeixinApiKey() {
        return weixinApiKey;
    }

    public void setWeixinApiKey(String weixinApiKey) {
        this.weixinApiKey = weixinApiKey;
    }

    public String getWeixinCerPath() {
        return weixinCerPath;
    }

    public void setWeixinCerPath(String weixinCerPath) {
        this.weixinCerPath = weixinCerPath;
    }

}
