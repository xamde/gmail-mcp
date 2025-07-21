package de.xam.vibe.gmailmcp.util;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.common.io.BaseEncoding;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class EmlToGmailConverter {

    /**
     * Creates a Gmail API Message object from an .eml file.
     *
     * @param emlFilePath Path to the .eml file.
     * @return A Gmail Message object ready to be sent or imported.
     * @throws Exception if parsing or encoding fails.
     */
    public static Message createMessageFromEml(String emlFilePath) throws Exception {
        // 1. Parse the .eml file using Jakarta Mail
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage mimeMessage;
        try (InputStream source = new FileInputStream(emlFilePath)) {
            mimeMessage = new MimeMessage(session, source);
        }

        // 2. Write the raw MimeMessage content to a byte array
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();

        // 3. Encode the raw bytes into Base64URL format
        String encodedEmail = BaseEncoding.base64().encode(rawMessageBytes);

        // 4. Create the Gmail API Message object
        Message gmailMessage = new Message();
        gmailMessage.setRaw(encodedEmail);

        // --- Populate payload and headers for programmatic access ---
        MessagePart payload = new MessagePart();
        java.util.List<MessagePartHeader> headers = new java.util.ArrayList<>();
        String[] headerNames = {"Subject", "From", "To", "Date"};
        for (String headerName : headerNames) {
            String value = mimeMessage.getHeader(headerName, null);
            if (value != null) {
                // Decode RFC 2047 encoded headers (e.g., =?utf-8?q?...?=)
                try {
                    value = jakarta.mail.internet.MimeUtility.decodeText(value);
                } catch (Exception e) {
                    // fallback to raw value
                }
                headers.add(new MessagePartHeader().setName(headerName).setValue(value));
            }
        }
        payload.setHeaders(headers);

        // --- Extract attachments and body parts ---
        java.util.List<MessagePart> parts = new java.util.ArrayList<>();
        if (mimeMessage.isMimeType("multipart/*")) {
            jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) mimeMessage.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                jakarta.mail.BodyPart bp = multipart.getBodyPart(i);
                MessagePart part = new MessagePart();
                part.setMimeType(bp.getContentType().split(";")[0]);
                String filename = bp.getFileName();
                if (filename != null) {
                    // Decode filename if encoded
                    try {
                        filename = jakarta.mail.internet.MimeUtility.decodeText(filename);
                    } catch (Exception e) {
                    }
                    part.setFilename(filename);
                }
                // Set headers for the part
                java.util.List<MessagePartHeader> partHeaders = new java.util.ArrayList<>();
                java.util.Enumeration<?> allHeaders = bp.getAllHeaders();
                while (allHeaders.hasMoreElements()) {
                    jakarta.mail.Header h = (jakarta.mail.Header) allHeaders.nextElement();
                    String v = h.getValue();
                    try {
                        v = jakarta.mail.internet.MimeUtility.decodeText(v);
                    } catch (Exception e) {
                    }
                    partHeaders.add(new MessagePartHeader().setName(h.getName()).setValue(v));
                }
                part.setHeaders(partHeaders);
                // For attachments, set the actual content as base64url data
                if (filename != null) {
                    java.io.InputStream is = bp.getInputStream();
                    byte[] bytes = is.readAllBytes();
                    String encoded = BaseEncoding.base64Url().encode(bytes);
                    part.setBody(new com.google.api.services.gmail.model.MessagePartBody().setData(encoded));
                } else if (bp.getContent() instanceof String) {
                    // For text parts, set the body data
                    String text = (String) bp.getContent();
                    String encoded = BaseEncoding.base64Url().encode(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    part.setBody(new com.google.api.services.gmail.model.MessagePartBody().setData(encoded));
                }
                parts.add(part);
            }
        }
        if (!parts.isEmpty()) {
            payload.setParts(parts);
        }
        gmailMessage.setPayload(payload);
        return gmailMessage;
    }

}
