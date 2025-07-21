package com.example.gmailmcp.tool;

import com.example.gmailmcp.service.GmailService;
import com.google.api.services.gmail.model.Message;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GmailToolService {

    private final GmailService gmailService;

    public GmailToolService(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    @Tool(name = "sendEmail", description = "Send an email")
    public String sendEmail(String to, String subject, String body, List<String> attachmentPaths) {
        try {
            gmailService.sendEmail(to, subject, body, attachmentPaths);
            return "Email sent successfully";
        } catch (GeneralSecurityException | IOException | jakarta.mail.MessagingException e) {
            return "Error sending email: " + e.getMessage();
        }
    }

    @Tool(name = "readEmail", description = "Read an email")
    public Message readEmail(String messageId) {
        try {
            return gmailService.getEmail(messageId);
        } catch (GeneralSecurityException | IOException e) {
            throw new com.example.gmailmcp.exception.GmailToolException("Error reading email", e);
        }
    }

    @Tool(name = "searchEmails", description = "Search for emails")
    public List<Message> searchEmails(String query) {
        try {
            return gmailService.searchEmails(query);
        } catch (GeneralSecurityException | IOException e) {
            throw new com.example.gmailmcp.exception.GmailToolException("Error searching emails", e);
        }
    }

    @Tool(name = "downloadAttachment", description = "Download an attachment")
    public String downloadAttachment(String messageId, String attachmentId, String savePath) {
        try {
            validatePath(savePath);
            byte[] attachmentBytes = gmailService.getAttachment(messageId, attachmentId);
            java.nio.file.Files.write(java.nio.file.Paths.get(savePath), attachmentBytes);
            return "Attachment downloaded successfully to " + savePath;
        } catch (GeneralSecurityException | IOException e) {
            throw new com.example.gmailmcp.exception.GmailToolException("Error downloading attachment", e);
        }
    }

    private void validatePath(String path) {
        try {
            java.nio.file.Path workingDir = java.nio.file.Paths.get("").toAbsolutePath().normalize();
            java.nio.file.Path savePath = workingDir.resolve(path).normalize();
            if (!savePath.startsWith(workingDir) || !savePath.toFile().getCanonicalPath().startsWith(workingDir.toFile().getCanonicalPath())) {
                throw new IllegalArgumentException("Invalid save path");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid save path", e);
        }
    }
}
