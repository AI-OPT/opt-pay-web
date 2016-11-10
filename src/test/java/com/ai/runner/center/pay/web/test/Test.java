package com.ai.runner.center.pay.web.test;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class Test {

	public static void main(String[] args) {
		String bizContent="{\"out_trade_no\":\"out_trade_no\",\"total_amount\":\"total_amount\",\"subject\":\"subject\", \"product_code\":\"product_code\" }";
		Map<String, String> sParaTemp = new HashMap<String, String>();
        // 公用回传参数, 本参数必须进行UrlEncode之后才可以发送给支付宝
        sParaTemp.put("passback_params", "tenantId");
//        sParaTemp.put("partner", partner);
//        sParaTemp.put("_input_charset", AliPayConfigManager.INPUT_CHARSET);
//        sParaTemp.put("sec_id", AliPayConfigManager.SIGN_TYPE);
//        sParaTemp.put("format", format);
//        sParaTemp.put("v", v);
        // 业务参数
        sParaTemp.put("out_trade_no", "_" );
        sParaTemp.put("subject", "subject");
        System.out.println(bizContent);
        System.out.println(JSON.toJSONString(sParaTemp));
	}
}
