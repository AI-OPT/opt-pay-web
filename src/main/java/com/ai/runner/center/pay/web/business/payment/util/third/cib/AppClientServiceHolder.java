package com.ai.runner.center.pay.web.business.payment.util.third.cib;

import org.apache.log4j.Logger;

import com.ai.runner.center.pay.web.system.listener.PaymentListener;
import com.cib.cap4i.appsvr.base.common.AppException;
import com.cib.cap4i.appsvr.client.AppClientService;

/**
 * 使用单例的方式初始化方式，让客户端服务类只初始化一次
 *
 * Date: 2016年1月12日 <br>
 * Copyright (c) 2016 asiainfo.com <br>
 * @author fanpw
 */
public class AppClientServiceHolder {
    
    private static final Logger LOG = Logger.getLogger(PaymentListener.class);
    
    private static final String CONFIG_PATH = "/appsvr_conf.properties";

    private static class InstanceHolder {

        private static final AppClientServiceHolder instance = new AppClientServiceHolder();

    }

    public static AppClientServiceHolder getInstance() {
        return InstanceHolder.instance;
    }
    
    private AppClientService appClientService;
    
    private AppClientServiceHolder() {
        try {
            appClientService = new AppClientService(CONFIG_PATH);
        } catch (AppException ex) {
            LOG.error("初始化兴业客户端服务类失败", ex);
        }
    }

    public AppClientService getAppClientService() {
        return appClientService;
    }
}
