package com.ai.runner.center.pay.web.business.payment.util.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 支付中心支付相关工具类
 *
 * Date: 2015年11月4日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * @author fanpw
 */
public final class PaymentUtil {

    private static final Logger LOG = Logger.getLogger(PaymentUtil.class);
    
    private PaymentUtil() {
        
    }
    
    /**
     * 在线支付生成form提交
     * 
     * @param actionUrl
     * @param paramMap
     * @return
     */
    public static String generateAutoSubmitForm(String actionUrl, Map<String, String> paramMap) {
        StringBuilder html = new StringBuilder();
        html.append("<script language=\"javascript\">window.onload=function(){document.pay_form.submit();}</script>\n");
        html.append("<form id=\"pay_form\" name=\"pay_form\" action=\"").append(actionUrl)
                .append("\" method=\"post\">\n");
        
        Set<Entry<String, String>> set = paramMap.entrySet();
        Iterator<Entry<String, String>> iterator = set.iterator();
        while(iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            html.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\""
                    + value + "\">\n");
        }
        html.append("</form>\n");
        LOG.debug("提交报文："+html);
        return html.toString();
    }
    
    
}
