package com.ai.runner.center.pay.web.system.configcenter;

import org.apache.log4j.Logger;

import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;

/**
 * 支付中心配置服务公用管理类
 *
 * Date: 2015年8月25日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public abstract class AbstractPayConfigManager {
      
    private static final Logger LOG = Logger.getLogger(AbstractPayConfigManager.class);
           
    /**
     * 获取是否商用
     */
    public static String getServerType() {
        return ConfigUtil.getProperty(PayConstants.SERVER_TYPE);
    }
    
    /**
     * 获取请求参数加密对应key 
     */
    public static String getRequestKey() {
        return ConfigUtil.getProperty(PayConstants.REQUEST_KEY);
    }
    
    /**
     * 获取请求参数加密对应key 
     */
    public static String getPayUrl() {
        return ConfigUtil.getProperty(PayConstants.PAY_URL);
    }
    
    /**
     * 获取支付跳转地址
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String getPayAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getPayActionUrl(requestSource);
    }
    
    /**
     * 获取退款跳转地址
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String getRefundAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getRefundActionUrl(requestSource);
    }
    
    /**
     * 获取批量退款跳转地址
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String getBatchRefundAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getBatchRefundActionUrl(requestSource);
    }
    
    /**
     * 获取提现跳转地址
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String getWithDrawAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getWithDrawActionUrl(requestSource);
    }
    
    /**
     * 获取批量付款跳转地址
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public static String getBatchTransAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getBatchWithDrawActionUrl(requestSource);
    }
    
    /**
     * 
     * @param payOrgCode
     * @param requestSource
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static String getQueryAction(String payOrgCode, String requestSource) {
        AbstractPayConfigManager manager = getInstance(payOrgCode);
        return manager.getQueryActionUrl(requestSource);
    }
    /**
     * 获取支付配置管理类实例
     * @param payOrgCode
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
	public static AbstractPayConfigManager getInstance(String payOrgCode) {
		AbstractPayConfigManager manager = null;
		if (PayConstants.PayOrgCode.ZFB.equals(payOrgCode)) {
			manager = new AliPayConfigManager();
		} else if (PayConstants.PayOrgCode.WEIXIN.equals(payOrgCode)) {
			manager = new WeixinConfigManager();
		} else if (PayConstants.PayOrgCode.YL.equals(payOrgCode)) {
			manager = new YlPayConfigManager();
		} else if (PayConstants.PayOrgCode.XY.equals(payOrgCode)) {
			manager = new XyPayConfigManager();
		} else if (PayConstants.PayOrgCode.PP.equals(payOrgCode)) {
			manager = new PpPayConfigManager();
		} else {
			final String message = "获取不到该支付类型的： " + payOrgCode + "配置管理类实例，请查看配置或暂不支持此支付方式";
			LOG.error(message);
			throw new SystemException(message);
		}

		return manager;
	}
    
    /**
     * 获取支付响应地址
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public abstract String getPayActionUrl(String requestSource);
    
    /**
     * 获取退款响应地址
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public abstract String getRefundActionUrl(String requestSource);
    
    /**
     * 获取批量退款响应地址
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public abstract String getBatchRefundActionUrl(String requestSource);
    
    /**
     * 获取提现响应地址
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public abstract String getWithDrawActionUrl(String requestSource);
    
    /**
     * 获取批量付款响应地址
     * @param requestSource
     * @return
     * @author fanpw
     * @ApiDocMethod
     * @ApiCode
     */
    public abstract String getBatchWithDrawActionUrl(String requestSource);
    
    /**
     * 获取交易查询地址
     * @param requestSource
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public abstract String getQueryActionUrl(String requestSource);
}
