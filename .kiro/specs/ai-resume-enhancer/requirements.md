# Requirements: AI Resume Enhancer

## Introduction

AI Resume Enhancer is a Spring Boot web application. A user uploads a resume (PDF, DOCX or TXT) through a browser UI, optionally pastes a target job description, and receives an improved version of the resume produced by an Amazon Bedrock Agent.

## Glossary

- **App**: the Spring Boot web application (backend and static UI).
- **Agent**: an Amazon Bedrock Agent identified by an agent ID and an alias ID.
- **Resume Text**: plain text extracted from the uploaded file.
- **Enhanced Result**: the text the Agent returns.

## Scope

In scope: upload UI, text extraction, Agent invocation, result display, error handling, configuration.
Out of scope (v1): user accounts, persistence of resumes or results, exporting the result to PDF/DOCX, multi-turn chat with the Agent, OCR of scanned PDFs, creating the Bedrock Agent itself.

## Requirements

### Requirement 1: Upload a resume

**User Story:** As a job seeker, I want to upload my resume from the browser, so that it can be enhanced.

#### Acceptance Criteria
1. WHEN the user opens the root URL THEN the App SHALL show a form with a file input, an optional job description textarea and a submit button.
2. THE file input SHALL accept `.pdf`, `.docx` and `.txt` files.
3. WHEN a file larger than 5 MB is submitted THEN the App SHALL reject it with HTTP 413 and the message "File exceeds the 5MB limit."
4. WHEN a file with another extension is submitted THEN the App SHALL respond with HTTP 400 and a message naming the supported types.
5. WHEN an empty file is submitted THEN the App SHALL respond with HTTP 400.

### Requirement 2: Extract resume text

**User Story:** As a user, I want my document's content read correctly, so that the Agent sees what I wrote.

#### Acceptance Criteria
1. WHEN a PDF is uploaded THEN the App SHALL extract its text with PDFBox.
2. WHEN a DOCX is uploaded THEN the App SHALL extract its text with Apache POI.
3. WHEN a TXT is uploaded THEN the App SHALL read it as UTF-8.
4. IF no text can be extracted (blank result) THEN the App SHALL respond with HTTP 400 "No text could be read from the file."
5. IF the file is corrupt or password protected THEN the App SHALL respond with HTTP 400 and a readable message.
6. THE App SHALL truncate Resume Text to a configurable maximum (default 30,000 characters) before sending it to the Agent.

### Requirement 3: Enhance with a Bedrock Agent

**User Story:** As a user, I want an AI agent to improve my resume, so that it is clearer and better targeted.

#### Acceptance Criteria
1. WHEN a valid resume is submitted THEN the App SHALL call `InvokeAgent` on the configured Agent with a prompt containing the Resume Text.
2. WHERE a job description is supplied THEN the prompt SHALL include it so the Agent can tailor keywords and emphasis.
3. THE prompt SHALL instruct the Agent not to invent experience, and to return an improved resume in Markdown, a list of key changes and further suggestions.
4. THE App SHALL use a new random session ID per request so that no resume content is shared between users.
5. THE App SHALL assemble the streamed response chunks into a single Enhanced Result and return it as JSON `{ "enhanced": "..." }`.
6. IF the Agent ID or alias ID is not configured THEN the App SHALL respond with HTTP 503 "Bedrock agent is not configured."
7. IF the Agent call fails or exceeds the timeout (default 120 s) THEN the App SHALL respond with HTTP 503 and a user-safe message.

### Requirement 4: Show the result

**User Story:** As a user, I want to read and copy the enhanced resume in the page.

#### Acceptance Criteria
1. WHILE a request is in progress THE UI SHALL show a progress message and disable the submit button.
2. WHEN the request succeeds THEN the UI SHALL display the Enhanced Result and a Copy button.
3. WHEN the request fails THEN the UI SHALL display the error message in an element announced to screen readers.
4. THE UI SHALL render the Enhanced Result as plain text (never as HTML).
5. THE UI SHALL be keyboard operable and show visible focus indicators.

### Requirement 5: Configuration and credentials

**User Story:** As an operator, I want to configure the Agent without code changes.

#### Acceptance Criteria
1. THE App SHALL read `BEDROCK_AGENT_ID`, `BEDROCK_AGENT_ALIAS_ID` and `AWS_REGION` from the environment.
2. THE App SHALL obtain AWS credentials from the default AWS credential provider chain and SHALL NOT store credentials in source or config files.
3. THE App SHALL grant only `bedrock:InvokeAgent` on the target agent alias to the runtime identity.

### Requirement 6: Security and privacy

#### Acceptance Criteria
1. THE App SHALL bind to `127.0.0.1` by default; the bind address is configurable via `SERVER_ADDRESS`.
2. THE App SHALL NOT persist uploaded files or Enhanced Results to disk or a database, and SHALL NOT log resume content.
3. IF the App is exposed beyond loopback THEN access SHALL be gated by an authenticating layer (see design, Security section) before any resume data is accepted.
4. THE App SHALL treat Resume Text and model output as untrusted (delimit the resume in the prompt; render output as text).

### Non-functional

- Java 17+, Spring Boot 3.3.x, AWS SDK for Java v2.
- A typical request completes in under 120 s; the UI never blocks the browser.
- Unit-testable components with no network access needed for tests.
