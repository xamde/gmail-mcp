# History
It all started in a discussion with Gemini: 

"The research has identified a clear implementation gap: the absence of a mature, open-source Gmail MCP server in the Java ecosystem. However, it has also confirmed that all the necessary building blocks are readily available and well-supported. This section provides a technical blueprint for constructing such a server, combining the power of the official MCP Java SDK with Google's official Gmail API client library."

It when directly went ahead to create a "Blueprint for a Java-Based Gmail MCP Server" including "Architectural and Dependency Choices" etc.
I then expaned that plan into this document.

# **Project Plan: A Production-Grade Gmail MCP Server in Java**

**Document Purpose:** This document outlines the strategic rationale and a detailed, phased development plan for creating a robust, open-source Gmail MCP Server using the Java ecosystem. The primary technology stack will be the official MCP Java SDK accelerated by Spring AI and Spring Boot, establishing a benchmark for enterprise-grade AI tooling in Java.  
**Target Audience:** Development Team, Project Managers, and Architects.

## **1\. Rationale & Strategic Vision ("The Why")**

The Model Context Protocol (MCP) is rapidly becoming the industry standard for integrating AI agents with external tools, creating a new paradigm for automated workflows. Our research indicates a vibrant ecosystem of MCP servers for services like GitHub, Slack, and JIRA, primarily in Python and JavaScript. However, a significant gap exists in the enterprise Java ecosystem: **there is no mature, open-source, and production-ready Gmail MCP server.**  
This project directly addresses that gap. By building this server, we will unlock significant value:

* **Enable Enterprise Java AI:** Provide a critical, missing piece of infrastructure for the vast number of enterprises that rely on Java and the Spring Framework. This allows them to build powerful, next-generation AI agents that can securely interact with a core business communication tool. Use cases range from AI-powered executive assistants that can summarize and triage inboxes to automated systems that process incoming customer support requests or financial invoices directly from Gmail.  
* **Establish a Reference Architecture:** Create a definitive, best-practice example of how to build high-quality, secure, and maintainable MCP servers using Spring AI. This architecture will demonstrate key enterprise patterns, including secure OAuth 2.0 token management, clean separation of concerns between authentication and tool logic, robust error handling, and comprehensive configuration management.  
* **Promote the Java MCP Ecosystem:** Contribute a high-value, reusable asset to the open-source community. This will not only provide immediate utility but also serve as a catalyst, encouraging further development and adoption of MCP within the Java world. We will actively promote this work through blog posts and community engagement to build momentum.

Technology Choice: Spring AI & Spring Boot  
We will use the official MCP Java SDK, but its implementation will be accelerated and fortified using Spring AI and Spring Boot. This strategic choice offers significant, compounding advantages over a manual implementation:

* **Development Speed & Simplicity:** Spring AI's @Tool annotation abstracts away the complexities of MCP's tool definition and discovery protocol. This declarative approach dramatically reduces boilerplate code, allowing developers to focus on business logic rather than protocol mechanics.  
* **Enterprise-Ready Foundation:** Aligns with the dominant framework in enterprise Java, ensuring the final product is robust, secure, maintainable, and easily integrated into existing corporate environments. We gain production-grade features like health checks and metrics via the Spring Boot Actuator out-of-the-box.  
* **Clean Dependency Injection:** Leverages Spring's core Inversion of Control (IoC) container to cleanly manage the lifecycle and dependencies of our components, such as the Google API clients, authentication services, and tool services. This leads to more modular, testable, and maintainable code.

## **2\. Phased Development Plan & Work Breakdown**

The project is broken down into four logical phases, each with a set of actionable issues designed to be completed in sequence.

### **Phase 1: Foundation & Authentication**

**Goal:** Establish a working, secure project skeleton that can successfully authenticate with the Google Gmail API via the OAuth 2.0 protocol for installed applications.

* **Issue \#1: Project Scaffolding & Dependency Setup**  
  * **Task:** Create a new Spring Boot 3.x project using Maven or Gradle.  
  * **Dependencies:**  
    * spring-boot-starter-web: Core web server capabilities.  
    * spring-ai-starter-mcp-server-webmvc: The official Spring AI starter for building MCP servers over HTTP/SSE.  
    * com.google.apis:google-api-services-gmail: The type-safe Google Gmail API client library.  
    * com.google.oauth-client:google-oauth-client-jetty: Provides a local, embedded server to handle the OAuth 2.0 redirect, which is essential for the "installed app" flow.  
    * spring-boot-starter-actuator: To expose production-ready health and info endpoints.  
  * **Acceptance Criteria:** The project builds successfully. A basic /actuator/health endpoint returns {"status":"UP"}, and the MCP server logs its startup on a configurable port.  
