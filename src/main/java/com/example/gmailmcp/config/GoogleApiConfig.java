package com.example.gmailmcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.api")
public class GoogleApiConfig {

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
