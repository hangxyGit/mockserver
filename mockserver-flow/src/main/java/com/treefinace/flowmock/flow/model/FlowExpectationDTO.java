package com.treefinace.flowmock.flow.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.treefinace.flowmock.model.ScriptConfigModel;
import com.treefinace.flowmock.script.template.model.ThirdPartyCallback;
import org.mockserver.mock.Expectation;
import org.mockserver.serialization.model.ExpectationDTO;

import java.util.List;
import java.util.Map;

public class FlowExpectationDTO extends ExpectationDTO {
    //expectationCode
    private String expectationCode;
    // 前置处理
    private Map<String, ScriptConfigModel> preProcess;
    // 后置处理
    private Map<String, ScriptConfigModel> postProcess;

    private List<ThirdPartyCallback> thirdPartyCallbacks;

    public FlowExpectationDTO(Expectation expectation) {
        super(expectation);
    }

    public FlowExpectationDTO() {
        super();
    }

    @Override
    public Expectation buildObject() {
        Expectation expectation = super.buildObject();
        FlowExpectation flowExpectation = new FlowExpectation(expectation.getHttpRequest(), expectation.getTimes(), expectation.getTimeToLive());
        flowExpectation.thenRespond(expectation.getHttpResponse())
            .thenRespond(expectation.getHttpResponseTemplate())
            .thenRespond(expectation.getHttpResponseClassCallback())
            .thenRespond(expectation.getHttpResponseObjectCallback())
            .thenForward(expectation.getHttpForward())
            .thenForward(expectation.getHttpForwardTemplate())
            .thenForward(expectation.getHttpForwardClassCallback())
            .thenForward(expectation.getHttpOverrideForwardedRequest())
            .thenForward(expectation.getHttpForwardObjectCallback());

        flowExpectation.setExpectationCode(this.expectationCode);
        if (this.preProcess != null) {
            flowExpectation.setPreProcess(Maps.newHashMap(this.preProcess));
        }
        if (this.postProcess != null) {
            flowExpectation.setPostProcess(Maps.newHashMap(this.postProcess));
        }
        if (this.thirdPartyCallbacks != null) {
            flowExpectation.setThirdPartyCallbacks(Lists.newArrayList(this.thirdPartyCallbacks));
        }
        return flowExpectation;
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

    public void setExpectationCode(String expectationCode) {
        this.expectationCode = expectationCode;
    }

    public List<ThirdPartyCallback> getThirdPartyCallbacks() {
        return thirdPartyCallbacks;
    }

    public void setThirdPartyCallbacks(List<ThirdPartyCallback> thirdPartyCallbacks) {
        this.thirdPartyCallbacks = thirdPartyCallbacks;
    }
}
