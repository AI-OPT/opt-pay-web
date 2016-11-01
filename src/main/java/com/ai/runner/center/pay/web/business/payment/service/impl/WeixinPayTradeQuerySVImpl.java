package com.ai.runner.center.pay.web.business.payment.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.base.exception.SystemException;
import com.ai.runner.center.pay.web.business.payment.model.TradeQueryRes;
import com.ai.runner.center.pay.web.business.payment.service.interfaces.ITradeQuerySV;
import com.ai.runner.center.pay.web.business.payment.util.third.weixin.CommonUtil;
import com.ai.runner.center.pay.web.system.configcenter.WeixinConfigManager;
import com.ai.runner.center.pay.web.system.constants.ExceptCodeConstants;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 微信支付交易结果查询
 *
 * Date: 2015年12月10日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public class WeixinPayTradeQuerySVImpl implements ITradeQuerySV {
    
    private static final Logger LOG = Logger.getLogger(WeixinPayTradeQuerySVImpl.class);

    @Override
    public TradeQueryRes tradeQuery(String tenantId, String orderId, String tradeOrderId) {
        LOG.info("微信支付订单查询...");
        /* 1.初始化返回信息 */
        TradeQueryRes tradeQueryRes = new TradeQueryRes();
        tradeQueryRes.setTenantId(tenantId);
        tradeQueryRes.setOrderId(orderId);
        tradeQueryRes.setPayOrgCode(PayConstants.PayOrgCode.WEIXIN);
        if (StringUtil.isBlank(tradeOrderId)) {
            throw new SystemException("交易查询失败，传入的交易订单号为空!");
        }
        tradeQueryRes.setTradeOrderId(tradeOrderId);
        LOG.info("微信支付订单查询操作:订单号[" + tradeOrderId + "]");
        Map<String, String> returnMap = this.wxTradeQuery(tenantId, tradeOrderId);
        if (returnMap == null) {
            LOG.error("向微信支付查询此订单[" + tradeOrderId + "]交易信息失败！");
            throw new SystemException("向微信支付查询此订单[" + tradeOrderId + "]交易信息失败！");
        }

        /* 组织返回参数 */
        String return_code = returnMap.get("return_code");
        String result_code = returnMap.get("result_code");
        if (PayConstants.WeixinReturnCode.SUCCESS.equals(return_code)
                && PayConstants.WeixinReturnCode.SUCCESS.equals(result_code)) {
            tradeQueryRes.setReturnCode(PayConstants.ReturnCode.SUCCESS);
        } else {
            String message = "错误码： " + returnMap.get("err_code") + ",错误信息： " + returnMap.get("err_code_des");
            tradeQueryRes.setReturnCode(PayConstants.ReturnCode.FAILD);
            tradeQueryRes.setErrCode(ExceptCodeConstants.TRADE_FAIL);
            tradeQueryRes.setErrMsg(message);
        }
        String trade_state = returnMap.get("trade_state");
        if (PayConstants.WeixinReturnCode.SUCCESS.equals(trade_state)) {
            tradeQueryRes.setTradeStatus(PayConstants.ReturnCode.SUCCESS);
        } else {
            tradeQueryRes.setTradeStatus(PayConstants.ReturnCode.FAILD);
        }
        
        tradeQueryRes.setOutOrderId(returnMap.get("transaction_id"));
        JSONObject attach = new JSONObject();
        Set<Entry<String, String>> set = returnMap.entrySet();
        Iterator<Entry<String, String>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            attach.put(entry.getKey(), entry.getValue());
        }
        tradeQueryRes.setAttach(JSON.toJSONString(attach));
        LOG.info("微信支付订单查询结果返回报文：[" + JSON.toJSONString(tradeQueryRes) + "]");
        return tradeQueryRes;
    }

    
    /**
     * 微信订单交易查询
     * 
     * @param tenantId
     * @param orderId
     * @return
     * @author fanpw
     */
    private Map<String, String> wxTradeQuery(String tenantId, String tradeOrderId) {
        /* 2.将请求传递过来的参数，放到微信请求里 */
        String nonce_str = CommonUtil.getNonceStr();
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_APPID));
        packageParams.put("mch_id", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_MCH_ID));
        packageParams.put("out_trade_no", tradeOrderId);
        packageParams.put("nonce_str", nonce_str);// 随机数
        /* 3.报文信息加密 */
        String sign = CommonUtil.createSign(packageParams, ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_API_KEY));
        packageParams.put("sign", sign);
        /* 4.组织请求报文 */
        String xml = null;
        try {
            xml = CommonUtil.getRequestXml(packageParams);
        } catch (UnsupportedEncodingException ex) {
            LOG.error("获取微信订单交易查询请求xml报文出错", ex);
            throw new SystemException("获取微信订单交易查询请求xml报文出错");
        }
        LOG.info("请求报文:" + xml + "");
        /* 5.发起请求 */
        String createOrderURL = WeixinConfigManager.WEIXIN_PAY_QUERY_GATEWAY;
        return CommonUtil.doRequest(createOrderURL, xml);
    }
}
