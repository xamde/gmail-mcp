package com.example.gmailmcp.util;

import com.example.gmailmcp.model.LocalAttachment;
import com.example.gmailmcp.model.LocalEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmailConverter {

    public static LocalEmail toLocalEmail(Message message) throws MessagingException, IOException {
        String from = "";
        if (message.getFrom() != null && message.getFrom().length > 0) {
            from = ((InternetAddress) message.getFrom()[0]).getAddress();
        }
        String subject = message.getSubject();
        ZonedDateTime sentDate = message.getSentDate() != null
                ? message.getSentDate().toInstant().atZone(ZoneId.systemDefault())
                : null;
        String bodyText = getTextFromMessage(message);
        List<LocalAttachment> attachments = getAttachmentsFromMessage(message);
        // ID is not available directly, it will be set later
        return new LocalEmail(null, from, subject, bodyText, sentDate, attachments);
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            result = getTextFromMimeMultipart(multipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(Multipart multipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            Part bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
                break; // We only want the plain text part
            } else if (bodyPart.isMimeType("text/html")) {
                // Skip HTML part
            } else if (bodyPart.getContent() instanceof Multipart) {
                result.append(getTextFromMimeMultipart((Multipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private static List<LocalAttachment> getAttachmentsFromMessage(Message message) throws IOException, MessagingException {
        List<LocalAttachment> attachments = new ArrayList<>();
        if (message.getContent() instanceof Multipart) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Part bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String fileName = bodyPart.getFileName();
                    String contentType = bodyPart.getContentType();
                    byte[] content = IOUtils.toByteArray(bodyPart.getInputStream());
                    attachments.add(new LocalAttachment(fileName, contentType, content));
                }
            }
        }
        return attachments;
    }
}
