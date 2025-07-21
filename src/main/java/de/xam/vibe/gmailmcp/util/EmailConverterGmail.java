package de.xam.vibe.gmailmcp.util;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import de.xam.vibe.gmailmcp.model.LocalAttachment;
import de.xam.vibe.gmailmcp.model.LocalEmail;
import de.xam.vibe.gmailmcp.service.GmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class EmailConverterGmail {

    private static final Logger log = LoggerFactory.getLogger(EmailConverterGmail.class);

    private static void extractParts(List<MessagePart> parts, String[] bodyTextHolder, List<LocalAttachment> attachments) {
        if (parts == null) return;
        for (MessagePart part : parts) {
            if (part.getParts() != null && !part.getParts().isEmpty()) {
                extractParts(part.getParts(), bodyTextHolder, attachments);
            } else {
                MessagePartBody body = part.getBody();
                // Prefer text/plain, but if not found, use text/html as fallback
                if (body != null && body.getData() != null) {
                    if (bodyTextHolder[0].isEmpty() && "text/plain".equals(part.getMimeType())) {
                        bodyTextHolder[0] = decodeBase64Url(body.getData());
                    } else if (bodyTextHolder[0].isEmpty() && "text/html".equals(part.getMimeType())) {
                        bodyTextHolder[0] = decodeBase64Url(body.getData());
                    }
                }
                if (part.getFilename() != null && !part.getFilename().isEmpty() && body != null) {
                    byte[] content = null;
                    if (body.getData() != null) {
                        content = Base64.getUrlDecoder().decode(
                            body.getData() + "===".substring(0, (4 - body.getData().length() % 4) % 4)
                        );
                    }
                    attachments.add(new LocalAttachment(part.getFilename(), part.getMimeType(), content));
                }
            }
        }
    }

    private static String decodeBase64Url(String data) {
        // Gmail API uses base64url encoding, which may omit padding
        int padding = (4 - data.length() % 4) % 4;
        StringBuilder sb = new StringBuilder(data);
        for (int i = 0; i < padding; i++) sb.append('=');
        return new String(Base64.getUrlDecoder().decode(sb.toString()));
    }

    public static LocalEmail toLocalEmail(Message gmailMessage) {
        log.debug("Converting Gmail message to LocalEmail...");
        String id = gmailMessage.getId();
        MessagePart payload = gmailMessage.getPayload();
        if (payload == null) {
            // Defensive: If payload is null, return minimal LocalEmail
            return new LocalEmail(id, "", "", "", ZonedDateTime.now(), new ArrayList<>());
        }
        List<MessagePartHeader> headers = payload.getHeaders() != null ? payload.getHeaders() : new ArrayList<>();
        String from = headers.stream().filter(header -> header.getName().equalsIgnoreCase("From")).map(MessagePartHeader::getValue).findFirst().orElse("");
        String subject = headers.stream().filter(header -> header.getName().equalsIgnoreCase("Subject")).map(MessagePartHeader::getValue).findFirst().orElse("");

        ZonedDateTime sentDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(gmailMessage.getInternalDate() != null ? gmailMessage.getInternalDate() : System.currentTimeMillis()), ZoneId.systemDefault());

        String[] bodyTextHolder = new String[]{""};
        List<LocalAttachment> attachments = new ArrayList<>();

        // Always check for a body in the payload itself, even if there are parts
        if (payload.getBody() != null && payload.getBody().getData() != null) {
            bodyTextHolder[0] = decodeBase64Url(payload.getBody().getData());
        }
        // Then check for parts (which may override the above if text/plain is found)
        if (payload.getParts() != null && !payload.getParts().isEmpty()) {
            extractParts(payload.getParts(), bodyTextHolder, attachments);
        }

        log.debug("Gmail message converted to LocalEmail successfully.");
        return new LocalEmail(id, from, subject, bodyTextHolder[0], sentDate, attachments);
    }

    public static LocalEmail toLocalEmailWithAttachments(Message gmailMessage, GmailService gmailService) {
        Message gmailMessageWithAttachments = gmailService.fetchContentAndAttachmentsFor(gmailMessage);
        return toLocalEmail(gmailMessageWithAttachments);
    }

}
