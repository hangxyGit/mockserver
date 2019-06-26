package com.treefinace.flowmock.flow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.treefinace.flowmock.model.ProjectModel;
import com.treefinace.flowmock.service.ProjectService;
import org.apache.commons.collections4.MapUtils;
import org.mockserver.mock.Expectation;
import org.mockserver.server.initialize.ExpectationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * 从redis加载Expectation
 */
public class RedisExpectationInitializer implements ExpectationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RedisExpectationInitializer.class);

    private static ProjectService projectService;

    @Override
    public Expectation[] initializeExpectations() {
        logger.info("start flow expectations init.....");
        if (projectService == null) {
            logger.info("end flow expectations init : project info is null");
            return new Expectation[0];
        }
        List<String> projects = projectService.getAllProjects();
        List<FlowExpectation> flowExpectations = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(projects)) {
            projects.forEach(project -> {
                    ProjectModel projectModel = projectService.getProject(project);
                    Map<String, FlowExpectation> flowExpectationMap = projectModel.getFlowExpectations();
                    if (MapUtils.isNotEmpty(flowExpectationMap)) {
                        if (MapUtils.isNotEmpty(projectModel.getPreProccess())
                            || MapUtils.isNotEmpty(projectModel.getPostProccess())) {
                            for (FlowExpectation expectation : flowExpectationMap.values()) {
                                if (expectation.getPreProccess() == null) {
                                    expectation.setPreProccess(Maps.newHashMap());
                                }
                                if (expectation.getPostProccess() == null) {
                                    expectation.setPostProccess(Maps.newHashMap());
                                }
                                // 继承项目全局的前置处理
                                if (MapUtils.isNotEmpty(projectModel.getPreProccess())) {
                                    projectModel.getPreProccess().keySet().stream().forEach(key -> {
                                        if (!expectation.getPreProccess().containsKey(key)) {
                                            expectation.getPreProccess().put(key, projectModel.getPreProccess().get(key));
                                        }
                                    });
                                }

                                // 继承项目全局的后置处理
                                if (MapUtils.isNotEmpty(projectModel.getPostProccess())) {
                                    projectModel.getPostProccess().keySet().stream().forEach(key -> {
                                        if (!expectation.getPostProccess().containsKey(key)) {
                                            expectation.getPostProccess().put(key, projectModel.getPostProccess().get(key));
                                        }
                                    });
                                }
                            }
                        }
                    }
                    flowExpectations.addAll(flowExpectationMap.values());
                }
            );
        }
        Expectation[] expectations = new Expectation[flowExpectations.size()];
        for (int i = 0; i < flowExpectations.size(); i++) {
            expectations[0] = flowExpectations.get(i);
        }
        logger.info("end flow expectations init : expectations={}", expectations.length);
        return expectations;
    }

    public static ProjectService getProjectService() {
        return projectService;
    }

    public static void setProjectService(ProjectService projectService) {
        RedisExpectationInitializer.projectService = projectService;
    }
}
