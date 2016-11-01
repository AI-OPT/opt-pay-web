package com.ai.runner.center.pay.web.system.configcenter;

import com.ai.runner.center.pay.web.system.constants.PayConstants;

/**
 * 微信支付配置管理类
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author LiangMeng
 */
public class WeixinConfigManager extends AbstractPayConfigManager {

    /**
     * 配置中心微信配置根目录
     */
    public static final String PAY_ORG_NAME = "WEIXIN";
    
    /**
     * 微信公众号相关key
     */
    public static final String WEIXIN_APPID = "WEIXIN_APPID";
    public static final String WEIXIN_MCH_ID = "WEIXIN_MCH_ID";
    public static final String WEIXIN_APPSECRET = "WEIXIN_APPSECRET";
    public static final String WEIXIN_API_KEY = "WEIXIN_API_KEY";
    public static final String WEIXIN_CER_PATH = "WEIXIN_CER_PATH";
    
    /**
     * 微信扫码支付地址
     */
    private static final String WEIXIN_WEB_CODE_ACTION = "/weixin/wxCode";
    /**
     * 微信APP支付地址
     */
    private static final String WEIXIN_APP_CODE_ACTION = "/weixin/wxAppPay";
    /**
     * 微信交易查询响应地址
     */
    private static final String WEIXIN_TRADE_QUERY_ACTION = "/weixin/tradeQuery";

    /**
     * 微信退款响应地址
     */
    private static final String WEIXIN_REFUND_ACTION = "/weixin/refund";
    
    /**
     * 微信统一支付网关
     */
    public static final String WEIXIN_PAY_GATEWAY = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    /**
     * 微信交易查询网关
     */
    public static final String WEIXIN_PAY_QUERY_GATEWAY = "https://api.mch.weixin.qq.com/pay/orderquery";
    /**
     * 微信退费网关
     */
    public static final String WEIXIN_REFUND_GATEWAY = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    /**
     * 微信授权网关
     */
    public static final String WEIXIN_AUTH_GATEWAY = "https://open.weixin.qq.com/connect/oauth2/authorize";
    /**
     * 获取网络授权凭证网关
     */
    public static final String WEIXIN_TOKEN_GATEWAY = "https://api.weixin.qq.com/sns/oauth2/access_token";
    
    /**
     * 根据支付请求端获取对应action
     */
    @Override
    public String getPayActionUrl(String requestSource) {
        if(PayConstants.RequestSource.WEB.equals(requestSource)) {
            return WEIXIN_WEB_CODE_ACTION;
        }else if(PayConstants.RequestSource.APP.equals(requestSource)) {
            return WEIXIN_APP_CODE_ACTION;
        }         
        return null;
    }

    @Override
    public String getRefundActionUrl(String requestSource) {
        return WEIXIN_REFUND_ACTION;
    }

    @Override
    public String getQueryActionUrl(String requestSource) {
        if(PayConstants.RequestSource.WEB.equals(requestSource)) {
            return WEIXIN_TRADE_QUERY_ACTION;
        }         
        return null;
    }

    @Override
    public String getWithDrawActionUrl(String requestSource) {
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
