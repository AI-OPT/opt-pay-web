package com.ai.runner.center.pay.web.system.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;


/**
 * HTTPS请求工具类
 * Date: 2015年12月2日 <br>
 * Copyright (c) 2015 asiainfo.com <br>
 * 
 * @author LiangMeng
 */
public class HTTPSUtil {

    public interface ResultListener {

        public void onConnectionPoolTimeoutError();

    }

    private static final Logger LOG = Logger.getLogger(HTTPSUtil.class);

    // 连接超时时间，默认10秒
    private static int socketTimeoutConfig = 10000;

    // 传输超时时间，默认30秒
    private static int connectTimeoutConfig = 30000;

    // 请求器的配置
    private static RequestConfig requestConfig;

    // HTTP请求器
    private static CloseableHttpClient httpClient;

    /**
     * 双向认证，需要加载本地证书
     * @param certLocalPath
     * @param certPassword
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @author LiangMeng
     * @ApiDocMethod
     */
    private static void initWithCert(String certLocalPath, String certPassword) throws IOException,
            KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyManagementException {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(certLocalPath));// 加载本地的证书进行https加密传输
        try {
            keyStore.load(instream, certPassword.toCharArray());// 设置证书密码
        } catch (CertificateException e) {
            LOG.error("获取连接异常:" + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("获取连接异常:" + e.getMessage(), e);
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, certPassword.toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        // 根据默认超时限制初始化requestConfig
        requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeoutConfig)
                .setConnectTimeout(connectTimeoutConfig).build();

    }
    
    /**
     * 单向认证，无需校验本地证书
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @author LiangMeng
     * @ApiDocMethod
     */
    private static void init() throws IOException, KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyManagementException {

        httpClient = createSSLClientDefault();

        // 根据默认超时限制初始化requestConfig
        requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeoutConfig)
                .setConnectTimeout(connectTimeoutConfig).build();

    }
    /**
     * 获取https默认连接
     * @return
     * @author LiangMeng
     * @ApiDocMethod
     */
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                    new TrustStrategy() {
                        // 信任所有
                        public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            LOG.error("获取连接异常:" + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("获取连接异常:" + e.getMessage(), e);
        } catch (KeyStoreException e) {
            LOG.error("获取连接异常:" + e.getMessage(), e);
        }
        return HttpClients.createDefault();

    }
    /**
     * 通过Https往API post xml数据
     * 
     * @param url
     *            API地址
     * @param xmlObj
     *            要提交的XML数据对象
     * @return API回包的实际数据
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */

    public static String postXml(String url, String xmlObj, boolean needCert, String certLocalPath,
            String certPassword) throws IOException, KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyManagementException {
        // 初始化连接
        if (needCert) {
            initWithCert(certLocalPath, certPassword);
        } else {
            init();
        }
        String result = null;

        HttpPost httpPost = new HttpPost(url);
        // 将要提交给API的数据对象转换成XML格式数据Post给API
        String postDataXML = xmlObj;

        LOG.info("API，POST过去的数据是：");
        LOG.info(postDataXML);

        // 得指明使用UTF-8编码，否则到API服务器XML的中文不能被成功识别
        StringEntity postEntity = new StringEntity(postDataXML, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.setEntity(postEntity);

        // 设置请求器的配置
        httpPost.setConfig(requestConfig);

        LOG.info("executing request" + httpPost.getRequestLine());

        try {
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();

            result = EntityUtils.toString(entity, "UTF-8");

        } catch (ConnectionPoolTimeoutException e) {
            LOG.error("https请求异常:" + e.getMessage(), e);

        } catch (ConnectTimeoutException e) {
            LOG.error("https请求异常:" + e.getMessage(), e);

        } catch (SocketTimeoutException e) {
            LOG.error("https请求异常:" + e.getMessage(), e);

        } catch (Exception e) {
            LOG.error("https请求异常:" + e.getMessage(), e);

        } finally {
            httpPost.abort();
        }

        return result;
    }
    public static JSONObject httpsQueryStr(String requestUrl, String rquestMethod, String outputStr) {
        JSONObject jsonObject = null;
        try {
            // 创建SSLContext对象，并使用我们制定的新人管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new SecureRandom());
            // 从上述SSLContext对象中得到SSLSockedFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);

            // 设置请求方式（GET/POST）
            conn.setRequestMethod(rquestMethod);

            // 当outputStr不为空时，向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            jsonObject = new JSONObject(buffer.toString());
        } catch (ConnectException ce) {
            LOG.error("链接超时", ce);
        } catch (Exception e) {
            LOG.error("https请求异常", e);
        }
        return jsonObject;
    }
    
    /**
     * 设置连接超时时间
     * 
     * @param socketTimeout
     *            连接时长，默认10秒
     */
    public void setSocketTimeout(int socketTimeout) {
        socketTimeoutConfig = socketTimeout;
        resetRequestConfig();
    }

    /**
     * 设置传输超时时间
     * 
     * @param connectTimeout
     *            传输时长，默认30秒
     */
    public void setConnectTimeout(int connectTimeout) {
        connectTimeoutConfig = connectTimeout;
        resetRequestConfig();
    }

    private void resetRequestConfig() {
        requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeoutConfig)
                .setConnectTimeout(connectTimeoutConfig).build();
    }

    /**
     * 允许商户自己做更高级更复杂的请求器配置
     * 
     * @param requestConfig
     *            设置HttpsRequest的请求器配置
     */
    public void setRequestConfig(RequestConfig requestConfigParam) {
        requestConfig = requestConfigParam;
    }
}
