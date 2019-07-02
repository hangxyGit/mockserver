package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.flow.model.FlowExpectation;
import com.treefinace.flowmock.flow.model.FlowExpectationDTO;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.model.HandleType;
import com.treefinace.flowmock.model.ScriptConfigModel;
import com.treefinace.flowmock.script.template.model.ThirdPartyCallback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mockserver.client.NettyHttpClient;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.HttpStateHandler;
import org.mockserver.mock.action.ActionHandler;
import org.mockserver.model.HttpRequest;
import org.mockserver.proxy.ProxyConfiguration;
import org.mockserver.responsewriter.ResponseWriter;
import org.mockserver.scheduler.Scheduler;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FlowActionHandler extends ActionHandler {
    FlowHttpStateHandler flowHttpStateHandler;
    ScriptProcessorManager scriptProcessorManager;
    NettyHttpClient httpClient;
    Scheduler scheduler;



    public FlowActionHandler(ScriptProcessorManager scriptProcessorManager, EventLoopGroup eventLoopGroup, FlowHttpStateHandler httpStateHandler, ProxyConfiguration proxyConfiguration) {
        super(eventLoopGroup, httpStateHandler, proxyConfiguration);
        this.scriptProcessorManager = scriptProcessorManager;
        this.flowHttpStateHandler = httpStateHandler;
        this.scheduler = httpStateHandler.getScheduler();
        // 强制获取父类httpClient
        try {
            Field nameField = ActionHandler.class.getDeclaredField("httpClient");
            Field modifiersField = Field.class.getDeclaredField("modifiers"); //①
            modifiersField.setAccessible(true);
            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL); //②
            nameField.setAccessible(true);
            httpClient = (NettyHttpClient) nameField.get(this);
        } catch (Exception e) {
        }
    }

    @Override
    public void processAction(HttpRequest request, ResponseWriter responseWriter, ChannelHandlerContext ctx, Set<String> localAddresses, boolean proxyingRequest, boolean synchronous) {
        FlowHttpRequest flowHttpRequest = (FlowHttpRequest) request;
        Expectation expectation = flowHttpStateHandler.firstMatchingExpectation(request);
        ((FlowServletResponseWriter) responseWriter).setScheduler(scheduler);

        FlowExpectation flowExpectation = null;
        // 业务流程类的mock
        if (expectation != null && expectation instanceof FlowExpectation) {
            // 前置处理
            flowExpectation = (FlowExpectation) expectation;
            flowHttpRequest.setFlowExpectation(flowExpectation);

            Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPreProcess();
            if (MapUtils.isNotEmpty(scriptConfigsMap)) {
                List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap
                    .values()
                    .stream()
//                    .filter(scriptConfigModel -> HandleType.pre.equals(scriptConfigModel.getHandleType()))
                    .sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex))
                    .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(scriptConfigModelList)) {
                    scriptConfigModelList.stream()
                        .forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest, null));
                }
            }
        }
        super.processAction(request, responseWriter, ctx, localAddresses, proxyingRequest, synchronous);
    }
}
