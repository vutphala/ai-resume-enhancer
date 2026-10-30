package com.vutphala.resumeenhancer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BedrockAgentServiceTest {

    @Test
    void promptIncludesResumeAndJobDescription() {
        String p = BedrockAgentService.buildPrompt("Jane Doe, Engineer", "Java developer");
        assertTrue(p.contains("<resume>"));
        assertTrue(p.contains("Jane Doe"));
        assertTrue(p.contains("<job_description>"));
    }
}
