package com.treefinace.flowmock.script.template.model;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.NottableString;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptHttpRequest extends HttpRequest {
    // 一级路径
    private String primaryPath;
    // 域名
    private String domain;
    // 原始请求
    private HttpServletRequest origin;
    // 上下文
    private final Map<String, Object> context = new ConcurrentHashMap<>();

    public ScriptHttpRequest(HttpRequest httpRequest, HttpServletRequest origin) {
        this.withPath(httpRequest.getPath());
        this.withMethod(httpRequest.getMethod());
        this.withBody(httpRequest.getBody());
        this.withHeaders(httpRequest.getHeaders());
        this.withCookies(httpRequest.getCookies());
        this.withSecure(httpRequest.isSecure());
        this.withQueryStringParameters(httpRequest.getQueryStringParameters());
        this.withKeepAlive(httpRequest.isKeepAlive());
        this.origin = origin;
    }

    @Override
    public HttpRequest withPath(NottableString path) {
        String pathString = path.getValue();
        int firstIndex = pathString.indexOf("/");
        String primaryPath = pathString.substring(firstIndex);
        if (primaryPath.length() >= 1 && primaryPath.indexOf("/", 1) != -1) {
            primaryPath = primaryPath.substring(0, primaryPath.indexOf("/", 1));
        } else if (primaryPath.contains(".")) {
            primaryPath = primaryPath.substring(0, primaryPath.indexOf("."));
        }
        this.primaryPath = primaryPath;

        String domain = pathString.substring(0, firstIndex);
        if (domain.contains(":")) {
            domain = domain.substring(0, domain.indexOf(":"));
        }
        this.domain = domain;
        return super.withPath(path);
    }

    public String getPrimaryPath() {
        return primaryPath;
    }

    public String getDomain() {
        return domain;
    }


    public HttpServletRequest getOrigin() {
        return origin;
    }

    public void setOrigin(HttpServletRequest origin) {
        this.origin = origin;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void context(String key, Object value) {
        this.context.put(key, value);
    }

    public void context(Map<String, Object> map) {
        if (map != null) {
            this.context.putAll(map);
        }
    }
}
