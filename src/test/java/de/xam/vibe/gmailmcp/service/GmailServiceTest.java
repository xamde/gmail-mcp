package de.xam.vibe.gmailmcp.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import de.xam.vibe.gmailmcp.auth.GoogleAuthService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GmailServiceTest {

    @Mock
    private GoogleAuthService googleAuthService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Gmail gmail;

    private GmailService gmailService;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        MockitoAnnotations.openMocks(this);
        when(googleAuthService.getGmailClient()).thenReturn(gmail);
        gmailService = new GmailService(googleAuthService, 10);
    }

    @Test
    void sendEmail() throws Exception {
        // Prepare
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        when(gmail.users().messages().send(anyString(), any(Message.class)).execute()).thenReturn(new Message());

        // Act
        gmailService.sendEmail(to, subject, body, null);

        // Assert
        verify(gmail.users().messages()).send(eq("me"), any(Message.class));
    }
}
