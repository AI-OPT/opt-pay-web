package com.ai.runner.center.pay.web.business.payment.util.third.unionpay;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ai.opt.sdk.util.StringUtil;
import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.sdk.HttpClient;
import com.ai.runner.center.pay.web.business.payment.util.third.unionpay.sdk.SDKUtil;



/**
 * 银联wap支付工具类
 *
 * Date: 2015年12月7日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class WapUtil {
    
    private static final Logger LOG = Logger.getLogger(WapUtil.class);
 
    private WapUtil(){
        
    }
        
    public static Map<String, String> getAllRequestParam(
            final HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<String, String>();
        Enumeration<String> enumeration = request.getParameterNames();
        if (null != enumeration) {
            while(enumeration.hasMoreElements()) {
                String paramName = enumeration.nextElement();
                String value = request.getParameter(paramName);
                if(!StringUtil.isBlank(value)) {
                    resultMap.put(paramName, value);
                }
            }
        }
        
        return resultMap;
    }
    
    /**
     * 功能：后台交易给银联地址发请求
     * @param contentData 请求报文map
     * @param encoding 上送请求报文域encoding字段的值
     * @return 返回报文 map
     */
    public static Map<String, String> submitUrl(Map<String, String> submitFromData,
            String requestUrl, String encoding) {
        String resultString = "";
        LOG.info("请求地址： " + requestUrl);
        /**
         * 发送后台请求数据
         */
        HttpClient hc = new HttpClient(requestUrl, 30000, 30000);
        try {
            int status = hc.send(submitFromData, encoding);
            if (200 == status) {
                resultString = hc.getResult();
            }
        } catch (Exception ex) {
            LOG.error("http post请求失败", ex);
        }

        if (!StringUtil.isBlank(resultString)) {
            return SDKUtil.convertResultStringToMap(resultString);
        }
        return null;
    }
}
