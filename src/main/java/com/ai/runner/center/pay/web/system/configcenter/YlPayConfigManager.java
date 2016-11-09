package com.ai.runner.center.pay.web.system.configcenter;

import com.ai.runner.center.pay.web.system.constants.PayConstants;

/**
 * 银联支付配置管理类
 *
 * Date: 2015年11月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class YlPayConfigManager extends AbstractPayConfigManager {

    /**
     * 配置中心银联配置根目录
     */
    public static final String PAY_ORG_NAME = "YL";
    
    /**
     * 商户号
     */
    public static final String MERID = "merId";  
    
    /**
     * 签名证书路径
     */
    public static final String SDK_SIGNCERT_PATH = "acpsdk.signCert.path";
    
    /**
     * 签名证书密码
     */
    public static final String SDK_SIGNCERT_PWD = "acpsdk.signCert.pwd";
    
    /**
     * 签名证书类型
     */
    public static final String SDK_SIGNCERT_TYPE = "acpsdk.signCert.type";
    
    /**
     * 银联手机网页支付网关
     */
    public static final String FRONT_TRANS_URL = "acpsdk.frontTransUrl";
    
    /**
     * 银联手机控件支付网关
     */
    public static final String APP_TRANS_URL = "acpsdk.appTransUrl";
   
    /**
     * 银联退货/代付交易网关
     */
    public static final String BACK_TRANS_URL = "acpsdk.backTransUrl";
    
    /**
     * 字符编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";
    
    /**
     * 银联手机网页支付响应地址
     */
    private static final String YL_WAP_PAY_ACTION = "/ylpay/wapPay";
    
    /**
     * 银联手机控件支付响应地址
     */
    private static final String YL_APP_PAY_ACTION = "/ylpay/appPay";
    
    /**
     * 银联退货交易响应地址
     */
    private static final String YL_REFUND_ACTION = "/ylpay/refund";
    
    /**
     * 银联单笔代付交易响应地址
     */
    private static final String YL_DF_ACTION = "/ylpay/withdraw";
    
    /**
     * 根据不同的终端获取对应的支付响应地址
     */
    @Override
    public String getPayActionUrl(String requestSource) {
        if(PayConstants.RequestSource.WAP.equals(requestSource) || PayConstants.RequestSource.WEB.equals(requestSource)) {
            return YL_WAP_PAY_ACTION;
        } else if(PayConstants.RequestSource.APP.equals(requestSource)) {
            return YL_APP_PAY_ACTION;
        }
        
        return null;
    }

    /**
     * 获取银联退款地址
     */
    @Override
    public String getRefundActionUrl(String requestSource) {
        return YL_REFUND_ACTION;
    }

    @Override
    public String getQueryActionUrl(String requestSource) {
        return null;
    }

    @Override
    public String getWithDrawActionUrl(String requestSource) {
        return YL_DF_ACTION;
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
