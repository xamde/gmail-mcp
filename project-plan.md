# Project Plan: A Production-Grade Gmail MCP Server in Java

**Note on Google Cloud Projects:** This project requires a single Google Cloud Project to be created by the development team. This is a standard requirement for accessing Google APIs and is necessary to obtain the credentials that this application will use to authenticate with Google's servers. **End-users of this application will NOT need to create their own Google Cloud Project.** They will simply go through a standard Google login and consent screen in their web browser to grant the application access to their Gmail account.

## Phase 1: Project Setup and Foundation

**Goal:** Establish a solid foundation for the project, including the basic project structure, dependencies, and a minimal working application.

**Deliverable:** A runnable Spring Boot application that starts up successfully.

### Steps:

1.  **Project Scaffolding & Dependency Setup**
    *   **Goal:** Create a new Spring Boot 3.x project using Maven.
    *   **Implementation Comments:**
        *   Use the Spring Initializr (start.spring.io) to generate the project with the following dependencies:
            *   `spring-boot-starter-web`: For web server capabilities.
            *   `com.google.apis:google-api-services-gmail`: The Gmail API client library.
            *   `com.google.oauth-client:google-oauth-client-jetty`: For the local OAuth 2.0 redirect server.
            *   `spring-boot-starter-actuator`: For health checks and monitoring.
    *   **How to Test:**
        *   **Input:** Run `mvn spring-boot:run` from the command line.
        *   **Expected Output:** The application should start without errors. You should be able to access the health check endpoint at `http://localhost:8080/actuator/health` and see the response `{"status":"UP"}`.

2.  **Basic Configuration**
    *   **Goal:** Create the initial `application.yml` file and define the basic configuration properties.
    *   **Implementation Comments:**
        *   Create a `src/main/resources/application.yml` file.
        *   Add a property for the server port: `server.port=8080`.
    *   **How to Test:**
        *   **Input:** Run the application.
        *   **Expected Output:** The application should start on port 8080 as specified in the configuration file.

## Phase 2: Gmail Authentication

**Goal:** Implement the complete OAuth 2.0 authentication flow to securely connect to the Gmail API.

**Deliverable:** An application that can obtain and persist an OAuth 2.0 refresh token from Google.

### Steps:

1.  **Google Cloud Project Setup Guide**
    *   **Goal:** Create a detailed guide for setting up the Google Cloud Project.
    *   **Implementation Comments:**
        *   Create a `SETUP_GUIDE.md` file.
        *   Follow the steps outlined in "Issue #2" of the original `README.md` to create the guide. This includes:
            *   Creating a Google Cloud Project.
            *   Enabling the Gmail API.
            *   Configuring the OAuth Consent Screen.
            *   Adding test users.
            *   Creating "OAuth 2.0 Client ID" credentials for a "Desktop app".
            *   Downloading the `client_secret.json` file.
    *   **How to Test:**
        *   **Input:** Have a team member (ideally someone not involved in writing the guide) follow the instructions.
        *   **Expected Output:** The team member should be able to successfully generate a `client_secret.json` file without needing assistance.

2.  **Core Authentication Service (GoogleAuthService)**
    *   **Goal:** Create a Spring service to manage the Google OAuth 2.0 flow.
    *   **Implementation Comments:**
        *   Create a `GoogleAuthService` class annotated with `@Service`.
        *   Implement the logic to:
            *   Load the `client_secret.json` file from a configurable file path.
            *   Use a `FileDataStoreFactory` to persist the user's refresh token in a configurable directory.
            *   Trigger the browser-based authorization flow on the first run.
            *   Provide a `getGmailClient()` method that returns an authenticated `com.google.api.services.gmail.Gmail` object.
    *   **How to Test:**
        *   **Input:** Run the application for the first time.
        *   **Expected Output:**
            1.  The application should log a URL to the console.
            2.  When you open the URL in a browser, you should be prompted to log in with your Google account and grant the application permission.
            3.  After granting permission, a `tokens.json` file should be created in the configured directory.
            4.  On subsequent runs, the application should authenticate silently without requiring user interaction.

## Phase 3: Core Email Functionality (Plain Java)

**Goal:** Implement the basic email operations as plain Java methods.

**Deliverable:** A set of Java methods that can send, read, and search emails using the authenticated Gmail client.

### Steps:

1.  **Implement `sendEmail`**
    *   **Goal:** Create a Java method to send emails.
    *   **Implementation Comments:**
        *   Create a `GmailService` class.
        *   Create a `sendEmail` method that takes `to`, `subject`, and `body` as string parameters.
        *   The method should use the `GoogleAuthService` to get an authenticated Gmail client.
        *   Construct and send a `MimeMessage`.
    *   **How to Test:**
        *   **Input:** Call the `sendEmail` method with valid parameters from a test class.
        *   **Expected Output:** The email should be successfully sent and received by the recipient.

2.  **Implement `readEmail` and `searchEmails`**
    *   **Goal:** Create Java methods to read and search for emails.
    *   **Implementation Comments:**
        *   Add `readEmail(String messageId)` and `searchEmails(String query)` methods to the `GmailService`.
        *   `readEmail` should return a structured object with the email's details.
        *   `searchEmails` should return a list of email summaries.
    *   **How to Test:**
        *   **`readEmail`:**
            *   **Input:** Call the `readEmail` method with the ID of a known email.
            *   **Expected Output:** The method should return the full content of the email.
        *   **`searchEmails`:**
            *   **Input:** Call the `searchEmails` method with a query like "from:test@example.com".
            *   **Expected Output:** The method should return a list of emails that match the query.

