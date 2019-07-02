package com.treefinace.flowmock.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.treefinace.flowmock.flow.FlowMockRefresher;
import com.treefinace.flowmock.model.ProjectModel;
import com.treefinace.flowmock.utils.RedisKeyGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.serialization.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FlowMockRefresher flowMockRefresher;

    public void update(ProjectModel project) {
        String projCode = project.getProjCode();
        String projectsKey = RedisKeyGenerator.get("project");
        stringRedisTemplate.opsForSet().add(projectsKey, projCode);

        String projectBaseKey = RedisKeyGenerator.get("project", projCode);
        Map<String, String> projectBaseMap = Maps.newHashMap();
        projectBaseMap.put("projCode", project.getProjCode());
        if (StringUtils.isNotEmpty(project.getProjName())) {
            projectBaseMap.put("projName", project.getProjName());
        }

        if (StringUtils.isNotEmpty(project.getProjDesc())) {
            projectBaseMap.put("projDesc", project.getProjDesc());
        }

        if (StringUtils.isNotEmpty(project.getProjDomain())) {
            projectBaseMap.put("projDomain", project.getProjDomain());
        }
        stringRedisTemplate.opsForHash().putAll(projectBaseKey, projectBaseMap);

        // map property 写入hash
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(ProjectModel.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            try {
                // map
                if (propertyDescriptor.getPropertyType().isAssignableFrom(Map.class)) {
                    Map<?, ?> map = (Map<?, ?>) propertyDescriptor.getReadMethod().invoke(project);
                    if (MapUtils.isEmpty(map)) {
                        continue;
                    }
                    String propertyRedisKey = RedisKeyGenerator.get("project", projCode, propertyDescriptor.getDisplayName());
                    Map<String, String> propertyStringMap = toJSONStringMap(map);
                    stringRedisTemplate.opsForHash().putAll(propertyRedisKey, propertyStringMap);
                }
                // list
                else if (propertyDescriptor.getPropertyType().isAssignableFrom(List.class)) {
                    String propertyRedisKey = RedisKeyGenerator.get("project", projCode, propertyDescriptor.getDisplayName());
                    List<?> list = (List<?>) propertyDescriptor.getReadMethod().invoke(project);
                    if (CollectionUtils.isNotEmpty(list)) {
                        stringRedisTemplate.opsForValue().set(propertyRedisKey, JSON.toJSONString(list));
                    } else {
                        stringRedisTemplate.delete(propertyRedisKey);
                    }
                }
            } catch (Exception e) {
            }

        }
        // 强制重新刷新mock 配置
        flowMockRefresher.refresh(projCode);
    }

    public ProjectModel getProject(String projectCode) {
        String projectKey = RedisKeyGenerator.get("project", projectCode);
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        Map<String, String> projectBaseMap = hashOperations.entries(projectKey);
        if (projectBaseMap == null || MapUtils.isEmpty(projectBaseMap)) {
            return null;
        }
        String projectJson = JSON.toJSONString(projectBaseMap);
        ProjectModel project = JSON.parseObject(projectJson, ProjectModel.class);

        // map property 写入hash
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(ProjectModel.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            try {
                if (propertyDescriptor.getPropertyType().isAssignableFrom(Map.class)) {
                    String property = propertyDescriptor.getDisplayName();
                    String propertyRedisKey = RedisKeyGenerator.get("project", projectCode, propertyDescriptor.getDisplayName());
                    Map<String, String> propertyStringMap = hashOperations.entries(propertyRedisKey);
                    Type type = ProjectModel.class.getDeclaredField(property).getGenericType();
                    Class<?> clazz = null;
                    if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        clazz = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    }
                    Map<String, ?> propertyMap = toObjectMap(propertyStringMap, clazz);
                    if (MapUtils.isEmpty(propertyMap)) {
                        continue;
                    }
                    propertyDescriptor.getWriteMethod().invoke(project, propertyMap);
                }
                // list
                else if (propertyDescriptor.getPropertyType().isAssignableFrom(List.class)) {
                    String property = propertyDescriptor.getDisplayName();
                    String propertyRedisKey = RedisKeyGenerator.get("project", projectCode, propertyDescriptor.getDisplayName());
                    String json = stringRedisTemplate.opsForValue().get(propertyRedisKey);

                    Type type = ProjectModel.class.getDeclaredField(property).getGenericType();
                    Class<?> clazz = null;
                    if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        clazz = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    }
                    List<?> list = JSON.parseArray(json, clazz);
                    propertyDescriptor.getWriteMethod().invoke(project, list);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return project;
    }

    public <T> Map<String, T> getProjectConfig(String project, String property, Class<T> clazz) {
        String projectKey = RedisKeyGenerator.get("project", project, property);
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        Map<String, String> propertyMap = hashOperations.entries(projectKey);
        return toObjectMap(propertyMap, clazz);
    }

    public List<String> getAllProjects() {
        String projectsKey = RedisKeyGenerator.get("project");
        Set<String> projectSet = stringRedisTemplate.opsForSet().members(projectsKey);
        return Lists.newArrayList(projectSet);
    }


    Map<String, String> toJSONStringMap(Map<?, ?> map) {
        Map<String, String> jsonStringMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((key, config) -> {
                try {
                    jsonStringMap.put(key.toString(), ObjectMapperFactory.createObjectMapper().writeValueAsString(config));
                } catch (JsonProcessingException e) {
                    logger.error("json parse exception:{},json={}", key, config, e);
                }
            });
        }
        return jsonStringMap;
    }

    <T> Map<String, T> toObjectMap(Map<String, String> map, Class<T> clazz) {
        Map<String, T> objectMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((key, json) -> {
                try {
                    objectMap.put(key.toString(), ObjectMapperFactory.createObjectMapper().readValue(json, clazz));
                } catch (IOException e) {
                    logger.error("json parse exception:{},json={}", key, json, e);
                }
            });
        }
        return objectMap;
    }

}
