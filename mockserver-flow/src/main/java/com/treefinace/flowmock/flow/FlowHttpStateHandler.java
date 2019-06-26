package com.treefinace.flowmock.flow;

import com.treefinace.flowmock.model.ProjectModel;
import org.mockserver.mock.HttpStateHandler;
import org.mockserver.model.HttpRequest;
import org.mockserver.responsewriter.ResponseWriter;
import org.mockserver.scheduler.Scheduler;

public class FlowHttpStateHandler extends HttpStateHandler {

    public FlowHttpStateHandler(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    public boolean handle(HttpRequest request, ResponseWriter responseWriter, boolean warDeployment) {
        FlowHttpRequest flowHttpRequest = (FlowHttpRequest) request;
        ProjectModel projectModel = flowHttpRequest.getProject();

        // #TODO 新增Expectation
        return super.handle(request, responseWriter, warDeployment);
    }
}
