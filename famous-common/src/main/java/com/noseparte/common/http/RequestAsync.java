package com.noseparte.common.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public abstract class RequestAsync {
    private static Logger LOG = LoggerFactory
            .getLogger(RequestAsync.class);

    public abstract void execute() throws Exception;

    protected void async(String baseUrl, boolean isPost,
                         List<KeyValuePair> urlParams,
                         List<KeyValuePair> postBody, FutureCallback callback)
            throws Exception {

        if (baseUrl == null) {
            LOG.error("we don't have base url, check config");
            throw new Exception("missing base url");
        }

        HttpRequestBase httpMethod;
        CloseableHttpAsyncClient httpAsyncClient = null;
        try {
            httpAsyncClient = HttpClientFactory.getInstance().getHttpAsyncClientPool().getAsyncHttpClient();
            httpAsyncClient.start();
            HttpClientContext localContext = HttpClientContext.create();
            BasicCookieStore cookieStore = new BasicCookieStore();

            if (isPost) {
                httpMethod = new HttpPost(baseUrl);
                if (null != postBody) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("async post postBody={}", postBody);
                    }
                    JSONObject jsonObject = new JSONObject();
                    for (KeyValuePair keyValuePair : postBody) {
                        jsonObject.put(keyValuePair.name, keyValuePair.value);
                    }
                    StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    ((HttpPost) httpMethod).setEntity(entity);
                }
            } else {
                httpMethod = new HttpGet(baseUrl);
                if (null != urlParams) {
                    String getUrl = EntityUtils
                            .toString(new UrlEncodedFormEntity(urlParams));
                    httpMethod.setURI(new URI(httpMethod.getURI().toString()
                            + "?" + getUrl));
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("async getparams:" + httpMethod.getURI());
            }
            localContext.setAttribute(HttpClientContext.COOKIE_STORE,
                    cookieStore);
            httpAsyncClient.execute(httpMethod, localContext, callback);

        } catch (Exception e) {
            LOG.error("RequestAsync send error: ", e);
        }

    }

    protected void async(String baseUrl, List<KeyValuePair> postBody, FutureCallback callback)
            throws Exception {
        this.async(baseUrl, true, null, postBody, callback);
    }

    protected String getHttpContent(HttpResponse response) {

        HttpEntity entity = response.getEntity();
        String body = null;

        if (entity == null) {
            return null;
        }

        try {
            body = EntityUtils.toString(entity, "utf-8");
        } catch (ParseException e) {
            LOG.error("the response's content inputstream is corrupt", e);
        } catch (IOException e) {
            LOG.error("the response's content inputstream is corrupt", e);
        }
        return body;
    }

}
