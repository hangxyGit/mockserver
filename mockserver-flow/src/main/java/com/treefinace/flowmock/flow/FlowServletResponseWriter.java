package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.flow.model.FlowExpectation;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.model.ScriptConfigModel;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.servlet.responsewriter.ServletResponseWriter;

import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowServletResponseWriter extends ServletResponseWriter {
    ScriptProcessorManager scriptProcessorManager;

    public FlowServletResponseWriter(ScriptProcessorManager scriptProcessorManager, HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        this.scriptProcessorManager = scriptProcessorManager;
    }

    @Override
    public void writeResponse(HttpRequest request, HttpResponse response, boolean apiResponse) {
        if (request instanceof FlowHttpRequest) {
            FlowHttpRequest flowHttpRequest = (FlowHttpRequest) request;
            // 前置处理
            FlowExpectation flowExpectation = flowHttpRequest.getFlowExpectation();
            if (flowExpectation != null && flowExpectation.getPostProcess() != null) {
                Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPostProcess();
                if (scriptConfigsMap != null) {
                    List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap.values().stream().sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex)).collect(Collectors.toList());
                    scriptConfigModelList.stream().forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest));
                }
            }
        }
        super.writeResponse(request, response, apiResponse);
    }
}
