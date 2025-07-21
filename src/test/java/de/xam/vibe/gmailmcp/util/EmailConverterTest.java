package de.xam.vibe.gmailmcp.util;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailConverterTest {

    @Test
    public void testToLocalEmail_ConvertsSimpleMessage() throws MessagingException, IOException {
        Message message = mock(MimeMessage.class);
        when(message.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress("test@example.com")});
        when(message.getSubject()).thenReturn("Test Subject");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.isMimeType("text/plain")).thenReturn(true);
        when(message.getContent()).thenReturn("Test Body");

        LocalEmail localEmail = EmailConverter.toLocalEmail(message);

        assertEquals("test@example.com", localEmail.getFrom());
        assertEquals("Test Subject", localEmail.getSubject());
        assertEquals("Test Body", localEmail.getBodyText());
        assertTrue(localEmail.getAttachments().isEmpty());
    }

    @Test
    public void testToLocalEmail_ExtractsAttachments() throws MessagingException, IOException {
        Message message = mock(MimeMessage.class);
        when(message.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress("test@example.com")});
        when(message.getSubject()).thenReturn("Test Subject");
        when(message.getSentDate()).thenReturn(new Date());

        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Test Body");
        multipart.addBodyPart(textPart);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setFileName("test.txt");
        attachmentPart.setContent("test content", "text/plain");
        attachmentPart.setDisposition(Part.ATTACHMENT);
        multipart.addBodyPart(attachmentPart);

        when(message.isMimeType("multipart/*")).thenReturn(true);
        when(message.getContent()).thenReturn(multipart);

        LocalEmail localEmail = EmailConverter.toLocalEmail(message);

        assertEquals("test@example.com", localEmail.getFrom());
        assertEquals("Test Subject", localEmail.getSubject());
        assertEquals("Test Body", localEmail.getBodyText());
        assertFalse(localEmail.getAttachments().isEmpty());
        assertEquals(1, localEmail.getAttachments().size());
        assertEquals("test.txt", localEmail.getAttachments().get(0).filename());
    }
}
