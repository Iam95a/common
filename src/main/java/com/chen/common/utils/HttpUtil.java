package com.chen.common.utils;


import com.chen.common.http.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HttpUtil {


    private static final int DEF_TIME_OUT = 3000;
    private static final String DEF_CHARSET = "utf-8";

    public static String doPost(String url, String json) {
        // 超时时间为3s
        return doPost(url, json, DEF_TIME_OUT);
    }

    public static String downProxyGet(String url, String filePath, String fileName) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEF_TIME_OUT)
                    .setConnectTimeout(DEF_TIME_OUT)
                    .setProxy(HttpHost.create("http://127.0.0.1:1080")).

                            build();
            httpGet.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response != null) {
                    InputStream stream = response.getEntity().getContent();
                    FileUtil.writeToLocal(filePath, fileName, stream);
                }
            }
        } catch (Exception e) {
        }
        return result;
    }


    public static String doProxyGet(String url, String charSet) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEF_TIME_OUT)
                    .setConnectTimeout(DEF_TIME_OUT)
                    .setProxy(HttpHost.create("http://127.0.0.1:1080")).

                            build();
            httpGet.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response != null) {
                    InputStream stream = response.getEntity().getContent();
                    result = IOUtils.toString(stream, charSet);
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String doGet(String url) {
        // 超时时间为3s
        return doGet(url, DEF_TIME_OUT, DEF_CHARSET);
    }

    public static String doGet(String url, String charSet) {
        return doGet(url, DEF_TIME_OUT, charSet);
    }


    public static String doPost(String url, String json, int timeOut) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            if (StringUtils.isNotBlank(json)) {
                StringEntity entity = new StringEntity(json, DEF_CHARSET);
                entity.setContentEncoding(DEF_CHARSET);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut)
                    .setConnectTimeout(timeOut).build();
            httpPost.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response != null) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, DEF_CHARSET).trim();
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * post请求重试1次
     *
     * @param url     请求url
     * @param json    参数
     * @param timeOut 超时时间
     * @param retry   是否重试
     * @return
     */
    public static String doPostRetry(String url, String json, int timeOut, boolean retry) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            if (StringUtils.isNotBlank(json)) {
                StringEntity entity = new StringEntity(json, DEF_CHARSET);
                entity.setContentEncoding(DEF_CHARSET);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut)
                    .setConnectTimeout(timeOut).build();
            httpPost.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response != null) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, DEF_CHARSET).trim();
                    }
                }
            }
        } catch (ConnectTimeoutException cte) {
            if (retry) {
                // 超时重试一次
                return doPostRetry(url, json, timeOut, false);
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String doGet(String url, int timeOut, String charset) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut)
                    .setConnectTimeout(timeOut).build();
            httpGet.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response != null) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, charset).trim();
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String doPostNvp(String url, Map<String, String> params, int timeOut) {
        String result = null;
        // try() 关闭流
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            // 处理参数
            if (null != params && !params.isEmpty()) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                Set<String> keys = params.keySet();
                for (String key : keys) {
                    nvps.add(new BasicNameValuePair(key, params.get(key)));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            }

            // 设置超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut)
                    .setConnectTimeout(timeOut).build();
            httpPost.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response != null) {
                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        result = EntityUtils.toString(resEntity, DEF_CHARSET).trim();
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

}