## Phase 4: MCP Server Setup

**Goal:** Introduce the MCP server dependency and set up the basic MCP server.

**Deliverable:** A running MCP server that can be connected to by an MCP client.

### Steps:

1.  **Add MCP Dependency**
    *   **Goal:** Add the `spring-ai-starter-mcp-server-webmvc` dependency to the project.
    *   **Implementation Comments:**
        *   Add the dependency to the `pom.xml` file.
    *   **How to Test:**
        *   **Input:** Run `mvn dependency:tree`.
        *   **Expected Output:** The MCP server dependency should be listed in the dependency tree. The application should still start successfully.

2.  **Create `GmailToolService`**
    *   **Goal:** Create the `GmailToolService` that will contain the MCP tools.
    *   **Implementation Comments:**
        *   Create a `GmailToolService` class annotated with `@Service`.
        *   Inject the `GmailService` from Phase 3.
    *   **How to Test:**
        *   **Input:** Run the application.
        *   **Expected Output:** The application should start successfully.

## Phase 5: Exposing Core Email Functionality as MCP Tools

**Goal:** Expose the core email functionality as MCP tools.

**Deliverable:** A running MCP server that can send, read, and search emails via MCP.

### Steps:

1.  **Implement `sendEmail` Tool**
    *   **Goal:** Create an MCP tool to send emails.
    *   **Implementation Comments:**
        *   Create a `sendEmail` method in `GmailToolService` annotated with `@Tool`.
        *   This method will call the `sendEmail` method from the `GmailService`.
    *   **How to Test:**
        *   **Input:** Use an MCP client to call the `sendEmail` tool with valid parameters.
        *   **Expected Output:** The email should be successfully sent and received by the recipient.

2.  **Implement `readEmail` and `searchEmails` Tools**
    *   **Goal:** Create MCP tools to read and search for emails.
    *   **Implementation Comments:**
        *   Create `readEmail(String messageId)` and `searchEmails(String query)` methods in `GmailToolService` annotated with `@Tool`.
        *   These methods will call the corresponding methods from the `GmailService`.
    *   **How to Test:**
        *   **`readEmail`:**
            *   **Input:** Call the `readEmail` tool with the ID of a known email.
            *   **Expected Output:** The tool should return the full content of the email.
        *   **`searchEmails`:**
            *   **Input:** Call the `searchEmails` tool with a query like "from:test@example.com".
            *   **Expected Output:** The tool should return a list of emails that match the query.

## Phase 6: Advanced Features, Packaging, Documentation, and Release

**Goal:** Add advanced features, containerize the application, create comprehensive documentation, and prepare for release.

**Deliverable:** A Dockerized, well-documented, and tested v1.0.0 release.

### Steps:

1.  **Attachment Handling**
    *   **Goal:** Add support for sending and downloading attachments.
    *   **Implementation Comments:**
        *   Enhance the `GmailService` and `GmailToolService` to support attachments.
    *   **How to Test:**
        *   **`sendEmail` with attachment:**
            *   **Input:** Call `sendEmail` with a valid file path in the `attachmentPaths` parameter.
            *   **Expected Output:** The email should be sent with the file attached.
        *   **`downloadAttachment`:**
            *   **Input:** Call `downloadAttachment` with a valid message ID, attachment ID, and save path.
            *   **Expected Output:** The attachment should be downloaded to the specified path.

2.  **Robust Configuration and Usability**
    *   **Goal:** Improve the server's configuration, logging, and overall user experience.
    *   **Implementation Comments:**
        *   Move all configurable values to `application.yml`.
        *   Implement structured logging for key events.
    *   **How to Test:**
        *   **Input:** Modify the configuration in `application.yml` (e.g., change the server port).
        *   **Expected Output:** The application should use the new configuration values. The logs should be clear and informative.

3.  **Containerization with Docker**
    *   **Goal:** Create a `Dockerfile` for easy deployment.
    *   **Implementation Comments:**
        *   Use a multi-stage build to create a small, secure final image.
    *   **How to Test:**
        *   **Input:** Build the Docker image and run it.
        *   **Expected Output:** The server should start and run correctly within the Docker container.

4.  **Finalize README and User Documentation**
    *   **Goal:** Create comprehensive documentation for the project.
    *   **Implementation Comments:**
        *   Update the `README.md` to be the single source of truth.
        *   Include a complete reference for all MCP tools.
    *   **How to Test:**
        *   **Input:** Have a new user try to set up and use the server using only the documentation.
        *   **Expected Output:** The user should be able to do so without assistance.

5.  **Testing, Release, and Community Engagement**
    *   **Goal:** Add a robust testing suite, perform end-to-end testing, and create the official v1.0.0 release.
    *   **Implementation Comments:**
        *   Implement unit and integration tests.
        *   Set up a CI/CD pipeline with GitHub Actions.
        *   Create a v1.0.0 release on GitHub.
    *   **How to Test:**
        *   **Input:** Run the test suite.
        *   **Expected Output:** All tests should pass. The CI/CD pipeline should successfully build and test the project on every push.
