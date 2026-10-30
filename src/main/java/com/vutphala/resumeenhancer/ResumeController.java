package com.vutphala.resumeenhancer;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    private final ResumeTextExtractor extractor;
    private final BedrockAgentService agent;

    public ResumeController(ResumeTextExtractor extractor, BedrockAgentService agent) {
        this.extractor = extractor;
        this.agent = agent;
    }

    @PostMapping("/enhance")
    public Map<String, String> enhance(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "jobDescription", required = false) String jobDescription)
            throws IOException {
        log.info("Enhance request received: file='{}', size={} bytes, jobDescriptionProvided={}",
                file.getOriginalFilename(), file.getSize(), jobDescription != null && !jobDescription.isBlank());
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a resume file to upload.");
        }
        String text = extractor.extract(file);
        log.info(text);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("No text could be read from the file.");
        }
        long start = System.currentTimeMillis();
        String enhanced = agent.enhance(text, jobDescription);
        log.info("Enhance request completed in {} ms (response {} chars)",
                System.currentTimeMillis() - start, enhanced == null ? 0 : enhanced.length());
        return Map.of("enhanced", enhanced);
    }

    @ExceptionHandler({IllegalArgumentException.class, ResumeTextExtractor.UnsupportedFileTypeException.class})
    ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> tooLarge() {
        log.warn("Upload rejected: file exceeds the size limit");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", "File exceeds the 5MB limit."));
    }

    @ExceptionHandler(BedrockAgentService.AgentUnavailableException.class)
    ResponseEntity<Map<String, String>> agentDown(BedrockAgentService.AgentUnavailableException e) {
        log.error("Agent unavailable: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<Map<String, String>> unreadable(IOException e) {
        log.warn("Could not read uploaded file: {}", e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "The file could not be read. It may be corrupted or password protected."));
    }
}
