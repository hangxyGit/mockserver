package com.treefinace.flowmock.controller;

import com.treefinace.flowmock.model.ProjectModel;
import com.treefinace.flowmock.service.ProjectService;
import org.mockserver.mappers.ContentTypeMapper;
import org.mockserver.model.StringBody;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.streams.IOStreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.mockserver.mappers.ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET;

@Controller
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    ProjectService projectService;

    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    @ResponseBody
    public Object update(HttpServletRequest httpServletRequest) throws IOException {
        byte[] bodyBytes = IOStreamUtils.readInputStreamToByteArray(httpServletRequest);
        String contentType = CONTENT_TYPE.toString();
        Charset requestCharset = ContentTypeMapper.getCharsetFromContentTypeHeader(httpServletRequest.getHeader(contentType));
        StringBody stringBody = new StringBody(new String(bodyBytes, requestCharset), DEFAULT_HTTP_CHARACTER_SET.equals(requestCharset) ? null : requestCharset);

        String jsonString = stringBody.toString();
        ProjectModel projectModel = ObjectMapperFactory.createObjectMapper().readValue(jsonString, ProjectModel.class);
        projectService.update(projectModel);
        return new HashMap<>();
    }
}