* **Issue \#2: Google Cloud Project Setup & Documentation**  
  * **Task:** Create a detailed, step-by-step guide in the README.md for users to configure their own Google Cloud Project. This is a critical onboarding step.  
  * **Details to Cover:**  
    1. Creating a new Google Cloud Project.  
    2. Navigating to "APIs & Services" \-\> "Enabled APIs & services" and enabling the "Gmail API" and "Google Calendar API" (for the stretch goal).  
    3. Configuring the OAuth Consent Screen: specifying the app name, user support email, and crucially, defining the required API scopes (e.g., https://www.googleapis.com/auth/gmail.modify, https://www.googleapis.com/auth/calendar.events).  
    4. Adding their own Google account to the "Test Users" list while the app is in testing mode.  
    5. Creating "OAuth 2.0 Client ID" credentials for a "Desktop app".  
    6. Downloading the client\_secret.json file and explaining where to place it.  
  * **Acceptance Criteria:** A non-technical user can follow the guide from start to finish and successfully generate a valid client\_secret.json file configured with the correct scopes.  
* **Issue \#3: Core Authentication Service (GoogleAuthService)**  
  * **Task:** Develop a Spring @Service bean to encapsulate and manage the entire Google OAuth 2.0 flow. This service is the security backbone of the application.  
  * **Responsibilities:**  
    * Load the client\_secret.json from a file path specified in application.yml. The service must handle errors gracefully if the file is not found or is malformed.  
    * Manage a FileDataStoreFactory to securely persist the user's refresh token. The storage directory must also be configurable via application.yml to allow users to place it in a secure, non-version-controlled location.  
    * Implement the logic to trigger the browser-based authorization flow on the first run. The service should log a clear URL for the user to copy/paste if a GUI is not available.  
    * Provide a central, thread-safe method, getGmailClient(), that returns a fully authenticated com.google.api.services.gmail.Gmail service object for use by the tool services.  
  * **Acceptance Criteria:** On first run, the application prompts for browser login. After consent, a tokens.json file is created in the configured directory. On all subsequent runs, the app authenticates silently and successfully without user interaction.

### **Phase 2: Core Email Functionality (MCP Tools)**

**Goal:** Implement the essential email operations as discoverable, callable, and well-described MCP Tools.

* **Issue \#4: Implement GmailToolService and sendEmail Tool**  
  * **Task:** Create a new @Service named GmailToolService that will contain all Gmail-related tools. Implement the first and most critical tool: sendEmail.  
  * **Annotation:** @Tool(description \= "Sends a new email. The body can be plain text. Requires a recipient email address, a subject line, and the main body content.")  
  * **Logic:** The method should accept to (String), subject (String), and body (String) as parameters. It will use the GoogleAuthService to get an authenticated Gmail client. The implementation must correctly create a MimeMessage, set the headers, set the text content, and then Base64URL encode it before calling the users.messages.send API.  
  * **Acceptance Criteria:** An MCP client can connect, discover the sendEmail tool with its description, and successfully invoke it to send a plain-text email that is received in the recipient's inbox.  
* **Issue \#5: Implement Read & Search Tools**  
  * **Task:** Add readEmail and searchEmails tools to the GmailToolService to enable information retrieval.  
  * **readEmail(String messageId):** Fetches the full content of a single email by its ID. The return value must be a structured Map or a custom DTO containing from, to, subject, snippet, date, and both bodyText and bodyHtml parts to give the AI model maximum context.  
  * **searchEmails(String query):** Exposes the powerful Gmail search API. It should return a list of email summaries (ID, sender, subject, snippet). The query parameter documentation should include examples like "from:boss@company.com is:unread" to showcase its utility. The implementation should handle pagination and limit results to a reasonable number (e.g., 25\) by default.  
  * **Acceptance Criteria:** An AI agent can be prompted to "find the last 5 emails from my boss" and then "read the full content of email with ID xyz".  
* **Issue \#6: Implement Email Management Tools**  
  * **Task:** Add tools for common email management tasks to allow agents to perform cleanup and organizational actions.  
  * **Tools to Implement:**  
    * trashEmail(String messageId): Moves an email to the trash.  
    * deleteEmail(String messageId): Permanently deletes an email (use with caution).  
    * markAsRead(String messageId)  
    * markAsUnread(String messageId)  
  * **Logic:** Each tool should return a boolean true on success to provide clear feedback to the calling agent.  
  * **Acceptance Criteria:** An agent can perform cleanup tasks like "trash the spam email I just received" or "mark the newsletter email as read".

### **Phase 3: Advanced Features & Refinements**

**Goal:** Enhance the server with high-value features that demonstrate complex interactions and improve robustness.

* **Issue \#7: Attachment Handling**  
  * **Task:** Upgrade the server to be a first-class citizen in handling file attachments.  
  * **Enhance sendEmail:** Add an optional parameter, List\<String\> attachmentPaths, for a list of local file paths to attach. The implementation must handle various MIME types.  
  * **Enhance readEmail:** The return object must be updated to include a list of attachments, where each item contains the attachmentId, filename, and sizeInBytes.  
  * **New Tool downloadAttachment(String messageId, String attachmentId, String savePath):** Downloads a specific attachment to a local file path. The savePath must be validated to prevent directory traversal attacks.  
  * **Acceptance Criteria:** An agent can "send an email to finance with the attached /invoices/inv-001.pdf" and "download the presentation from the last email from marketing to /downloads/".  
* **Issue \#8: (Optional Stretch Goal) Calendar Integration**  
  * **Task:** Create a new GoogleCalendarToolService to demonstrate powerful cross-service capabilities.  
  * **New Tool createCalendarEvent(String summary, String startTime, String endTime, String description, List\<String\> attendees):** Creates a new event in the user's primary calendar, using RFC3339 format for timestamps (e.g., "2025-07-21T10:00:00-07:00").  
  * **Rationale:** This demonstrates the true power of AI agents that can reason across different tools and data sources.  
  * **Acceptance Criteria:** An agent can execute a complex command like "read the email about the project kickoff, find a 1-hour slot we're both free next week, and schedule a meeting with the summary in the description, inviting teammate@company.com".  
* **Issue \#9: Robust Configuration & Usability**  
  * **Task:** Refactor to improve the server's configuration, logging, and overall user experience.  
  * **Logic:** Move all configurable values (server port, paths for credentials/tokens, API scopes, API timeouts, max search results) to application.yml with sensible, documented defaults.  
  * **Logging:** Implement clear and helpful structured logging. Log successful authentications, tool invocations with parameters (scrubbing sensitive data like email bodies), and especially detailed errors from the Google API, including their specific error codes and messages to aid in debugging.  
  * **Acceptance Criteria:** A user can fully configure and understand the server's behavior by editing a single, well-documented application.yml file. Logs are clear and actionable.

### **Phase 4: Packaging, Documentation & Release**

**Goal:** Prepare the server for public consumption, ensuring it is easy to deploy, use, and understand.

* **Issue \#10: Containerization with Docker**  
  * **Task:** Create a Dockerfile and a .dockerignore file for easy deployment.  
  * **Logic:** The Dockerfile must use a multi-stage build. The first stage uses a full JDK image (e.g., maven:3.9-eclipse-temurin-21) to build the application JAR. The final stage copies this JAR into a minimal JRE image (e.g., eclipse-temurin:21-jre-jammy) to create a small, secure final container. The Dockerfile should allow mounting volumes for the configuration directory and the tokens directory.  
  * **Acceptance Criteria:** A user can run the fully configured server with a single, well-documented docker run command.  
* **Issue \#11: Finalize README and User Documentation**  
  * **Task:** Perform a full review and significant expansion of the README.md to make it the single source of truth.  
  * **Content to Include:**  
    * Project overview, value proposition, and a GIF demonstrating it in action.  
    * The full, polished Google Cloud setup guide (from Issue \#2).  
    * Detailed configuration instructions for application.yml.  
    * A complete reference for every available MCP tool, including its name, description, parameters, and an example return value.  
    * Clear examples of how to run the server (locally via java \-jar and via Docker).  
    * A copy-pasteable JSON snippet for configuring an MCP client like Claude Desktop to connect to the server.  
  * **Acceptance Criteria:** The documentation is complete, professional, and enables a new user to get the server running successfully without any external assistance.  
* **Issue \#12: Testing, Release, and Community Engagement**  
  * **Task:** Add a robust testing suite, perform end-to-end testing, and create the official v1.0.0 release.  
  * **Testing:** Implement unit tests for tool services by mocking the GoogleAuthService. Create a separate integration test suite (e.g., using a @TestProfile) that can run against the real Google APIs if a user provides credentials.  
  * **CI/CD:** Create a GitHub Actions workflow that automatically builds the project and runs the unit tests on every push.  
  * **Release:** Perform final end-to-end testing with at least two different MCP Host applications (e.g., Claude Desktop, Cursor). Create a v1.0.0 release tag on GitHub with compiled binaries.  
  * **Acceptance Criteria:** The server is published, announced, and the project is ready for community use and contributions.


# State
To fully realize the vision of the README, the following items are still missing:

Robust Configuration & Usability: While basic configuration is in place, the server could be enhanced with more advanced configuration options, such as API timeouts and more detailed logging, as described in Issue #9.
Containerization with Docker: A Dockerfile for easy deployment, as described in Issue #10.

Finalized README and User Documentation: A comprehensive README with detailed instructions and examples, as described in Issue #11.

Testing, Release, and Community Engagement: A full testing suite, including integration tests, and a proper release process, as described in Issue #12.

Additionally, the optional stretch goal of Calendar Integration (Issue #8) has not been implemented.
