package com.vutphala.resumeenhancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/** Sends resume text to an Amazon Bedrock model via Spring AI and returns the completion. */
@Service
public class BedrockAgentService {

    private static final Logger log = LoggerFactory.getLogger(BedrockAgentService.class);

    private final BedrockProperties props;
    private final ChatClient chatClient;
 
    public BedrockAgentService(BedrockProperties props, ChatClient.Builder builder) {
        this.props = props;
        this.chatClient = builder.build();
    }

    public record PromptRequest(String prompt) {}

    @AgentCoreInvocation
    public String chat(PromptRequest request) {
        log.info("AgentCore chat invocation received (promptChars={})",
                request.prompt() == null ? 0 : request.prompt().length());
        return chatClient.prompt()
                .user(request.prompt())
                .call()
                .content();
    }
    
    public String enhance(String resumeText, String jobDescription) {
        int max = props.maxResumeChars();
        String text = resumeText;
        if (resumeText.length() > max) {
            log.warn("Resume truncated from {} to {} characters", resumeText.length(), max);
            text = resumeText.substring(0, max);
        }

        String prompt = buildPrompt(text, jobDescription);
        log.debug("Calling model: resumeChars={}, promptChars={}", text.length(), prompt.length());
        long start = System.currentTimeMillis();
        try {
            String result = chatClient.prompt(prompt).call().content();
            log.debug("Model responded in {} ms", System.currentTimeMillis() - start);
            return result;
        } catch (RuntimeException e) {
            log.error("Model call failed after {} ms: {}", System.currentTimeMillis() - start, e.toString());
            throw new AgentUnavailableException("The AI model call failed: " + e.getMessage());
        }
    }

    static String buildPrompt(String resume, String jobDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("Enhance the following resume. Improve clarity, impact, action verbs, quantified ")
          .append("achievements and ATS keyword coverage. Keep all facts truthful and do not invent ")
          .append("experience. Return: 1) an improved resume in Markdown, 2) a short list of key changes, ")
          .append("3) further suggestions.\n\n");
        if (jobDescription != null && !jobDescription.isBlank()) {
            sb.append("Target job description:\n<job_description>\n")
              .append(jobDescription.strip())
              .append("\n</job_description>\n\n");
        }
        sb.append("<resume>\n").append(resume.strip()).append("\n</resume>");
        return sb.toString();
    }

    
    public static class AgentUnavailableException extends RuntimeException {
        public AgentUnavailableException(String message) {
            super(message);
        }
    }
}
