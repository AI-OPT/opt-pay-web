package com.ai.runner.center.pay.web.test.simulator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ai.runner.center.pay.web.system.util.HTTPUtil;

/**
 * 模拟支付宝后台通知
 *
 * Date: 2015年11月5日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class AlipayNotifySimulator {
    
    private static final Logger LOG = Logger.getLogger(AlipayNotifySimulator.class);

    @Test
    public void testAliPayWapNotify() {
        String notifyData = "<notify><subject>1440-苹果 6 plus</subject><trade_no>2015120900001000590063055033</trade_no><buyer_email>15010207836</buyer_email><out_trade_no>0000320151209112749</out_trade_no><notify_time>20151209232752</notify_time><trade_status>TRADE_SUCCESS</trade_status><seller_email>hxzfb@huaxiangtelecom.cn</seller_email><notify_id>128447</notify_id></notify>";
        //String notifyUrl ="http://localhost:8989/Runner-Pay-Web/alipay/wapNotify";
        String notifyUrl ="http://10.1.228.222:14111/Runner-Pay/alipay/wapNotify";
        Map<String, String> params = new HashMap<String, String>();
        params.put("notify_data", notifyData);
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("通知成功，返回：[" + returnStr + "]");
    }

    @Test
    public void testAliPayWebNotify() {
        //String notifyUrl ="http://localhost:8989/Runner-Pay-Web/alipay/webNotify";
        String notifyUrl ="http://10.1.228.222:14111/Runner-Pay/alipay/webNotify";
        Map<String, String> params = new HashMap<String, String>();
        params.put("subject", "1440-苹果 6 plus");
        params.put("trade_no", "2015120921001004590064843254");
        params.put("buyer_email", "15010207836");
        params.put("out_trade_no", "0000320151209065840");
        params.put("notify_time", "20151209185839");
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("seller_email", "admin@101test.com");
        params.put("notify_id", "1236993");     
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("通知成功，返回：[" + returnStr + "]");
    }
    
    @Test
    public void testNoPwdRefundNotify() {
        String notifyUrl ="http://localhost:8989/Runner-Pay-Web/alipay/noPwdRefundNotify";
        Map<String, String> params = new HashMap<String, String>();
        params.put("batch_no", "2015111000001747105");
        params.put("success_num", "1");
        params.put("result_details", "2015111021001004590057494712^0.01^SUCCESS");
        params.put("notify_type", "trade_status_sync");
        params.put("notify_time", "20151110135336");
        params.put("notify_id", "1234992");     
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("通知成功，返回：[" + returnStr + "]");
    }
    
    @Test
    public void testBatchNoPwdRefundNotify() {
        String notifyUrl ="http://localhost:8989/Runner-Pay-Web/alipay/batchNoPwdRefundNotify";
        Map<String, String> params = new HashMap<String, String>();
        params.put("batch_no", "2015111100001004579");
        params.put("success_num", "1");
        params.put("result_details", "2015111021001004590061668904^0.01^SUCCESS#2015111021001004590061400884^0.01^TRADE_NOT_EXISTS");
        params.put("notify_type", "trade_status_sync");
        params.put("notify_time", "20151110135336");
        params.put("notify_id", "1234993");     
        String returnStr = HTTPUtil.httpRequest(notifyUrl, params,"POST");
        LOG.info("通知成功，返回：[" + returnStr + "]");
    }
    
}
