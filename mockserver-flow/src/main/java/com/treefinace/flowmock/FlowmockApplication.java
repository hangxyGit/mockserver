package com.treefinace.flowmock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages={"com.treefinace.flowmock"})
@EnableScheduling
public class FlowmockApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowmockApplication.class, args);
    }


}
