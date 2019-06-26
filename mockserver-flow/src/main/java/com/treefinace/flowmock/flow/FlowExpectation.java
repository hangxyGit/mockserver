package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.model.ScriptConfigModel;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;

import java.util.Map;

public class FlowExpectation extends Expectation {
    // 前置处理
    private Map<String, ScriptConfigModel> preProccess;
    // 后置处理
    private Map<String, ScriptConfigModel> postProccess;


    public FlowExpectation(FlowHttpRequest httpRequest) {
        super(httpRequest);
    }

    public FlowExpectation(FlowHttpRequest httpRequest, Times times, TimeToLive timeToLive) {
        super(httpRequest, times, timeToLive);
    }

    public Map<String, ScriptConfigModel> getPreProccess() {
        return preProccess;
    }

    public void setPreProccess(Map<String, ScriptConfigModel> preProccess) {
        this.preProccess = preProccess;
    }

    public Map<String, ScriptConfigModel> getPostProccess() {
        return postProccess;
    }

    public void setPostProccess(Map<String, ScriptConfigModel> postProccess) {
        this.postProccess = postProccess;
    }
}
