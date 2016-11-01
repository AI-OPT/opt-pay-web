package com.ai.runner.center.pay.web.test.payment;

import java.util.regex.Pattern;

import org.junit.Test;

import com.ai.runner.center.pay.web.business.payment.util.core.VerifyUtil;
import com.ai.runner.center.pay.web.system.configcenter.AbstractPayConfigManager;

public class PaymentReqBuilder {

    @Test
    public void buildPaymentReq() {
        String tenantId = "BIS-ST";
        String orderId = "20151030p000084591";
        String orderAmount = "0.01";
        String notifyUrl = "http://127.0.0.1:8989/Runner-Pay-Web/demo/testNotify";
        String infoStr = orderId + VerifyUtil.SEPARATOR
                + orderAmount + VerifyUtil.SEPARATOR
                + notifyUrl + VerifyUtil.SEPARATOR
                + tenantId;
        String key = AbstractPayConfigManager.getRequestKey();
        String encodeStr = VerifyUtil.encodeParam(infoStr, key);
        System.out.println(encodeStr);
    }
    
    @Test
    public void buildRefundReq() {
        String tenantId = "BIS-ST";
        String orderId = "20151030p000083502";
        String oriOrderId = "20151030p000083501";
        String refundAmount = "0.01";
        String notifyUrl = "http://127.0.0.1:8989/Runner-Pay-Web/demo/testRefundNotify";
        String infoStr = orderId + VerifyUtil.SEPARATOR
                + oriOrderId + VerifyUtil.SEPARATOR
                + refundAmount + VerifyUtil.SEPARATOR
                + notifyUrl + VerifyUtil.SEPARATOR
                + tenantId;
        String key = AbstractPayConfigManager.getRequestKey();
        String encodeStr = VerifyUtil.encodeParam(infoStr, key);
        System.out.println(encodeStr);
    }
    
    @Test
    public void buildBatchRefundReq() {
        String tenantId = "BIS-ST";
        String detailData = "20151030p000083505^20151030p000083503^0.01^退货#20151030p000083506^20151030p000083504^0.01^退货";
        String notifyUrl = "http://127.0.0.1:8989/Runner-Pay-Web/demo/testRefundNotify";
        String infoStr = detailData + VerifyUtil.SEPARATOR
                + notifyUrl + VerifyUtil.SEPARATOR
                + tenantId;
        String key = AbstractPayConfigManager.getRequestKey();
        String encodeStr = VerifyUtil.encodeParam(infoStr, key);
        System.out.println(encodeStr);
    }
    
    @Test
    public void buildTradeQueryReq() {
        String tenantId = "OPT-PAY";
        String orderId = "20151210030125";
        String infoStr = orderId + VerifyUtil.SEPARATOR
                + tenantId;
        String key = AbstractPayConfigManager.getRequestKey();
        String encodeStr = VerifyUtil.encodeParam(infoStr, key);
        System.out.println(encodeStr);
    }
    
    @Test
    public void test() {
        String money = "999999999999999.01";
        System.out.println(money.matches("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,2})?$"));        
    }
    
}
