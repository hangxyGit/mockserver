package com.treefinace.flowmock.flow.model;

import com.treefinace.flowmock.model.ScriptConfigModel;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;

import java.util.Map;

public class FlowExpectation extends Expectation {

    //expectationCode
    private String expectationCode;
    // 前置处理
    private Map<String, ScriptConfigModel> preProcess;
    // 后置处理
    private Map<String, ScriptConfigModel> postProcess;

    public FlowExpectation(HttpRequest httpRequest) {
        super(httpRequest);
    }

    public FlowExpectation(HttpRequest httpRequest, Times times, TimeToLive timeToLive) {
        super(httpRequest, times, timeToLive);
    }

    public String getExpectationCode() {
        return expectationCode;
    }

    public void setExpectationCode(String expectationCode) {
        this.expectationCode = expectationCode;
    }

    public Map<String, ScriptConfigModel> getPreProcess() {
        return preProcess;
    }

    public void setPreProcess(Map<String, ScriptConfigModel> preProcess) {
        this.preProcess = preProcess;
    }

    public Map<String, ScriptConfigModel> getPostProcess() {
        return postProcess;
    }

    public void setPostProcess(Map<String, ScriptConfigModel> postProcess) {
        this.postProcess = postProcess;
    }
}
