package com.ai.runner.center.pay.web.system.configcenter;

import com.ai.runner.center.pay.web.system.constants.PayConstants;

/**
 * 兴业配置管理
 * Date: 2015年12月28日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class XyPayConfigManager extends AbstractPayConfigManager {

    /**
     * 配置中心银联配置根目录
     */
    public static final String PAY_ORG_NAME = "XY";
    
    
    /**
     * 字符编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";
    
    /**
     * 商户号
     */
    public static final String APP_ID = "appId";  
  
    /**
     * 兴业APP处理action
     */
    private static final String XY_APP_PAY_ACTION = "/xypay/appPay";
    
    /**
     * 兴业银行单笔资金代付交易响应地址
     */
    private static final String XY_DF_ACTION = "/xypay/withdraw";
    
    /**
     * 根据不同的终端获取对应的支付响应地址
     */
    @Override
    public String getPayActionUrl(String requestSource) {
        if(PayConstants.RequestSource.APP.equals(requestSource)) {
            return XY_APP_PAY_ACTION;
        }
        return null;
    }

    @Override
    public String getRefundActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getWithDrawActionUrl(String requestSource) {
        return XY_DF_ACTION;
    }

    @Override
    public String getQueryActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getBatchWithDrawActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getBatchRefundActionUrl(String requestSource) {
        return null;
    }

}
