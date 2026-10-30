package com.vutphala.resumeenhancer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bedrock")
public record BedrockProperties(
        String region,
        int timeoutSeconds,
        int maxResumeChars) {

   
}
