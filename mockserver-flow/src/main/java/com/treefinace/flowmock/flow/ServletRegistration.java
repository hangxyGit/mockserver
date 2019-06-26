package com.treefinace.flowmock.flow;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletRegistration {

    @Bean
    public ServletRegistrationBean servletRegistrationBean(FlowMockServlet flowMockServlet) {
        return new ServletRegistrationBean(flowMockServlet, "/*");
    }
}
