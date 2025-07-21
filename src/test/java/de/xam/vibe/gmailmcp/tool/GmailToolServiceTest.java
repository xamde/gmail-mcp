package de.xam.vibe.gmailmcp.tool;

import de.xam.vibe.gmailmcp.exception.GmailToolException;
import de.xam.vibe.gmailmcp.service.GmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GmailToolServiceTest {

    @Mock
    private GmailService gmailService;

    private GmailToolService gmailToolService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gmailToolService = new GmailToolService(gmailService);
    }

    @Test
    void sendEmail_shouldReturnSuccessMessage() throws GeneralSecurityException, IOException, jakarta.mail.MessagingException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        String result = gmailToolService.sendEmail(to, subject, body, null);

        assertEquals("Email sent successfully", result);
        verify(gmailService).sendEmail(to, subject, body, null);
    }

    @Test
    void sendEmail_shouldThrowGmailToolException_whenServiceFails() throws GeneralSecurityException, IOException, jakarta.mail.MessagingException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new GeneralSecurityException("Test Exception")).when(gmailService).sendEmail(to, subject, body, null);

        assertThrows(GmailToolException.class, () -> gmailToolService.sendEmail(to, subject, body, null));
    }

    @Test
    void downloadAttachment_shouldDownloadFile() throws GeneralSecurityException, IOException {
        String messageId = "testMessageId";
        String attachmentId = "testAttachmentId";
        Path savePath = tempDir.resolve("test.txt");
        byte[] attachmentBytes = "Test Data".getBytes();

        when(gmailService.getAttachment(messageId, attachmentId)).thenReturn(attachmentBytes);

        String result = gmailToolService.downloadAttachment(messageId, attachmentId, savePath.toString());

        assertEquals("Attachment downloaded successfully to " + savePath, result);
        assertArrayEquals(attachmentBytes, Files.readAllBytes(savePath));
    }

    @Test
    void downloadAttachment_shouldThrowException_forInvalidPath() {
        String messageId = "testMessageId";
        String attachmentId = "testAttachmentId";
        String savePath = "../test.txt";

        assertThrows(IllegalArgumentException.class, () -> gmailToolService.downloadAttachment(messageId, attachmentId, savePath));
    }
}
