package com.treefinace.flowmock.flow.model;

import com.treefinace.flowmock.model.ProjectModel;
import com.treefinace.flowmock.script.template.model.ScriptHttpRequest;
import org.mockserver.model.HttpRequest;

import javax.servlet.http.HttpServletRequest;

public class FlowHttpRequest extends ScriptHttpRequest {
    // project info
    private ProjectModel project;

    private FlowExpectation flowExpectation;

    public FlowHttpRequest(HttpRequest httpRequest, HttpServletRequest origin) {
        super(httpRequest, origin);
    }


    public ProjectModel getProject() {
        return project;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }

    public FlowExpectation getFlowExpectation() {
        return flowExpectation;
    }

    public void setFlowExpectation(FlowExpectation flowExpectation) {
        this.flowExpectation = flowExpectation;
    }
}
