package com.treefinace.flowmock.flow.script;

import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.model.FlowHttpResponse;
import com.treefinace.flowmock.model.ScriptConfigModel;

public interface ScriptProcessor {

    void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request, FlowHttpResponse response);
}
