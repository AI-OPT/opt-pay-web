package com.ai.runner.center.pay.web.business.payment.util.third.alipay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.log4j.Logger;

import com.ai.runner.center.pay.web.system.configcenter.AliPayConfigManager;

/* *
 *类名：AlipayFunction
 *功能：支付宝接口公用函数类
 *详细：该类是请求、通知返回两个文件所调用的公用函数核心处理文件，不需要修改
 *版本：3.3
 *日期：2012-08-14
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */
public final class AlipayCore {
    
    private static final Logger LOG = Logger.getLogger(AlipayCore.class);
    
    private AlipayCore() {
        
    }
    
	/**
	 * 除去数组中的空值和签名参数
	 * 
	 * @param sArray
	 *            签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {

		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0) {
			return result;
		}

		Set<Entry<String, String>> set = sArray.entrySet();
		Iterator<Entry<String, String>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null || "".equals(value)
					|| "sign".equalsIgnoreCase(key)
					|| "sign_type".equalsIgnoreCase(key)) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * 
     * @param params
     *            需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        StringBuffer prestr = new StringBuffer("");

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            prestr.append(key);
            prestr.append("=");
            prestr.append(value);
            // 拼接时，不包括最后一个&字符
            if (i != keys.size() - 1) {
                prestr.append("&");
            } 
        }

        return prestr.toString();
    }
    
    /** 
     * 把数组所有元素按照固定参数排序，以“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkStringNoSort(Map<String, String> params) {
    	
    	//手机网站支付MD5签名固定参数排序，顺序参照文档说明
    	StringBuilder sb = new StringBuilder();
    	sb.append("service="+params.get("service"));
    	sb.append("&v="+params.get("v"));
    	sb.append("&sec_id="+params.get("sec_id"));
    	sb.append("&notify_data="+params.get("notify_data"));
    	
    	return sb.toString();
    }

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * 
     * @param sWord
     *            要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        String errMsg = "写日志发生错误！";
        try {
            writer = new FileWriter(AliPayConfigManager.LOG_PATH + "alipay_log_"
                    + System.currentTimeMillis() + ".txt");
            writer.write(sWord);
        } catch (IOException ex) {
            LOG.error(errMsg, ex);
        } catch (Exception ex) {
            LOG.error(errMsg, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    LOG.error(errMsg, ex);
                }
            }
        }
    }

    /**
     * 生成文件摘要
     * 
     * @param strFilePath
     *            文件路径
     * @param file_digest_type
     *            摘要算法
     * @return 文件摘要结果
     */
    public static String getAbstract(String strFilePath, String file_digest_type)
            throws IOException {
        PartSource file = new FilePartSource(new File(strFilePath));
        if ("MD5".equals(file_digest_type)) {
            return DigestUtils.md5Hex(file.createInputStream());
        } else if ("SHA".equals(file_digest_type)) {
            return DigestUtils.sha256Hex(file.createInputStream());
        } else {
            return "";
        }
    }
}
