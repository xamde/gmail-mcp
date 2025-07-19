# Gmail MCP Server - Junior Developer Implementation Plan

## Overview
This document provides a detailed, 5-phase implementation plan for building a Gmail MCP (Model Context Protocol) server in Java. Each phase has clear deliverables, detailed testing instructions, and implementation guidance suitable for junior developers.

**Key Principles:**
- Each phase builds upon the previous one
- Every phase has a working, testable deliverable
- Focus on Gmail access without requiring users to set up Google Cloud projects
- Use OAuth 2.0 "installed application" flow for simple user authentication
- Clear step-by-step instructions with expected inputs/outputs

---

## Phase 1: Project Foundation & Build Infrastructure

### Goal
Establish a working Java project with all necessary dependencies, build system, and basic infrastructure. After this phase, you'll have a runnable Spring Boot application with MCP server capabilities.

### Deliverables
- Spring Boot project with Maven/Gradle build system
- All required dependencies configured
- Basic application structure
- Health check endpoint working
- MCP server framework initialized

### Implementation Steps

#### Step 1.1: Create Spring Boot Project Structure
```bash
# Create project using Spring Initializr or manually
mkdir gmail-mcp-server
cd gmail-mcp-server
```

**Goal:** Set up basic Maven project structure
**Files to create:**
- `pom.xml` (or `build.gradle`)
- `src/main/java/com/example/gmailmcp/GmailMcpApplication.java`
- `src/main/resources/application.yml`
- `src/test/java/` (test directory structure)

#### Step 1.2: Configure Dependencies
Add these dependencies to `pom.xml`:
```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring AI MCP Server -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
    
    <!-- Google Gmail API -->
    <dependency>
        <groupId>com.google.apis</groupId>
        <artifactId>google-api-services-gmail</artifactId>
        <version>v1-rev20240530-2.0.0</version>
    </dependency>
    
    <!-- Google OAuth Client -->
    <dependency>
        <groupId>com.google.oauth-client</groupId>
        <artifactId>google-oauth-client-jetty</artifactId>
        <version>1.34.1</version>
    </dependency>
    
    <!-- Spring Boot Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Step 1.3: Create Main Application Class
**File:** `src/main/java/com/example/gmailmcp/GmailMcpApplication.java`
```java
@SpringBootApplication
public class GmailMcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmailMcpApplication.class, args);
    }
}
```

#### Step 1.4: Configure Application Properties
**File:** `src/main/resources/application.yml`
```yaml
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.example.gmailmcp: DEBUG
    org.springframework.ai: INFO
```

### How to Test Phase 1

#### Test 1.1: Build Project
**Input:**
```bash
mvn clean compile
```
**Expected Output:**
- Build succeeds without errors
- All dependencies download successfully
- Classes compile without issues

#### Test 1.2: Run Application
**Input:**
```bash
mvn spring-boot:run
```
**Expected Output:**
```
Started GmailMcpApplication in X.XXX seconds
```
- Application starts on port 8080
- No startup errors in logs

#### Test 1.3: Health Check Endpoint
**Input:**
```bash
curl http://localhost:8080/actuator/health
```
**Expected Output:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {...}
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Test 1.4: MCP Server Initialization
**Input:** Check application logs during startup
**Expected Output:** Look for logs indicating MCP server components are loading (exact format depends on Spring AI MCP implementation)

### Implementation Comments
- Use Maven for simpler dependency management
- Start with basic Spring Boot configuration
- Ensure all Google API dependencies are compatible versions
- Set up proper logging early for debugging
- Create separate profiles for development and production

---

## Phase 2: OAuth Authentication Flow (No Google Cloud Project Required)

### Goal
Implement Gmail OAuth 2.0 authentication using the "installed application" flow that doesn't require users to create Google Cloud projects. After this phase, the application can authenticate with Gmail API using a user's personal Google account.

### Deliverables
- OAuth 2.0 authentication service
- Token storage and refresh mechanism
- Test Gmail account authentication working
- Documentation for users to enable Gmail API

### Implementation Steps

#### Step 2.1: Create Authentication Configuration
**File:** `src/main/resources/application.yml` (add to existing)
```yaml
gmail:
  auth:
    credentials-path: ${user.home}/.gmail-mcp/credentials.json
    tokens-directory: ${user.home}/.gmail-mcp/tokens
    scopes:
      - https://www.googleapis.com/auth/gmail.modify
      - https://www.googleapis.com/auth/gmail.compose
      - https://www.googleapis.com/auth/gmail.readonly
