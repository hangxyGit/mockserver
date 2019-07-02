package com.treefinace.flowmock.flow.model;

import com.treefinace.flowmock.script.template.model.ScriptHttpResponse;
import org.mockserver.model.HttpResponse;

import javax.servlet.http.HttpServletResponse;

public class FlowHttpResponse extends ScriptHttpResponse {

    public FlowHttpResponse(HttpResponse response, HttpServletResponse origin) {
        super(response, origin);
    }
}
