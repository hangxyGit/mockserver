package com.treefinace.flowmock.flow.script;

import com.treefinace.flowmock.flow.FlowHttpRequest;
import com.treefinace.flowmock.model.ScriptConfigModel;

public interface ScriptProcessor {

    void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request);
}
