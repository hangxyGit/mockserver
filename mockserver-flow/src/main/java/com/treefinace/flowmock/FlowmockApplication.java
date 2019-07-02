package com.treefinace.flowmock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.treefinace.flowmock"}, exclude = {DataSourceAutoConfiguration.class})
public class FlowmockApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowmockApplication.class, args);
    }


}
