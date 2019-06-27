package com.treefinace.flowmock.flow.script;

import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.script.impl.JavaScriptProcessor;
import com.treefinace.flowmock.model.ScriptConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptProcessorManager implements ScriptProcessor {
    @Autowired
    private JavaScriptProcessor javaScriptProcessor;

    @Override
    public void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request) {
        switch (scriptConfigModel.getScriptType()) {
            case JAVA:
                javaScriptProcessor.process(scriptConfigModel, request);
                break;
            case GROOVY:
                ;
            case REDIS:
                ;
            case MYSQL:
                ;
        }
    }
}
