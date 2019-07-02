package com.treefinace.flowmock.script.template.model;

import org.mockserver.model.HttpResponse;

import javax.servlet.http.HttpServletResponse;

public class ScriptHttpResponse extends HttpResponse {

    private HttpServletResponse origin;

    public ScriptHttpResponse(HttpResponse response, HttpServletResponse origin) {
        this.withBody(response.getBody());
        this.withStatusCode(response.getStatusCode());
        this.withReasonPhrase(response.getReasonPhrase());
        this.withHeaders(response.getHeaders());
        this.withCookies(response.getCookies());
        this.withConnectionOptions(response.getConnectionOptions());
        this.withDelay(response.getDelay());
        this.origin = origin;
    }


    public HttpServletResponse getOrigin() {
        return origin;
    }

    public void setOrigin(HttpServletResponse origin) {
        this.origin = origin;
    }
}