```

#### Step 2.2: Create OAuth Service
**File:** `src/main/java/com/example/gmailmcp/service/GoogleAuthService.java`
```java
@Service
@Slf4j
public class GoogleAuthService {
    
    @Value("${gmail.auth.credentials-path}")
    private String credentialsPath;
    
    @Value("${gmail.auth.tokens-directory}")
    private String tokensDirectory;
    
    @Value("#{'${gmail.auth.scopes}'.split(',')}")
    private List<String> scopes;
    
    private Gmail gmailService;
    
    @PostConstruct
    public void initialize() {
        // Implementation for OAuth flow
    }
    
    public Gmail getGmailService() {
        // Return authenticated Gmail service
    }
    
    private Credential authorize() throws IOException, GeneralSecurityException {
        // Implement OAuth 2.0 flow
    }
}
```

#### Step 2.3: Implement OAuth Flow
**Key methods to implement:**
1. Load credentials from JSON file
2. Set up local redirect server for OAuth callback
3. Handle authorization code flow
4. Store and refresh tokens
5. Provide authenticated Gmail service

#### Step 2.4: Create User Setup Documentation
**File:** `SETUP.md`
Document the simplified process for users:
1. Enable Gmail API in their personal Google account (no Cloud Project needed)
2. Create OAuth 2.0 credentials for "Desktop Application"
3. Download credentials JSON file
4. Place file in configured location

### How to Test Phase 2

#### Test 2.1: Credentials File Loading
**Input:**
1. Create a test credentials JSON file (can use dummy data initially)
2. Place in configured path
3. Start application

**Expected Output:**
- Application loads credentials without errors
- Logs show credentials file found and parsed

#### Test 2.2: OAuth Flow Initiation
**Input:**
1. Delete any existing tokens
2. Start application
3. Trigger authentication (via REST endpoint or startup)

**Expected Output:**
- Browser opens with Google OAuth consent screen
- OR Console shows authorization URL to copy/paste
- User can grant permissions

#### Test 2.3: Token Storage and Retrieval
**Input:**
1. Complete OAuth flow once
2. Restart application

**Expected Output:**
- No new OAuth flow required
- Application uses stored refresh token
- Logs show successful authentication

#### Test 2.4: Gmail API Connection Test
**Input:**
Create test endpoint:
```java
@RestController
public class TestController {
    
    @GetMapping("/test/gmail-connection")
    public ResponseEntity<String> testGmailConnection() {
        // Test basic Gmail API call (e.g., get user profile)
    }
}
```

**Expected Output:**
```bash
curl http://localhost:8080/test/gmail-connection
```
Returns success message with user's Gmail profile info

### Implementation Comments
- Use FileDataStoreFactory for token persistence
- Implement proper error handling for network issues
- Add retry logic for token refresh
- Create clear user documentation for OAuth setup
- Test with a real Gmail account early and often

---

## Phase 3: Basic Gmail API Integration & Core Tools

### Goal
Implement core Gmail operations as testable methods and expose them as MCP tools. After this phase, you can send, read, and search emails through the MCP interface.

### Deliverables
- Gmail service wrapper with core operations
- MCP tools for email sending, reading, and searching
- Integration tests with real Gmail account
- Error handling and validation

### Implementation Steps

#### Step 3.1: Create Gmail Operations Service
**File:** `src/main/java/com/example/gmailmcp/service/GmailOperationsService.java`
```java
@Service
@Slf4j
public class GmailOperationsService {
    
    private final GoogleAuthService authService;
    
    public String sendEmail(String to, String subject, String body) {
        // Implementation
    }
    
    public EmailDetails readEmail(String messageId) {
        // Implementation
    }
    
    public List<EmailSummary> searchEmails(String query, int maxResults) {
        // Implementation
    }
}
```

#### Step 3.2: Create Data Transfer Objects
**File:** `src/main/java/com/example/gmailmcp/dto/EmailDetails.java`
**File:** `src/main/java/com/example/gmailmcp/dto/EmailSummary.java`

#### Step 3.3: Implement MCP Tools
**File:** `src/main/java/com/example/gmailmcp/tools/GmailTools.java`
```java
@Component
@Slf4j
public class GmailTools {
    
