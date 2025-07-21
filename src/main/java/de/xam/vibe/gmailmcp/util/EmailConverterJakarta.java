package de.xam.vibe.gmailmcp.util;

import de.xam.vibe.gmailmcp.model.LocalAttachment;
import de.xam.vibe.gmailmcp.model.LocalEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to convert Jakarta Mail {@link jakarta.mail.Message} objects to
 * {@link de.xam.vibe.gmailmcp.model.LocalEmail} objects. This class is responsible for extracting the relevant
 * information from a `Message` and creating a `LocalEmail` object.
 *
 * @see de.xam.vibe.gmailmcp.model.LocalEmail
 * @see de.xam.vibe.gmailmcp.model.LocalAttachment
 */
public class EmailConverterJakarta {

    private static final Logger log = LoggerFactory.getLogger(EmailConverterJakarta.class);

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

    public static LocalEmail toLocalEmail(Message jakartaMessage) throws MessagingException, IOException {
        log.debug("Converting message to LocalEmail...");
        String from = "";
        if (jakartaMessage.getFrom() != null && jakartaMessage.getFrom().length > 0) {
            from = ((InternetAddress) jakartaMessage.getFrom()[0]).getAddress();
        }
        String subject = jakartaMessage.getSubject();
        ZonedDateTime sentDate = jakartaMessage.getSentDate().toInstant().atZone(ZoneId.systemDefault());
        String bodyText = getTextFromMessage(jakartaMessage);
        List<LocalAttachment> attachments = getAttachmentsFromMessage(jakartaMessage);
        // ID is not available directly, it will be set later
        LocalEmail localEmail = new LocalEmail(null, from, subject, bodyText, sentDate, attachments);
        log.debug("Message converted to LocalEmail successfully.");
        return localEmail;
    }

}
