package com.ai.runner.center.pay.web.system.configcenter;

import com.ai.runner.center.pay.web.system.constants.PayConstants;

/**
 * 支付宝支付配置管理类
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class AliPayConfigManager extends AbstractPayConfigManager {

    /**
     * 配置中心支付宝配置根目录
     */
    public static final String PAY_ORG_NAME = "ZFB";
    
    /**
     * 调试用，创建TXT日志文件夹路径
     */
    public static final String LOG_PATH = "D:\\";

    /**
     * 字符编码格式 目前支持 gbk 或 utf-8
     */
    public static final String INPUT_CHARSET = "utf-8";
    
    /**
     * 签名方式 不需修改
     */
    public static final String SIGN_TYPE = "MD5";
    
    /**
     * 卖家支付宝账号-WEB端
     */
    public static final String WEB_SELLER_EMAIL = "web_seller_email";  
    
    /**
     * 卖家PID-WEB端
     */
    public static final String WEB_SELLER_PID = "web_seller_PID"; 
    
    /**
     * 私钥-WEB端
     */
    public static final String WEB_SELLER_KEY = "web_seller_key"; 
    
    /**
     * 卖家支付宝账号-WAP端
     */
    public static final String WAP_SELLER_EMAIL = "wap_seller_email";
    
    /**
     * 卖家PID-WAP端
     */
    public static final String WAP_SELLER_PID = "wap_seller_PID"; 
    
    /**
     * 私钥-WAP端
     */
    public static final String WAP_SELLER_KEY = "wap_seller_key"; 
    
    /**
     * 付款方支付宝账号
     */
    public static final String BATCH_TRANS_SELLER_EMAIL = "batch_trans_seller_email";
    
    /**
     * 付款账户名
     */
    public static final String BATCH_TRANS_ACCT_NAME = "batch_trans_acct_name"; 
    
    /**
     * 付款方支付宝PID
     */
    public static final String BATCH_TRANS_SELLER_PID = "batch_trans_seller_PID"; 
    
    /**
     * 付款方支付宝私钥
     */
    public static final String BATCH_TRANS_SELLER_KEY = "batch_trans_seller_key"; 
    
    /**
     * 支付宝WEB即时到账交易响应地址
     */
    private static final String ZFB_WEB_PAY_ACTION = "/alipay/webPayment/alipayapi";

    /**
     * 支付宝wap支付响应地址
     */
    private static final String ZFB_WAP_PAY_ACTION = "/alipay/wapPayment/alipayapi";
    
    /**
     * 支付宝app支付响应地址
     */
    private static final String ZFB_APP_PAY_ACTION = "/alipay/appPayment/alipayapi";
    
    /**
     * 支付宝单笔无密退款地址
     */
    private static final String ZFB_NOPWD_REFUND_ACTION = "/alipay/noPwdRefund";
    
    /**
     * 支付宝批量无密退款地址
     */
    private static final String ZFB_BATCH_NOPWD_REFUND_ACTION = "/alipay/batchNoPwdRefund";
    
    /**
     * 支付宝批量付款到支付宝账户地址
     */
    private static final String ZFB_BATCH_TRANS_ACTION = "/alipay/batchTrans/alipayapi";
    
    /**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    public static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";
    
    /**
     * 支付宝提供给商户的服务接入网关URL(WAP支付)
     */
    public static final String ALIPAY_GATEWAY_NEW_WAP = "http://wappaygw.alipay.com/service/rest.htm?";

    /**
     * 根据支付请求端获取对应的支付宝支付地址
     */
    @Override
    public String getPayActionUrl(String requestSource) {
        if(PayConstants.RequestSource.WEB.equals(requestSource)) {
            return ZFB_WEB_PAY_ACTION;
        } else if(PayConstants.RequestSource.WAP.equals(requestSource)) {
            return ZFB_WAP_PAY_ACTION;
        } else if(PayConstants.RequestSource.WEIXIN.equals(requestSource)) {
            return ZFB_WAP_PAY_ACTION;
        } else if(PayConstants.RequestSource.APP.equals(requestSource)) {
            return ZFB_APP_PAY_ACTION;
        } 
        
        return null;
    }

    /**
     * 获取支付宝无密退款地址
     */
    @Override
    public String getRefundActionUrl(String requestSource) {
        return ZFB_NOPWD_REFUND_ACTION;
    }

    @Override
    public String getQueryActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getWithDrawActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getBatchWithDrawActionUrl(String requestSource) {
        return ZFB_BATCH_TRANS_ACTION;
    }

    @Override
    public String getBatchRefundActionUrl(String requestSource) {
        return ZFB_BATCH_NOPWD_REFUND_ACTION;
    }

}