    @Tool("Send an email with the specified recipient, subject, and body")
    public String sendEmail(
        @P("Recipient email address") String to,
        @P("Email subject line") String subject,
        @P("Email body content") String body) {
        // Call GmailOperationsService
    }
    
    @Tool("Read the full content of an email by its message ID")
    public EmailDetails readEmail(@P("Gmail message ID") String messageId) {
        // Implementation
    }
    
    @Tool("Search emails using Gmail search syntax")
    public List<EmailSummary> searchEmails(
        @P("Gmail search query") String query,
        @P("Maximum number of results") int maxResults) {
        // Implementation
    }
}
```

### How to Test Phase 3

#### Test 3.1: Send Email Functionality
**Input:**
```bash
# Via REST endpoint or MCP client
POST /mcp/tools/sendEmail
{
  "to": "your-test-email@gmail.com",
  "subject": "Test Email from Gmail MCP",
  "body": "This is a test email sent through the Gmail MCP server."
}
```

**Expected Output:**
- Email successfully sent (check target inbox)
- Return message ID or success confirmation
- No errors in server logs

#### Test 3.2: Email Search
**Input:**
```bash
POST /mcp/tools/searchEmails
{
  "query": "subject:Test",
  "maxResults": 10
}
```

**Expected Output:**
```json
[
  {
    "messageId": "abc123",
    "from": "sender@example.com",
    "subject": "Test Email",
    "snippet": "This is a preview...",
    "date": "2024-01-15T10:30:00Z"
  }
]
```

#### Test 3.3: Read Email Content
**Input:**
```bash
POST /mcp/tools/readEmail
{
  "messageId": "abc123"
}
```

**Expected Output:**
```json
{
  "messageId": "abc123",
  "from": "sender@example.com",
  "to": ["recipient@example.com"],
  "subject": "Test Email",
  "bodyText": "Full email content...",
  "bodyHtml": "<html>Full email content...</html>",
  "date": "2024-01-15T10:30:00Z",
  "attachments": []
}
```

#### Test 3.4: Error Handling
**Input:**
```bash
# Test invalid email
POST /mcp/tools/sendEmail
{
  "to": "invalid-email",
  "subject": "Test",
  "body": "Test"
}
```

**Expected Output:**
- Appropriate error message
- No server crash
- Proper HTTP status code

#### Test 3.5: MCP Tool Discovery
**Input:**
```bash
GET /mcp/tools
```

**Expected Output:**
List of available tools with descriptions matching the @Tool annotations

### Implementation Comments
- Implement proper MIME message creation for emails
- Handle both plain text and HTML email bodies
- Add input validation for email addresses
- Implement pagination for search results
- Use proper exception handling and return meaningful errors
- Test with various Gmail search query formats

---

## Phase 4: Advanced Email Management & Attachment Handling

### Goal
Extend email functionality with management operations (mark read/unread, delete, trash) and attachment support. After this phase, you have a comprehensive email management system through MCP.

### Deliverables
- Email management tools (mark read/unread, delete, trash)
- Attachment upload and download capabilities
- Bulk operations support
- Comprehensive error handling

### Implementation Steps

#### Step 4.1: Implement Email Management Operations
**File:** `src/main/java/com/example/gmailmcp/tools/GmailManagementTools.java`
```java
@Component
@Slf4j
public class GmailManagementTools {
    
    @Tool("Mark an email as read")
    public boolean markAsRead(@P("Gmail message ID") String messageId) {
        // Implementation
    }
    
    @Tool("Mark an email as unread")
    public boolean markAsUnread(@P("Gmail message ID") String messageId) {
        // Implementation
    }
    
    @Tool("Move an email to trash")
    public boolean trashEmail(@P("Gmail message ID") String messageId) {
        // Implementation
    }
    
    @Tool("Permanently delete an email")
    public boolean deleteEmail(@P("Gmail message ID") String messageId) {
        // Implementation
    }
}
```

#### Step 4.2: Add Attachment Support
**Enhance existing tools:**
```java
@Tool("Send an email with optional attachments")
public String sendEmailWithAttachments(
    @P("Recipient email address") String to,
    @P("Email subject line") String subject,
    @P("Email body content") String body,
    @P("List of file paths to attach") List<String> attachmentPaths) {
    // Implementation
}

