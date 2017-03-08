package com.ai.runner.center.pay.web.system.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.sdk.SDKConfig;


/**
 * 支付平台监听器
 *
 * Date: 2015年12月7日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class PaymentListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(PaymentListener.class);
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            //加载sdk配置信息。
            SDKConfig.getConfig().loadPropertiesFromSrc();
            LOG.info("Application initialize successfully!");
        } catch(Exception ex) {
            LOG.error("加载银联支付配置文件出错！");
        }
    }

}
