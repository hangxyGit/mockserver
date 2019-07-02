package com.treefinace.flowmock.utils;

import lombok.Builder;
import lombok.Data;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static HttpClient httpClient;

    static {
        try {

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(200); // 最大连接数
            connManager.setDefaultMaxPerRoute(40); // 每个路由连接数


            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // 信任任何链接
            TrustStrategy anyTrustStrategy = new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore,
                anyTrustStrategy).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            httpClient = HttpClients.custom().setConnectionManager(connManager).setSSLSocketFactory(sslConnectionSocketFactory).build();
        } catch (Exception e) {
            logger.error("创建https协议的HttpClient对象出错：{}", e);
        }
    }


    /**
     * 请求资源或服务
     *
     * @param config 请求参数配置
     * @return
     */
    public static String send(HttpConfig config) throws Exception {
        return fmt2String(execute(config), config.getEncoding());
    }

    public static String send(String url) throws Exception {
        HttpConfig httpConfig = HttpConfig.builder().url(url).method(HttpMethod.GET).build();
        return fmt2String(execute(httpConfig), httpConfig.getEncoding());
    }

    /**
     * 转化为字符串
     *
     * @param resp     响应对象
     * @param encoding 编码
     * @return
     */
    private static String fmt2String(HttpResponse resp, String encoding) throws HttpProcessException {
        String body;
        try {
            if (resp.getEntity() != null) {
                body = EntityUtils.toString(resp.getEntity(), encoding);
                logger.info("http response body:{}", body);
            } else {
                // 有可能是head请求
                body = resp.getStatusLine().toString();
            }
            EntityUtils.consume(resp.getEntity());

        } catch (IOException exception) {
            throw new HttpProcessException(exception);
        }
        return body;
    }

    public static HttpResponse execute(HttpConfig config) throws IOException {

        HttpRequestBase request = getRequest(config.getUrl(), config.getMethod(), config.getTimeOut());
        if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(request.getClass())) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            String url = config.getUrl();
            checkHasParas(url, nvps, config.getEncoding());
            HttpEntity entity = map2HttpEntity(nvps, config.getParam(), config.getEncoding());

            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
            logger.info("请求地址：{} 请求参数：{}", config.getUrl(), nvps.toString());

        } else {

            map2Url(config.getUrl(), config.getParam());
            int idx = config.getUrl().indexOf("?");
            logger.info("请求地址：{}", config.getUrl().substring(0, (idx > 0 ? idx : config.getUrl().length())));
        }
        if (config.getHeaders() != null) {
            config.getHeaders().forEach((k, v) -> request.setHeader(k, v));
        }

        return (config.getContext() == null) ?
            httpClient.execute(request) :
            httpClient.execute(request, config.getContext());

    }

    /**
     * 根据请求方法名，获取request对象
     *
     * @param url    资源地址
     * @param method 请求方式
     * @return
     */
    private static HttpRequestBase getRequest(String url, HttpMethod method, int timeOut) {

        HttpRequestBase request = null;
        switch (method) {
            case GET:// HttpGet
                request = new HttpGet(url);
                break;
            case POST:// HttpPost
                request = new HttpPost(url);
                break;
            case HEAD:// HttpHead
                request = new HttpHead(url);
                break;
            case PUT:// HttpPut
                request = new HttpPut(url);
                break;
            case DELETE:// HttpDelete
                request = new HttpDelete(url);
                break;
            case TRACE:// HttpTrace
                request = new HttpTrace(url);
                break;
            case PATCH:// HttpPatch
                request = new HttpPatch(url);
                break;
            case OPTIONS:// HttpOptions
                request = new HttpOptions(url);
                break;
            default:
                request = new HttpPost(url);
                break;
        }

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut).setConnectTimeout(timeOut).setConnectionRequestTimeout(timeOut).build();
        request.setConfig(requestConfig);
        return request;
    }


    /**
     * 检测url是否含有参数，如果有，则把参数加到参数列表中
     *
     * @param url  资源地址
     * @param nvps 参数列表
     * @return 返回去掉参数的url
     * @throws UnsupportedEncodingException
     */
    public static String checkHasParas(String url, List<NameValuePair> nvps,
                                       String encoding) throws UnsupportedEncodingException {
        // 检测url中是否存在参数
        if (url.contains("?") && url.indexOf("?") < url.indexOf("=")) {
            Map<String, Object> map = buildParas(url.substring(url.indexOf("?") + 1));
            map2HttpEntity(nvps, map, encoding);
            url = url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    /**
     * 参数转换，将map中的参数，转到参数列表中
     *
     * @param nvps 参数列表
     * @param map  参数列表（map）
     * @throws UnsupportedEncodingException
     */
    public static HttpEntity map2HttpEntity(List<NameValuePair> nvps, Map<String, Object> map,
                                            String encoding) throws UnsupportedEncodingException {
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
        }
        HttpEntity entity = new UrlEncodedFormEntity(nvps, encoding);
        return entity;
    }

    /**
     * 参数转换，将map中的参数拼接到url上
     *
     * @param url 请求url
     * @param map 参数列表（map）
     * @throws UnsupportedEncodingException
     */
    public static String map2Url(String url, Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                sb.append("&" + entry.getKey() + "=" + entry.getValue());
            }
        }
        if (sb.length() == 0) {
            return url;
        }

        if (url.contains("?") && url.indexOf("?") < url.indexOf("=")) {
            return url + sb.toString();
        } else {
            sb.delete(0, 0);
            return url + "?" + sb.toString();
        }

    }


    /**
     * 生成参数 参数格式“k1=v1&k2=v2”
     *
     * @param paras 参数列表
     * @return 返回参数列表（map）
     */
    public static Map<String, Object> buildParas(String paras) {
        String[] p = paras.split("&");
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < p.length; i++) {

            String[] split = p[i].split("=");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }

        return map;

    }

    public static class HttpProcessException extends Exception {
        public HttpProcessException(Exception e) {
            super(e);
        }

        public HttpProcessException(String msg) {
            super(msg);
        }

        public HttpProcessException(String message, Exception e) {
            super(message, e);
        }
    }

    @Data
    @Builder
    public static class HttpConfig {
        private String url;
        private Map<String, String> headers;
        private Map<String, Object> param;
        private HttpMethod method = HttpMethod.GET;
        private int timeOut = 5000;//单位毫秒
        private String encoding = Charset.defaultCharset().displayName();

        private HttpRequestBase httpRequest;
        private HttpContext context;
    }
}
