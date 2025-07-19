# Google Cloud Project Setup Guide

This guide will walk you through the process of setting up a Google Cloud Project, enabling the Gmail API, and creating the necessary credentials to run this application.

## 1. Create a Google Cloud Project

1.  Go to the [Google Cloud Console](https://console.cloud.google.com/).
2.  Click the project drop-down menu at the top of the page and click **New Project**.
3.  Enter a project name (e.g., "Gmail MCP Server") and click **Create**.

## 2. Enable the Gmail API

1.  In the Google Cloud Console, go to the **APIs & Services** > **Enabled APIs & services** page.
2.  Click **+ ENABLE APIS AND SERVICES**.
3.  Search for "Gmail API" and select it from the search results.
4.  Click the **Enable** button.

## 3. Configure the OAuth Consent Screen

1.  In the Google Cloud Console, go to the **APIs & Services** > **OAuth consent screen** page.
2.  Choose the **External** user type and click **Create**.
3.  Fill in the required information:
    *   **App name:** A descriptive name for your application (e.g., "Gmail MCP Server").
    *   **User support email:** Your email address.
    *   **Developer contact information:** Your email address.
4.  Click **Save and Continue**.
5.  On the **Scopes** page, click **Add or Remove Scopes**.
6.  In the "Manually add scopes" field, add the following scope:
    *   `https://www.googleapis.com/auth/gmail.modify`
7.  Click **Add to table**, then click **Update**.
8.  Click **Save and Continue**.
9.  On the **Test users** page, click **+ ADD USERS**.
10. Enter the email addresses of the users who will be testing the application (e.g., your own Gmail address).
11. Click **Add**, then click **Save and Continue**.

## 4. Create OAuth 2.0 Client ID Credentials

1.  In the Google Cloud Console, go to the **APIs & Services** > **Credentials** page.
2.  Click **+ CREATE CREDENTIALS** and select **OAuth client ID**.
3.  Select **Desktop app** from the **Application type** drop-down menu.
4.  Enter a name for your client ID (e.g., "Gmail MCP Server Client ID").
5.  Click **Create**.
6.  A dialog box will appear with your client ID and client secret. Click **DOWNLOAD JSON** to download the `client_secret.json` file.

## 5. Place the `client_secret.json` File

Place the downloaded `client_secret.json` file in a secure location on your computer. You will need to provide the path to this file in the `application.yml` configuration file.
