package com.treefinace.flowmock.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.treefinace.flowmock.model.ProjectModel;
import com.treefinace.flowmock.utils.RedisKeyGenerator;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void update(ProjectModel project) {
        String projCode = project.getProjCode();
        String projectsKey = RedisKeyGenerator.get("project");
        stringRedisTemplate.opsForSet().add(projectsKey);

        String projectBaseKey = RedisKeyGenerator.get("project", projCode);
        Map<String, String> projectBaseMap = Maps.newHashMap();
        projectBaseMap.put("projCode", project.getProjCode());
        projectBaseMap.put("projName", project.getProjName());
        projectBaseMap.put("projDesc", project.getProjDesc());
        projectBaseMap.put("projDomain", project.getProjDomain());
        stringRedisTemplate.opsForHash().putAll(projectBaseKey, projectBaseMap);

        // map property 写入hash
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(ProjectModel.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getPropertyType().isAssignableFrom(Map.class)) {
                try {
                    Map<?, ?> map = (Map<?, ?>) propertyDescriptor.getReadMethod().invoke(project);
                    String propertyRedisKey = RedisKeyGenerator.get("project", projCode, propertyDescriptor.getDisplayName());
                    Map<String, String> propertyStringMap = toJSONStringMap(map);
                    stringRedisTemplate.opsForHash().putAll(propertyRedisKey, propertyStringMap);
                } catch (Exception e) {
                }
            }
        }
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
            if (propertyDescriptor.getPropertyType().isAssignableFrom(Map.class)) {
                try {
                    String property = propertyDescriptor.getDisplayName();
                    String propertyRedisKey = RedisKeyGenerator.get(projectKey, propertyDescriptor.getDisplayName());
                    Map<String, String> propertyStringMap = hashOperations.entries(propertyRedisKey);
                    Type type = ProjectModel.class.getDeclaredField(property).getGenericType();
                    Class<?> clazz = null;
                    if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        clazz = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    }
                    Map<String, ?> propertyMap = toObjectMap(propertyStringMap, clazz);
                    propertyDescriptor.getWriteMethod().invoke(project, propertyMap);
                } catch (Exception e) {
                }
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
            map.forEach((key, config) -> jsonStringMap.put(key.toString(), JSON.toJSONString(config)));
        }
        return jsonStringMap;
    }

    <T> Map<String, T> toObjectMap(Map<String, String> map, Class<T> clazz) {
        Map<String, T> objectMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((key, json) -> objectMap.put(key.toString(), JSON.parseObject(json, clazz)));
        }
        return objectMap;
    }

}
