package com.ai.runner.center.pay.web.test.loadconfig;

import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.google.gson.JsonObject;

public class WeixinConfigManagerTest {

    public static void main(String[] args) {
        JsonObject json = new JsonObject();
        json.addProperty("WEIXIN_APPID", "wx85ba8a6a248a6a19");
        json.addProperty("WEIXIN_MCH_ID", "1262451901");
        json.addProperty("WEIXIN_APPSECRET", "6088daacce8714610894fefa61261e84");
        json.addProperty("WEIXIN_API_KEY", "EF3F788E6E8CED3A20BE0D54C1965ADA");
        json.addProperty("WEIXIN_CER_PATH", "E:/MVNE/apiclient_cert.p12");
        String config = json.toString();
        ConfigUtil.addProperty("OPT-PAY", "WEIXIN", config);
        System.out.println(ConfigUtil.getProperty("OPT-PAY", "WEIXIN", "WEIXIN_APPSECRET"));
    }

}
