package com.example.gmailmcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.api")
public class GoogleApiConfig {

    // TODO: Move all configurable values to application.yml with sensible, documented defaults.
    // This includes server port, paths for credentials/tokens, API scopes, API timeouts, and max search results.

    // TODO: Implement clear and helpful structured logging.
    // Log successful authentications, tool invocations with parameters (scrubbing sensitive data),
    // and detailed errors from the Google API.

    private String credentialsPath;
    private String tokensDirectory;

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    public String getTokensDirectory() {
        return tokensDirectory;
    }

    public void setTokensDirectory(String tokensDirectory) {
        this.tokensDirectory = tokensDirectory;
    }
}
