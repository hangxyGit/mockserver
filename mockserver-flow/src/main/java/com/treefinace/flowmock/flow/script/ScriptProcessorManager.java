package com.treefinace.flowmock.flow.script;

import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.model.FlowHttpResponse;
import com.treefinace.flowmock.flow.script.java.JavaScriptProcessor;
import com.treefinace.flowmock.model.ScriptConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptProcessorManager implements ScriptProcessor {
    @Autowired
    private JavaScriptProcessor javaScriptProcessor;

    @Override
    public void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request, FlowHttpResponse response) {
        ScriptProcessor scriptProcessor = getProcessor(scriptConfigModel);
        if (scriptProcessor != null) {
            scriptProcessor.process(scriptConfigModel, request, response);
        }
    }

    ScriptProcessor getProcessor(ScriptConfigModel scriptConfigModel) {
        switch (scriptConfigModel.getScriptType()) {
            case JAVA:
                return javaScriptProcessor;
            case GROOVY:
                ;
            case REDIS:
                ;
            case MYSQL:
                ;
        }
        return null;
    }
}
