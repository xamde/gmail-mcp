package com.example.gmailmcp.service;

import com.example.gmailmcp.auth.GoogleAuthService;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Service
public class GmailService {

    private final GoogleAuthService googleAuthService;
    private final long maxSearchResults;

    public GmailService(GoogleAuthService googleAuthService, @Value("${google.api.max-search-results}") long maxSearchResults) {
        this.googleAuthService = googleAuthService;
        this.maxSearchResults = maxSearchResults;
    }

    public void sendEmail(String to, String subject, String body, List<String> attachmentPaths) throws GeneralSecurityException, IOException, MessagingException {
        Gmail gmail = googleAuthService.getGmailClient();
        MimeMessage mimeMessage = createEmail(to, subject, body, attachmentPaths);
        Message message = createMessageWithEmail(mimeMessage);
        gmail.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmail(String to, String subject, String bodyText, List<String> attachmentPaths) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        // Create a multipart message
        var multipart = new jakarta.mail.internet.MimeMultipart();

        // Create the message part
        var messageBodyPart = new jakarta.mail.internet.MimeBodyPart();
        messageBodyPart.setText(bodyText);
        multipart.addBodyPart(messageBodyPart);

        // Add attachments
        if (attachmentPaths != null) {
            for (String attachmentPath : attachmentPaths) {
                var attachmentPart = new jakarta.mail.internet.MimeBodyPart();
                var source = new jakarta.activation.FileDataSource(attachmentPath);
                attachmentPart.setDataHandler(new jakarta.activation.DataHandler(source));
                attachmentPart.setFileName(new java.io.File(attachmentPath).getName());
                multipart.addBodyPart(attachmentPart);
            }
        }

        email.setContent(multipart);

        return email;
    }

    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public Message getEmail(String messageId) throws GeneralSecurityException, IOException {
        Gmail gmail = googleAuthService.getGmailClient();
        return gmail.users().messages().get("me", messageId).execute();
    }

    public List<Message> searchEmails(String query) throws GeneralSecurityException, IOException {
        Gmail gmail = googleAuthService.getGmailClient();
        return gmail.users().messages().list("me").setQ(query).setMaxResults(maxSearchResults).execute().getMessages();
    }

    public byte[] getAttachment(String messageId, String attachmentId) throws GeneralSecurityException, IOException {
        Gmail gmail = googleAuthService.getGmailClient();
        return gmail.users().messages().attachments().get("me", messageId, attachmentId).execute().decodeData();
    }
}
