package de.xam.vibe.gmailmcp.util;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import de.xam.vibe.gmailmcp.model.LocalAttachment;
import de.xam.vibe.gmailmcp.model.LocalEmail;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static de.xam.vibe.gmailmcp.util.EmlToGmailConverter.createMessageFromEml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EmailConverterGmailTest {

    @Test
    public void testToLocalEmail_withTextHtmlAndPdfAttachment() {
        // Setup headers
        List<MessagePartHeader> headers = new ArrayList<>();
        headers.add(new MessagePartHeader().setName("From").setValue("sender@example.com"));
        headers.add(new MessagePartHeader().setName("Subject").setValue("Test Subject"));

        // text/plain part
        MessagePartBody textBody = new MessagePartBody().setData(Base64.getUrlEncoder().encodeToString("This is the plain text body.".getBytes(StandardCharsets.UTF_8)));
        MessagePart textPart = new MessagePart().setMimeType("text/plain").setBody(textBody);

        // text/html part
        MessagePartBody htmlBody = new MessagePartBody().setData(Base64.getUrlEncoder().encodeToString("<b>This is HTML</b>".getBytes(StandardCharsets.UTF_8)));
        MessagePart htmlPart = new MessagePart().setMimeType("text/html").setBody(htmlBody);

        // pdf attachment part
        MessagePartBody pdfBody = new MessagePartBody().setAttachmentId("att123");
        MessagePart pdfPart = new MessagePart().setFilename("file.pdf").setMimeType("application/pdf").setBody(pdfBody);

        // Combine parts
        List<MessagePart> parts = new ArrayList<>();
        parts.add(textPart);
        parts.add(htmlPart);
        parts.add(pdfPart);

        MessagePart payload = new MessagePart().setHeaders(headers).setParts(parts);

        Message gmailMessage = new Message().setId("msgid123").setPayload(payload).setInternalDate(System.currentTimeMillis());

        // Act
        LocalEmail localEmail = EmailConverterGmail.toLocalEmail(gmailMessage);

        // Assert
        assertEquals("msgid123", localEmail.getId());
        assertEquals("sender@example.com", localEmail.getFrom());
        assertEquals("Test Subject", localEmail.getSubject());
        assertEquals("This is the plain text body.", localEmail.getBodyText());
        assertEquals(1, localEmail.getAttachments().size());
        LocalAttachment attachment = localEmail.getAttachments().getFirst();
        assertEquals("file.pdf", attachment.filename());
        assertEquals("application/pdf", attachment.contentType());
        assertNull(attachment.content()); // Content is not fetched in this method
    }

    @Test
    void test() throws Exception {
        Message message = createMessageFromEml("./src/test/resources/test.eml");

        // test saveEmail in repo with this message
        LocalEmail localEmail = EmailConverterGmail.toLocalEmail(message);
        assertEquals("Test Email with Attachment", localEmail.getSubject());
        assertEquals(1, localEmail.getAttachments().size());
        assertEquals("sample.pdf", localEmail.getAttachments().getFirst().filename());

        // now try to save mail

    }

}

