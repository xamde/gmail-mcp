package com.example.gmailmcp.dto;

import java.util.List;

public record Email(
        String id,
        String from,
        String to,
        String subject,
        String snippet,
        String date,
        String bodyText,
        String bodyHtml,
        List<Attachment> attachments) {

    public record Attachment(String attachmentId, String filename, long sizeInBytes) {
    }
}
