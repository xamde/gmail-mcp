## Gmail MCP Server: Usage Guide

This guide will walk you through the process of setting up and running the Gmail MCP Server, and provide an overview of its capabilities.

### Prerequisites

Before you begin, ensure you have the following installed:

*   Java 21 or later
*   Maven 3.8 or later

### Setup

1.  **Clone the Repository:**

    ```bash
    git clone https://github.com/your-username/gmail-mcp-server.git
    cd gmail-mcp-server
    ```

2.  **Configure Google Cloud Project:**

    To use the Gmail API, you'll need to set up a project in the Google Cloud Console and obtain OAuth 2.0 client credentials. Follow these steps:

    1.  Go to the [Google Cloud Console](https://console.cloud.google.com/) and create a new project.
    2.  Navigate to **APIs & Services > Enabled APIs & services** and enable the **Gmail API**.
    3.  Go to **APIs & Services > OAuth consent screen**. Choose **External** and create a new consent screen. You'll need to provide an app name, user support email, and developer contact information.
    4.  In the **Scopes** section, add the `https://www.googleapis.com/auth/gmail.modify` scope.
    5.  Add your Google account to the **Test users** list.
    6.  Go to **APIs & Services > Credentials** and create a new **OAuth 2.0 Client ID**. Select **Desktop app** as the application type.
    7.  Download the `client_secret.json` file.

3.  **Configure the Application:**

    1.  Move the downloaded `client_secret.json` file to a secure location on your machine.
    2.  Open the `src/main/resources/application.yml` file and update the following properties:

        ```yaml
        google:
          api:
            credentials-path: /path/to/your/client_secret.json
            tokens-directory: /path/to/your/tokens
        ```

        Replace `/path/to/your/client_secret.json` with the actual path to your downloaded credentials file, and `/path/to/your/tokens` with a directory where you want to store the OAuth 2.0 refresh token.

### Running the Server

1.  **Build the Project:**

    ```bash
    mvn clean install
    ```

2.  **Run the Application:**

    ```bash
    mvn spring-boot:run
    ```

    The first time you run the application, it will open a browser window and prompt you to authorize the application to access your Gmail account. After you grant access, a `tokens.json` file will be created in the directory you specified in `application.yml`.

### Capabilities

The Gmail MCP Server provides the following tools, which can be used by AI agents to interact with your Gmail account:

*   **`sendEmail`**: Sends a new email.
*   **`readEmail`**: Reads the full content of a single email.
*   **`searchEmails`**: Searches for emails using a Gmail query.
*   **`trashEmail`**: Moves an email to the trash.
*   **`deleteEmail`**: Permanently deletes an email.
*   **`markAsRead`**: Marks an email as read.
*   **`markAsUnread`**: Marks an email as unread.
*   **`downloadAttachment`**: Downloads an attachment from an email to a local file path.

These tools can be discovered and invoked by any MCP-compatible client.
