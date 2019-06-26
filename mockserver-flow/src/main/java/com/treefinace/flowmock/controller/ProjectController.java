package com.treefinace.flowmock.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.treefinace.flowmock.model.ProjectModel;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("/mockconfig/project")
public class ProjectController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/update")
    public String update(ProjectModel project) {
        return "";
    }
}
