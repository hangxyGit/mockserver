package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.model.ScriptConfigModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.HttpStateHandler;
import org.mockserver.mock.action.ActionHandler;
import org.mockserver.model.HttpRequest;
import org.mockserver.proxy.ProxyConfiguration;
import org.mockserver.responsewriter.ResponseWriter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FlowActionHandler extends ActionHandler {
    FlowHttpStateHandler flowHttpStateHandler;
    ScriptProcessorManager scriptProcessorManager;


    public FlowActionHandler(ScriptProcessorManager scriptProcessorManager, EventLoopGroup eventLoopGroup, FlowHttpStateHandler httpStateHandler, ProxyConfiguration proxyConfiguration) {
        super(eventLoopGroup, httpStateHandler, proxyConfiguration);
        this.scriptProcessorManager = scriptProcessorManager;
    }

    @Override
    public void processAction(HttpRequest request, ResponseWriter responseWriter, ChannelHandlerContext ctx, Set<String> localAddresses, boolean proxyingRequest, boolean synchronous) {
        FlowHttpRequest flowHttpRequest = (FlowHttpRequest) request;
        Expectation expectation = flowHttpStateHandler.firstMatchingExpectation(request);
        // 业务流程类的mock
        if (expectation instanceof FlowExpectation) {
            // 前置处理
            FlowExpectation flowExpectation = (FlowExpectation) expectation;
            Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPreProccess();
            List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap.values().stream().sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex)).collect(Collectors.toList());
            scriptConfigModelList.stream().forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest));
        }
        super.processAction(request, responseWriter, ctx, localAddresses, proxyingRequest, synchronous);
    }
}
