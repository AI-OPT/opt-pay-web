package com.ai.runner.center.pay.web.system.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * HTTP请求工具类
 * Date: 2015年12月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class HTTPUtil {


    private static final Logger LOG = Logger.getLogger(HTTPUtil.class);

    private static final int CONNECTIONTIMEOUT = 50000;
    
    /**
     * 发送报文并接收到报文
     * 
     * @param requestUrl
     * @param params
     * @return
     * @author LiangMeng
     */
    public static String httpRequest(String requestUrl, Map<String, String> params,String requestMethod) {
        URL url;
        HttpURLConnection conn;
        String result = null;
        try {
            /* 1.初始化连接 */
            url = new URL(requestUrl);
            LOG.info("请求地址：[ " + requestUrl + "]");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(CONNECTIONTIMEOUT);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            /* 2.构造发送参数 */
            StringBuilder sb = new StringBuilder();
            if (params != null) {
                for (Entry<String, String> e : params.entrySet()) {
                    sb.append(e.getKey());
                    sb.append("=");
                    sb.append(e.getValue());
                    sb.append("&");
                }
                sb.substring(0, sb.length() - 1);
            }
            /* 3.发送报文 */
            OutputStreamWriter writer;
            writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            LOG.info("发送报文：[" + sb + "]");
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            String line;
            StringBuilder builder = new StringBuilder();
            /* 4.接收返回报文 */
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            reader.close();
            LOG.info("接收报文: [" + builder.toString() + "]");
            return builder.toString();
        } catch (Exception e) {
            LOG.error("返回数据有误:" + e.getMessage(), e);
        }
        return result;
    }
    
    
}