@Tool("Download an email attachment")
public String downloadAttachment(
    @P("Gmail message ID") String messageId,
    @P("Attachment ID") String attachmentId,
    @P("Local path to save attachment") String savePath) {
    // Implementation
}
```

#### Step 4.3: Enhance Email Details with Attachments
Update `EmailDetails` DTO to include attachment information:
```java
public class EmailDetails {
    // ... existing fields
    private List<AttachmentInfo> attachments;
}

public class AttachmentInfo {
    private String attachmentId;
    private String filename;
    private String mimeType;
    private long sizeInBytes;
}
```

### How to Test Phase 4

#### Test 4.1: Mark Email as Read/Unread
**Input:**
1. Search for an unread email
2. Note its message ID
3. Call markAsRead tool
4. Verify in Gmail web interface

**Expected Output:**
- Email shows as read in Gmail
- Tool returns `true`
- No errors

#### Test 4.2: Email with Attachments
**Input:**
1. Create a test file: `echo "Test attachment content" > /tmp/test.txt`
2. Send email with attachment:
```bash
POST /mcp/tools/sendEmailWithAttachments
{
  "to": "your-test-email@gmail.com",
  "subject": "Test with Attachment",
  "body": "This email has an attachment.",
  "attachmentPaths": ["/tmp/test.txt"]
}
```

**Expected Output:**
- Email received with attachment
- Attachment opens correctly
- File content matches original

#### Test 4.3: Download Attachment
**Input:**
1. Find an email with attachments using readEmail
2. Note attachment ID
3. Download attachment:
```bash
POST /mcp/tools/downloadAttachment
{
  "messageId": "abc123",
  "attachmentId": "def456",
  "savePath": "/tmp/downloaded-attachment.txt"
}
```

**Expected Output:**
- File saved to specified path
- Content matches original attachment
- Tool returns success message

#### Test 4.4: Trash and Delete Operations
**Input:**
1. Send a test email to yourself
2. Find the message ID
3. Trash the email
4. Verify it's in trash folder
5. Permanently delete if needed

**Expected Output:**
- Email moves to trash when trashed
- Email disappears when deleted
- Operations return `true` on success

### Implementation Comments
- Use Gmail's modify API for read/unread operations
- Implement proper MIME multipart handling for attachments
- Add file size limits for attachments
- Validate file paths to prevent security issues
- Handle various attachment MIME types correctly
- Implement progress reporting for large file operations

---

## Phase 5: Production Readiness & Advanced Features

### Goal
Make the application production-ready with comprehensive configuration, monitoring, security features, and optional advanced capabilities like calendar integration.

### Deliverables
- Comprehensive configuration management
- Health checks and monitoring
- Security hardening
- Docker containerization
- Complete documentation
- Optional calendar integration

### Implementation Steps

#### Step 5.1: Enhanced Configuration Management
**File:** `src/main/resources/application.yml` (complete version)
```yaml
server:
  port: ${SERVER_PORT:8080}

gmail:
  auth:
    credentials-path: ${GMAIL_CREDENTIALS_PATH:${user.home}/.gmail-mcp/credentials.json}
    tokens-directory: ${GMAIL_TOKENS_DIR:${user.home}/.gmail-mcp/tokens}
    scopes:
      - https://www.googleapis.com/auth/gmail.modify
      - https://www.googleapis.com/auth/gmail.compose
      - https://www.googleapis.com/auth/gmail.readonly
  api:
    timeout-seconds: ${GMAIL_API_TIMEOUT:30}
    max-search-results: ${GMAIL_MAX_SEARCH:25}
    max-attachment-size-mb: ${GMAIL_MAX_ATTACHMENT:25}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    com.example.gmailmcp: ${LOG_LEVEL:INFO}
    org.springframework.ai: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE:logs/gmail-mcp.log}
```

#### Step 5.2: Health Checks and Monitoring
**File:** `src/main/java/com/example/gmailmcp/health/GmailHealthIndicator.java`
```java
@Component
public class GmailHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check Gmail API connectivity
        // Check token validity
        // Return health status
    }
}
```

#### Step 5.3: Security Enhancements
- Input validation for all tool parameters
- Rate limiting for API calls
- Secure token storage
- Request/response logging (without sensitive data)

#### Step 5.4: Docker Support
**File:** `Dockerfile`
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**File:** `docker-compose.yml`
```yaml
version: '3.8'
services:
  gmail-mcp:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
    environment:
      - GMAIL_CREDENTIALS_PATH=/app/config/credentials.json
      - GMAIL_TOKENS_DIR=/app/config/tokens
