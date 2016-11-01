package com.ai.runner.center.pay.web.business.payment.util.third.weixin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.ai.opt.sdk.util.DateUtil;
import com.ai.runner.center.pay.web.system.configcenter.WeixinConfigManager;
import com.ai.runner.center.pay.web.system.constants.PayConstants;
import com.ai.runner.center.pay.web.system.util.ConfigUtil;
import com.ai.runner.center.pay.web.system.util.HTTPSUtil;
import com.ai.runner.center.pay.web.system.util.MD5;
import com.ai.runner.center.pay.web.system.util.PaymentSeqUtil;
import com.ai.runner.center.pay.web.system.util.XMLUtil;


/**
 * 微信支付，处理工具类 Date: 2015年11月30日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class CommonUtil {
    private static final Logger LOG = Logger.getLogger(CommonUtil.class);
    /**
     * 获取随机数
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static String getNonceStr(){
        return DateUtil.getDateString(DateUtil.YYYYMMDDHHMMSS) + PaymentSeqUtil.getRandomNum(4) + "";
    }
    /**
     * 微信支付获取报文加密串
     * @param packageParams
     * @param key
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static String createSign(SortedMap<String, String> packageParams,String key) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        //按微信的方式进行加密
        key = "key="+key;
        LOG.info("转换前参数:" + sb);
        String sign = MD5.sign(sb.toString(), key, "UTF-8").toUpperCase();
        LOG.info("packge签名:" + sign);
        return sign;

    }
    /**
     * 微信支付组织xml请求报文
     * @param parameters
     * @return
     * @throws UnsupportedEncodingException
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static String getRequestXml(SortedMap<String, String> parameters)
            throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append("<" + k + ">" + v + "</" + k + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }
    
    /**
     * 获取二维码链接
     * @param url
     * @param xmlParam
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static String getCodeUrl(String url, String xmlParam) {
        String code_url = null;
        try {
            LOG.info("获取二维码链接微信请求报文:" + xmlParam);
            String xmlResult = HTTPSUtil.postXml(url,xmlParam,false,null,null);
            LOG.info("获取二维码链接微信返回报文:" + xmlResult);
            if (xmlResult.indexOf("FAIL") != -1) {
                return code_url;
            }
            Map<String,String> map = XMLUtil.readStringXmlOut(xmlResult);
            code_url = (String) map.get("code_url");
        } catch (Exception e) {
            LOG.error("微信请求异常:" + e.getMessage(),e);
        }
        return code_url; 
        
    }
    
    /**
     * 获取预订单号
     * @param url
     * @param xmlParam
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static JSONObject getPrepayId(String url, String xmlParam) {        
        JSONObject json = new JSONObject();
        try {
            LOG.info("获取二维码链接微信请求报文:" + xmlParam);
            String xmlResult = HTTPSUtil.postXml(url,xmlParam,false,null,null);
            LOG.info("获取二维码链接微信返回报文:" + xmlResult);
            Map<String,String> map = XMLUtil.readStringXmlOut(xmlResult);

            String return_code = String.valueOf(map.get("return_code"));
            String result_code = String.valueOf(map.get("result_code"));
            if(PayConstants.WeixinReturnCode.SUCCESS.equals(return_code)
                    && PayConstants.WeixinReturnCode.SUCCESS.equals(result_code)){
                String prepay_id = (String) map.get("prepay_id");
                json.put("STATE","OK");
                json.put("MSG",prepay_id);
            }else{
                json.put("STATE","FAILD");
                json.put("MSG","return_msg:"+map.get("return_msg")+",err_code_des:"+map.get("err_code_des"));
            }
        } catch (Exception e) {
            json.put("STATE", "FAILD");
            json.put("MSG", "系统异常:"+e.getMessage());
            LOG.error("微信请求异常:" + e.getMessage(),e);
        }
        return json;
    }
    /**
     * 向微信发起请求
     * @param url
     * @param xmlParam
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static Map<String,String> doRequest(String url, String xmlParam) {
        Map<String,String> map = null;
        try {
            LOG.info("获取二维码链接微信请求报文:" + xmlParam);
            String xmlResult = HTTPSUtil.postXml(url,xmlParam,false,null,null);
            LOG.info("获取二维码链接微信返回报文:" + xmlResult);
            if (xmlResult.indexOf("FAIL") != -1) {
                return map;
            }
            map = XMLUtil.readStringXmlOut(xmlResult);
        } catch (Exception e) {
            LOG.error("微信请求异常:" + e.getMessage(),e);
        }
        return map; 
        
    }
    /**
     * 解析后台通知报文
     * @param request
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static Map<String, String> reciveNotifyInfo(HttpServletRequest request){
        String strxml = null;
        ServletInputStream sis = null;
        Map<String, String> requestInfo = null;
        // 取HTTP请求流长度
        int size = request.getContentLength();
        // 用于缓存每次读取的数据
        byte[] buffer = new byte[size];
        // 用于存放结果的数组
        byte[] xmldataByte = new byte[size];
        int count = 0;
        int rbyte = 0;
        // 循环读取
        try {
            sis = request.getInputStream();
            while (count < size) {
                // 每次实际读取长度存于rbyte中
                rbyte = sis.read(buffer);
                for (int i = 0; i < rbyte; i++) {
                    xmldataByte[count + i] = buffer[i];
                }
                count += rbyte;
            }
            strxml = new String(xmldataByte, "UTF-8");
            LOG.info("微信支付回调报文："+strxml);
            requestInfo = XMLUtil.readStringXmlOut(strxml);
        } catch (IOException e) {
            LOG.error("微信支付回调失败：[" + e.getMessage() + "]", e);
        } catch (Exception e) {
            LOG.error("微信支付回调失败：[" + e.getMessage() + "]", e);
        }
        return requestInfo;
    }
    
    public static WeixinOauth2Token refreshOauth2AccessToken(String code,String tenantId) {
        WeixinOauth2Token wat = null;
        // 拼接请求地址
        String requestUrl = WeixinConfigManager.WEIXIN_TOKEN_GATEWAY;
        requestUrl += "?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        requestUrl = requestUrl.replace("APPID", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_APPID));
        requestUrl = requestUrl.replace("SECRET", ConfigUtil.getProperty(tenantId,
                WeixinConfigManager.PAY_ORG_NAME, WeixinConfigManager.WEIXIN_APPSECRET));
        requestUrl = requestUrl.replace("CODE", code);
        JSONObject jsonObject = HTTPSUtil.httpsQueryStr(requestUrl, "GET", null);
        if (null != jsonObject) {
            LOG.info("获取网页授权凭证:" + jsonObject.toString());
            try {
                wat = new WeixinOauth2Token();
                wat.setAccessToken(jsonObject.getString("access_token"));
                wat.setExpiresIn(jsonObject.getInt("expires_in"));
                wat.setRefreshToken(jsonObject.getString("refresh_token"));
                wat.setOpenId(jsonObject.getString("openid"));
                wat.setScope(jsonObject.getString("scope"));
            } catch (Exception e) {
                LOG.error("获取网页授权凭证失败", e);
                wat = null;
                int errorCode = jsonObject.getInt("errcode");
                String errorMsg = jsonObject.getString("errmsg");
                LOG.error("获取网页授权凭证失败 errcode:{" + errorCode + "},errmsg:{" + errorMsg + "}");
            }
        }
        return wat;
    }
}
