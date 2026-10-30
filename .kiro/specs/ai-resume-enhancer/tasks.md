# Implementation Plan: AI Resume Enhancer

- [x] 1. Project scaffolding
  - Maven `pom.xml` (Spring Boot 3.3.5, AWS SDK v2 BOM, `bedrockagentruntime`, PDFBox, POI), `.gitignore`, `application.properties`
  - _Requirements: 5.1, 6.1_

- [x] 2. Configuration binding
  - `BedrockProperties` record with `isConfigured()`
  - _Requirements: 5.1, 5.2_

- [x] 3. Text extraction
  - [x] 3.1 `ResumeTextExtractor` for PDF, DOCX, TXT; unsupported type exception
  - [ ] 3.2 Unit tests with small fixtures for each type and for a corrupt file
  - _Requirements: 2.1-2.5_

- [x] 4. Bedrock Agent integration
  - [x] 4.1 `BedrockAgentService` with prompt builder, truncation, random session ID, streamed chunk join, timeout, error wrapping
  - [x] 4.2 Prompt unit test
  - [ ] 4.3 Test the 503 "not configured" path
  - _Requirements: 2.6, 3.1-3.7_

- [x] 5. REST endpoint
  - [x] 5.1 `POST /api/enhance` and exception mapping (400/413/503)
  - [ ] 5.2 `MockMvc` tests for 200, 400, 413, 503 with a mocked service
  - _Requirements: 1.3-1.5, 2.4, 2.5, 3.5-3.7_

- [x] 6. Browser UI
  - Upload form, progress state, result panel, copy button, accessible error region, text-only rendering
  - _Requirements: 1.1, 1.2, 4.1-4.5_

- [ ] 7. Build verification
  - `mvn clean verify` passes
  - _Requirements: all_

- [ ] 8. Bedrock setup and manual end-to-end test
  - Create and prepare agent, alias, IAM policy; set env vars; upload a sample resume and confirm the result
  - _Requirements: 3.1, 5.1-5.3_

- [ ] 9. README
  - Prerequisites, agent setup, env vars, run instructions, security note on exposure
  - _Requirements: 5, 6_

- [ ] 10. Before any non-local deployment (not part of v1)
  - Authentication layer, rate limiting, container/deploy config with unpublished or loopback-only ports
  - _Requirements: 6.3_

## Future (v2)

- Stream results to the UI (SSE)
- OCR for scanned PDFs (Textract)
- Export enhanced resume to DOCX/PDF
- Multi-turn refinement with the same Agent session
