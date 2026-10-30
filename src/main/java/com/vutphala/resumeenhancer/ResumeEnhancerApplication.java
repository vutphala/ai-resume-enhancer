package com.vutphala.resumeenhancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResumeEnhancerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeEnhancerApplication.class, args);
    }
}
