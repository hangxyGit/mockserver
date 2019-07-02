package com.treefinace.flowmock.script.template.java;

import com.treefinace.flowmock.script.template.model.ScriptHttpRequest;
import com.treefinace.flowmock.script.template.model.ScriptHttpResponse;

public interface ScriptHandler {

    /**
     * 前置处理请求
     *
     * @param request
     */
    void process(ScriptHttpRequest request, ScriptHttpResponse response);
}
