package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.flow.model.FlowExpectation;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.model.FlowHttpResponse;
import com.treefinace.flowmock.flow.script.ScriptProcessorManager;
import com.treefinace.flowmock.model.HandleType;
import com.treefinace.flowmock.model.ScriptConfigModel;
import com.treefinace.flowmock.script.template.model.ThirdPartyCallback;
import com.treefinace.flowmock.utils.HttpClientUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.scheduler.Scheduler;
import org.mockserver.servlet.responsewriter.ServletResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.treefinace.flowmock.utils.HttpClientUtils.HttpConfig;

public class FlowServletResponseWriter extends ServletResponseWriter {
    Logger logger = LoggerFactory.getLogger(getClass());

    ScriptProcessorManager scriptProcessorManager;
    HttpServletResponse httpServletResponse;
    HttpServletRequest httpServletRequest;
    Scheduler scheduler;


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
        // 正常response
        super.writeResponse(flowHttpRequest, flowHttpResponse, apiResponse);
        // response 后置处理
        if (flowExpectation != null && flowExpectation.getPostProcess() != null) {
            Map<String, ScriptConfigModel> scriptConfigsMap = flowExpectation.getPostProcess();
            if (scriptConfigsMap != null) {
                List<ScriptConfigModel> scriptConfigModelList = scriptConfigsMap.values().stream()
                    .filter(scriptConfigModel -> HandleType.after.equals(scriptConfigModel.getHandleType()))
                    .sorted(Comparator.comparing(ScriptConfigModel::getScriptIndex))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptConfigModelList)) {
                    scriptConfigModelList.stream()
                        .forEach(scriptConfigModel -> scriptProcessorManager.process(scriptConfigModel, flowHttpRequest, flowHttpResponse));
                }
            }
        }

        // 回调的处理
        if (flowExpectation != null && CollectionUtils.isNotEmpty(flowExpectation.getThirdPartyCallbacks())) {
            List<ThirdPartyCallback> thirdPartyCallbacks = flowExpectation.getThirdPartyCallbacks();
            List<ThirdPartyCallback> asynCallbacks = thirdPartyCallbacks.stream().filter(ThirdPartyCallback::isAsyn).collect(Collectors.toList());

            // 异步回调
            if (CollectionUtils.isNotEmpty(asynCallbacks)) {
                asynCallbacks.forEach(thirdPartyCallback -> {
                    String urlString = thirdPartyCallback.getUrl();
                    if (thirdPartyCallback.getDelay() == null) {
                        scheduler.submit(() -> {
                            try {
                                String result = request(thirdPartyCallback);
                                logger.info("asyn call url={} , result={}", urlString, result);
                            } catch (Exception e) {
                                logger.error("asyn call url={} exception...", urlString, e);
                            }
                        }, true);
                    } else {
                        scheduler.schedule(() -> {
                            try {
                                String result = request(thirdPartyCallback);
                                logger.info("asyn call url={} , result={}", urlString, result);
                            } catch (Exception e) {
                                logger.error("asyn call url={} exception...", urlString, e);
                            }
                        }, true, thirdPartyCallback.getDelay());
                    }
                });
            }

            // 同步回调
            List<ThirdPartyCallback> syncCallbacks = thirdPartyCallbacks.stream().filter(thirdPartyCallback -> !thirdPartyCallback.isAsyn()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(syncCallbacks)) {
                CountDownLatch latch = new CountDownLatch(syncCallbacks.size());
                syncCallbacks.forEach(thirdPartyCallback -> {
                    String urlString = thirdPartyCallback.getUrl();

                    Runnable runnable = () -> {
                        try {
                            String result = request(thirdPartyCallback);
                            logger.info(" sync call url={} , result={}", urlString, result);
                        } catch (Exception e) {
                            logger.error("sync call url={} exception...", urlString, e);
                        } finally {
                            latch.countDown();
                        }
                    };

                    if (thirdPartyCallback.getDelay() == null) {
                        scheduler.submit(runnable, true);
                    } else {
                        scheduler.schedule(runnable, true, thirdPartyCallback.getDelay());
                    }
                });
                try {
                    latch.await();
                } catch (Exception e) {
                }
            }
        }
    }

    private String request(ThirdPartyCallback thirdPartyCallback) throws Exception {
        HttpConfig httpConfig = HttpConfig.builder()
            .url(thirdPartyCallback.getUrl())
            .method(HttpMethod.resolve(thirdPartyCallback.getMethod()))
            .encoding(thirdPartyCallback.getEncoding())
            .param(thirdPartyCallback.getParam())
            .headers(thirdPartyCallback.getHeaders())
            .timeOut(thirdPartyCallback.getTimeOut())
            .build();
        return HttpClientUtils.send(httpConfig);
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
