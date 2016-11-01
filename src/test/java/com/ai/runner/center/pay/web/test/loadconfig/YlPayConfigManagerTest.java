package com.ai.runner.center.pay.web.test.loadconfig;

import com.ai.runner.center.pay.web.system.configcenter.YlPayConfigManager;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.google.gson.JsonObject;

public class YlPayConfigManagerTest {

    public static void main(String[] args) {
        addPrivateConfig("OPT-PAY");
        //addPublicConfig();
    }
    
    private static void addPublicConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("acpsdk.frontTransUrl", "https://101.231.204.80:5000/gateway/api/frontTransReq.do");
        json.addProperty("acpsdk.appTransUrl", "https://101.231.204.80:5000/gateway/api/appTransReq.do");
        json.addProperty("acpsdk.backTransUrl", "https://101.231.204.80:5000/gateway/api/backTransReq.do");
        ConfigUtil.addProperty(YlPayConfigManager.PAY_ORG_NAME, json.toString());
    }
    
    private static void addPrivateConfig(String tenantId) {
        JsonObject json = new JsonObject();
        json.addProperty("merId", "777290058115056");
        json.addProperty("acpsdk.signCert.path", "//aifs01/devusers/devrun11/applications/Runner-Pay-Web1S1/yl/certs/acp_test_sign.pfx");
        json.addProperty("acpsdk.signCert.pwd", "000000");
        String config = json.toString(); 
        ConfigUtil.addProperty(tenantId, YlPayConfigManager.PAY_ORG_NAME, config);
    }
    
}
