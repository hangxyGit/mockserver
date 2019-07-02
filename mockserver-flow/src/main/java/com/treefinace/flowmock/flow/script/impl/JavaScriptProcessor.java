package com.treefinace.flowmock.flow.script.impl;

import com.alibaba.fastjson.JSON;
import com.treefinace.flowmock.flow.DynamicJavaCompiler;
import com.treefinace.flowmock.flow.model.FlowHttpRequest;
import com.treefinace.flowmock.flow.model.FlowHttpResponse;
import com.treefinace.flowmock.flow.script.ScriptProcessor;
import com.treefinace.flowmock.model.ScriptConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JavaScriptProcessor implements ScriptProcessor {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    DynamicJavaCompiler dynamicJavaCompiler;

    @Override
    public void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request, FlowHttpResponse response) {
        String javaSource = generateJavaSource(scriptConfigModel);
        dynamicJavaCompiler.invoke(javaSource, "process", new Object[]{request, response});
    }

    String generateJavaSource(ScriptConfigModel scriptConfigModel) {
        String scriptContent = scriptConfigModel.getScriptContent();
        StringBuffer javaSourceBf = new StringBuffer();
        if (scriptContent.contains("public class")) {
            javaSourceBf.append(scriptContent);
        } else {
            String haseCode = (scriptContent.hashCode() > 0 ? scriptContent.hashCode() + "" : "F" + Math.abs(scriptContent.hashCode()));
            javaSourceBf.append("package com.treefinace.flowmock.flow.script.impl;\n")
                .append("import com.treefinace.flowmock.flow.model.FlowHttpRequest;\n")
                .append("import com.treefinace.flowmock.flow.model.FlowHttpResponse;\n")
                .append("import com.google.common.collect.*;\n\n")
                .append("import java.util.*;\n")
                .append("import com.wangyin.npp.util.*;\n")
                .append("import com.alibaba.fastjson.*;\n")
                .append("import static java.nio.charset.StandardCharsets.*;\n")
                .append("import static org.mockserver.model.Header.header;\n")
                .append("import static org.mockserver.model.HttpClassCallback.callback;\n")
                .append("import static org.mockserver.model.HttpRequest.request;\n")
                .append("import static org.mockserver.model.HttpResponse.notFoundResponse;\n")
                .append("import static org.mockserver.model.HttpResponse.response;\n")
                .append("import static org.mockserver.model.HttpStatusCode.*;\n")
                .append("import org.slf4j.Logger;\n")
                .append("import org.slf4j.LoggerFactory;\n")
                .append("import " + JSON.class.getName() + ";\n")
                .append("public class DynamicScript" + haseCode + "{ \n")
                .append("   final Logger logger = LoggerFactory.getLogger(getClass()); \n")
                .append("   public void process(FlowHttpRequest request , FlowHttpResponse response){\n ")
                .append("       try {\n")
                .append("           " + scriptContent + "\n")
                .append("       } catch(Exception e) { \n")
                .append("           logger.error(\"running script error:\",e); \n")
                .append("       }   \n")
                .append("   } \n")
                .append("} \n");
        }
        return javaSourceBf.toString();
    }
}
