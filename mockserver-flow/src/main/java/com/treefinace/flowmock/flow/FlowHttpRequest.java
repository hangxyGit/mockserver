package com.treefinace.flowmock.flow;

import com.google.common.collect.Maps;
import com.treefinace.flowmock.model.ProjectModel;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.NottableString;

import java.util.Map;

public class FlowHttpRequest extends HttpRequest {
    // 一级路径
    private String primaryPath;
    // 域名
    private String domain;

    // project info
    private ProjectModel project;

    private FlowExpectation flowExpectation;
    // 上下文
    private Map<String, Object> context = Maps.newConcurrentMap();

    public FlowHttpRequest(HttpRequest httpRequest) {
        this.withPath(httpRequest.getPath());
        this.withMethod(httpRequest.getMethod());
        this.withBody(httpRequest.getBody());
        this.withHeaders(httpRequest.getHeaders());
        this.withCookies(httpRequest.getCookies());
        this.withSecure(httpRequest.isSecure());
        this.withQueryStringParameters(httpRequest.getQueryStringParameters());
        this.withKeepAlive(httpRequest.isKeepAlive());
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


    public ProjectModel getProject() {
        return project;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }

    public FlowExpectation getFlowExpectation() {
        return flowExpectation;
    }

    public void setFlowExpectation(FlowExpectation flowExpectation) {
        this.flowExpectation = flowExpectation;
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
