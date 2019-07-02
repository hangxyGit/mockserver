package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.flow.model.FlowExpectation;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.model.FlowHttpResponse;
import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.model.HandleType;
import com.treefinace.flowmock.model.ScriptConfigModel;
import org.apache.commons.collections4.CollectionUtils;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.servlet.responsewriter.ServletResponseWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowServletResponseWriter extends ServletResponseWriter {
    ScriptProcessorManager scriptProcessorManager;

    HttpServletResponse httpServletResponse;
    HttpServletRequest httpServletRequest;

    public FlowServletResponseWriter(ScriptProcessorManager scriptProcessorManager, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        this.scriptProcessorManager = scriptProcessorManager;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public void writeResponse(HttpRequest request, HttpResponse response, boolean apiResponse) {
        FlowHttpRequest flowHttpRequest = getFlowHttpRequest(request);
        FlowHttpResponse flowHttpResponse = getFlowHttpResponse(response);
        // response 前置处理
        FlowExpectation flowExpectation = flowHttpRequest.getFlowExpectation();
        if (flowExpectation != null && flowExpectation.getPostProcess() != null) {
            Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPostProcess();
            if (scriptConfigsMap != null) {
                List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap.values().stream()
                    .filter(scriptConfigModel -> HandleType.pre.equals(scriptConfigModel.getHandleType()))
                    .sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptConfigModelList)) {
                    scriptConfigModelList.stream()
                        .forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest, flowHttpResponse));
                }
            }
        }
        super.writeResponse(flowHttpRequest, flowHttpResponse, apiResponse);
        // response 后置处理
        if (flowExpectation != null && flowExpectation.getPostProcess() != null) {
            Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPostProcess();
            if (scriptConfigsMap != null) {
                List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap.values().stream()
                    .filter(scriptConfigModel -> HandleType.post.equals(scriptConfigModel.getHandleType()))
                    .sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptConfigModelList)) {
                    scriptConfigModelList.stream()
                        .forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest, flowHttpResponse));
                }
            }
        }

    }

    private FlowHttpRequest getFlowHttpRequest(HttpRequest request) {
        FlowHttpRequest flowHttpRequest;
        if (request instanceof FlowHttpRequest) {
            flowHttpRequest = (FlowHttpRequest) request;
        } else {
            flowHttpRequest = new FlowHttpRequest(request, httpServletRequest);
        }
        return flowHttpRequest;
    }

    private FlowHttpResponse getFlowHttpResponse(HttpResponse response) {
        FlowHttpResponse flowHttpResponse;
        if (response instanceof FlowHttpResponse) {
            return (FlowHttpResponse) response;
        }
        return new FlowHttpResponse(response, httpServletResponse);
    }
}