```

#### Step 5.5: Optional Calendar Integration
**File:** `src/main/java/com/example/gmailmcp/tools/CalendarTools.java`
```java
@Component
@Slf4j
public class CalendarTools {
    
    @Tool("Create a calendar event")
    public String createCalendarEvent(
        @P("Event summary/title") String summary,
        @P("Start time (ISO 8601)") String startTime,
        @P("End time (ISO 8601)") String endTime,
        @P("Event description") String description,
        @P("List of attendee emails") List<String> attendees) {
        // Implementation
    }
}
```

### How to Test Phase 5

#### Test 5.1: Configuration Management
**Input:**
1. Set environment variables:
```bash
export SERVER_PORT=9090
export GMAIL_MAX_SEARCH=50
export LOG_LEVEL=DEBUG
```
2. Start application

**Expected Output:**
- Application starts on port 9090
- Max search results limited to 50
- Debug logs visible
- All configuration overrides work

#### Test 5.2: Health Endpoints
**Input:**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

**Expected Output:**
- Health endpoint shows Gmail connectivity status
- Metrics endpoint provides application metrics
- Custom health indicators work

#### Test 5.3: Docker Container
**Input:**
```bash
docker build -t gmail-mcp .
docker run -p 8080:8080 \
  -v ./config:/app/config \
  -e GMAIL_CREDENTIALS_PATH=/app/config/credentials.json \
  gmail-mcp
```

**Expected Output:**
- Container builds successfully
- Application runs in container
- Volume mounts work correctly
- All functionality available

#### Test 5.4: Security and Validation
**Input:**
```bash
# Test invalid email address
POST /mcp/tools/sendEmail
{
  "to": "not-an-email",
  "subject": "Test",
  "body": "Test"
}

# Test oversized attachment
POST /mcp/tools/sendEmailWithAttachments
{
  "attachmentPaths": ["/path/to/huge-file.zip"]
}
```

**Expected Output:**
- Proper validation error messages
- No security vulnerabilities
- Rate limiting works (if implemented)

#### Test 5.5: Calendar Integration (Optional)
**Input:**
```bash
POST /mcp/tools/createCalendarEvent
{
  "summary": "Test Meeting",
  "startTime": "2024-01-20T10:00:00-08:00",
  "endTime": "2024-01-20T11:00:00-08:00",
  "description": "Test calendar integration",
  "attendees": ["colleague@example.com"]
}
```

**Expected Output:**
- Calendar event created successfully
- Event appears in Google Calendar
- Attendees receive invitations

### Implementation Comments
- Use Spring Boot profiles for different environments
- Implement comprehensive logging without exposing sensitive data
- Add graceful shutdown handling
- Use connection pooling for Gmail API calls
- Implement proper exception handling and user-friendly error messages
- Add performance monitoring and metrics

---

## Parallel Development Opportunities

### Phases that can be developed in parallel:

1. **Phase 1 & Documentation**: While setting up the project structure, documentation can be written in parallel
2. **Phase 3 & 4 Tools**: Once OAuth is working (Phase 2), basic tools (Phase 3) and management tools (Phase 4) can be developed by different developers simultaneously
3. **Phase 5 Features**: Docker setup, monitoring, and calendar integration can be developed in parallel once core functionality is complete

### Team Assignment Suggestions:

- **Developer A**: Phases 1 → 2 → 3 (Core functionality)
- **Developer B**: Documentation → Phase 4 → Phase 5 (Docker/Security)  
- **Developer C**: Phase 5 (Calendar integration) → Testing → Documentation

### Risk Mitigation:

1. **Dependency Risk**: Phase 2 (OAuth) must be complete before Phases 3-4
2. **Testing Risk**: Have one developer focus on comprehensive testing throughout
3. **Integration Risk**: Regular integration testing between parallel workstreams

---

## Success Criteria Summary

By the end of all phases, you should have:

1. ✅ A working Gmail MCP server that authenticates without Google Cloud projects
2. ✅ Complete email functionality (send, read, search, manage, attachments)
3. ✅ Production-ready application with monitoring and security
4. ✅ Docker deployment capability
5. ✅ Comprehensive documentation and testing
6. ✅ Optional calendar integration

Each phase should be fully tested and deployable before moving to the next phase.