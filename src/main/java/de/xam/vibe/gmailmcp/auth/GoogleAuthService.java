package de.xam.vibe.gmailmcp.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String APPLICATION_NAME = "Gmail MCP Server";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);

    @Value("${google.credentials.file.path}")
    private String credentialsFilePath;

    @Value("${google.tokens.directory.path}")
    private String tokensDirectoryPath;

    @Value("${google.api.timeout}")
    private int timeout;

    private Credential getCredentials() throws IOException, GeneralSecurityException {
        log.info("Getting credentials...");
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        log.debug("Loading client secrets from: {}", credentialsFilePath);
        GoogleClientSecrets clientSecrets;
        try (java.io.FileInputStream fileInputStream = new java.io.FileInputStream(credentialsFilePath);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, inputStreamReader);
            log.debug("Client secrets loaded successfully.");
        } catch (IOException e) {
            log.error("Failed to load client secrets file: {}", credentialsFilePath, e);
            throw e;
        }

        log.debug("Building authorization code flow...");
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public Gmail getGmailClient() throws GeneralSecurityException, IOException {
        log.info("Getting Gmail client...");
        try {
            Credential credential = getCredentials();
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Gmail client = new Gmail.Builder(httpTransport, JSON_FACTORY, request -> {
                credential.initialize(request);
                request.setConnectTimeout(timeout);
                request.setReadTimeout(timeout);
            }).setApplicationName(APPLICATION_NAME).build();
            log.info("Gmail client created successfully.");
            return client;
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to create Gmail client", e);
            throw e;
        }
    }
}
