package com.ai.runner.center.pay.web.test.loadconfig;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ai.runner.center.pay.web.system.configcenter.AliPayConfigManager;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.google.gson.JsonObject;

public class AlipayConfigManagerTest {

    private static final Logger LOG = Logger.getLogger(AlipayConfigManagerTest.class);
    
//    @Test
//    public void addAlipayPublicConfig() {        
//        JsonObject json = new JsonObject();
//        json.addProperty("ALIPAY_GATEWAY_NEW_WAP", "http://wappaygw.alipay.com/service/rest.htm?");
//        json.addProperty("notify_url_web", "http://124.207.3.10:28038/Runner-Pay/alipay/webPayment/notify_url");
//        json.addProperty("call_back_url_web", "http://124.207.3.10:28038/Runner-Pay/alipay/webPayment/call_back_url");
//        String config = json.toString(); 
//        LOG.info(config);
//        ConfigUtil.addProperty(AliPayConfigManager.PAY_ORG_NAME, config);
//    }
    
//    @Test
//    public void addAlipayPrivateConfig() {               
//        JsonObject json = new JsonObject();
//        json.addProperty("web_seller_email", "admin@101test.com");
//        json.addProperty("web_seller_PID", "2088021319554592");
//        json.addProperty("web_seller_key", "enwig3z3wu07knwxvtoi3x0d6pq160e0");        
//        json.addProperty("wap_seller_email", "hxzfb@huaxiangtelecom.cn");
//        json.addProperty("wap_seller_PID", "2088911130951183");
//        json.addProperty("wap_seller_key", "ea6mhhxkaxhbsvmkf39q1wzjzi061la4");
//        String config = json.toString(); 
//        LOG.info(config);
//        ConfigUtil.addProperty("OPT-PAY", AliPayConfigManager.PAY_ORG_NAME, config);
//    }
    
}
