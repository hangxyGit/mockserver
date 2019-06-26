package com.treefinace.flowmock.flow.script.impl;

import com.alibaba.fastjson.JSON;
import com.treefinace.flowmock.flow.CustomStringJavaCompiler;
import com.treefinace.flowmock.flow.FlowHttpRequest;
import com.treefinace.flowmock.flow.script.ScriptProcessor;
import com.treefinace.flowmock.model.ScriptConfigModel;
import com.wangyin.npp.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JavaScriptProcessor implements ScriptProcessor {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    CustomStringJavaCompiler javaCompiler;

    @Override
    public void process(ScriptConfigModel scriptConfigModel, FlowHttpRequest request) {
        String scriptContent = scriptConfigModel.getScriptContent();

        StringBuffer javaSourceBf = new StringBuffer();
        if (scriptContent.contains("public class")) {
            javaSourceBf.append(scriptContent);
        } else {
            javaSourceBf.append("package com.treefinace.flowmock.flow.script.impl;\n")
                .append("import com.treefinace.flowmock.flow.FlowHttpRequest;\n")
                .append("import " + JSON.class.getName() + ";\n")
                .append("import " + Base64.class.getName() + ";\n")
                .append("public class DynamicScript" + scriptContent.hashCode() + "{ \n")
                .append("       public void handle(FlowHttpRequest request){\n")
                .append(scriptContent)
                .append("       }")
                .append("}");
        }
        String javaSource = javaSourceBf.toString();
        String fullClazzName = CustomStringJavaCompiler.getFullClassName(javaSource);
        logger.info("complier java source : {}", javaSource);
        if (javaCompiler.compiler(javaSource)) {
            try {
                javaCompiler.invoke(fullClazzName, "handle", new Object[]{request});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.error("complier java source failed : {}", javaCompiler.getCompilerMessage());
        }
    }
}
