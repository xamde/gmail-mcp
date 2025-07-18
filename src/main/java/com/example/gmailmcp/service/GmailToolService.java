package com.example.gmailmcp.service;

import com.example.gmailmcp.dto.Email;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Configuration
public class GmailToolService {

    private final GoogleAuthService googleAuthService;

    public GmailToolService(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @Bean
    @Description("Sends a new email. The body can be plain text. Requires a recipient email address, a subject line, and the main body content.")
    public java.util.function.Function<SendEmail.Request, Boolean> sendEmail() {
        return new SendEmail(googleAuthService);
    }

    @Bean
    @Description("Reads the full content of a single email by its ID.")
    public java.util.function.Function<ReadEmail.Request, Email> readEmail() {
        return new ReadEmail(googleAuthService);
    }

    @Bean
    @Description("Searches for emails using a Gmail query.")
    public java.util.function.Function<SearchEmails.Request, java.util.List<Email>> searchEmails() {
        return new SearchEmails(googleAuthService);
    }

    @Bean
    @Description("Moves an email to the trash.")
    public java.util.function.Function<TrashEmail.Request, Boolean> trashEmail() {
        return new TrashEmail(googleAuthService);
    }

    @Bean
    @Description("Permanently deletes an email.")
    public java.util.function.Function<DeleteEmail.Request, Boolean> deleteEmail() {
        return new DeleteEmail(googleAuthService);
    }

    @Bean
    @Description("Marks an email as read.")
    public java.util.function.Function<MarkAsRead.Request, Boolean> markAsRead() {
        return new MarkAsRead(googleAuthService);
    }

    @Bean
    @Description("Marks an email as unread.")
    public java.util.function.Function<MarkAsUnread.Request, Boolean> markAsUnread() {
        return new MarkAsUnread(googleAuthService);
    }

    private static class SendEmail implements java.util.function.Function<SendEmail.Request, Boolean> {

        private final GoogleAuthService googleAuthService;

        public SendEmail(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String to, String subject, String body) {
        }

        @Override
        public Boolean apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                MimeMessage mimeMessage = createEmail(request.to(), request.subject(), request.body());
                Message message = createMessageWithEmail(mimeMessage);
                gmail.users().messages().send("me", message).execute();
                return true;
            } catch (IOException | MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        private MimeMessage createEmail(String to, String subject, String bodyText) throws MessagingException {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress("me"));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject);
            email.setText(bodyText);
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
    }

    private static class ReadEmail implements java.util.function.Function<ReadEmail.Request, Email> {

        private final GoogleAuthService googleAuthService;

        public ReadEmail(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String messageId) {
        }

        @Override
        public Email apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                Message message = gmail.users().messages().get("me", request.messageId()).setFormat("full").execute();
                return new ReadEmail(googleAuthService).messageToEmail(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private Email messageToEmail(Message message) {
            String from = "";
            String to = "";
            String subject = "";
            String date = "";
            if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
                for (var header : message.getPayload().getHeaders()) {
                    if (header.getName().equals("From")) {
                        from = header.getValue();
                    }
                    if (header.getName().equals("To")) {
                        to = header.getValue();
                    }
                    if (header.getName().equals("Subject")) {
                        subject = header.getValue();
                    }
                    if (header.getName().equals("Date")) {
                        date = header.getValue();
                    }
                }
            }

            String snippet = message.getSnippet();
            String bodyText = "";
            String bodyHtml = "";

            if (message.getPayload() != null) {
                if (message.getPayload().getParts() != null) {
                    for (var part : message.getPayload().getParts()) {
                        if (part.getMimeType().equals("text/plain") && part.getBody() != null && part.getBody().getData() != null) {
                            bodyText = new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                        } else if (part.getMimeType().equals("text/html") && part.getBody() != null && part.getBody().getData() != null) {
                            bodyHtml = new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                        }
                    }
                } else if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                    bodyText = new String(Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()));
                }
            }


            return new Email(message.getId(), from, to, subject, snippet, date, bodyText, bodyHtml, java.util.Collections.emptyList());
        }
    }

    private static class SearchEmails implements java.util.function.Function<SearchEmails.Request, java.util.List<Email>> {

        private final GoogleAuthService googleAuthService;

        public SearchEmails(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String query) {
        }

        @Override
        public java.util.List<Email> apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                var messages = gmail.users().messages().list("me").setQ(request.query()).execute().getMessages();
                if (messages == null) {
                    return java.util.Collections.emptyList();
                }
                return messages.stream().map(message -> {
                    try {
                        Message fullMessage = gmail.users().messages().get("me", message.getId()).setFormat("full").execute();
                        return new ReadEmail(googleAuthService).messageToEmail(fullMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(java.util.stream.Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ... existing messageToEmail implementation ...

    private static class TrashEmail implements java.util.function.Function<TrashEmail.Request, Boolean> {
        private final GoogleAuthService googleAuthService;

        public TrashEmail(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String messageId) {}

        @Override
        public Boolean apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                gmail.users().messages().trash("me", request.messageId()).execute();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DeleteEmail implements java.util.function.Function<DeleteEmail.Request, Boolean> {
        private final GoogleAuthService googleAuthService;

        public DeleteEmail(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String messageId) {}

        @Override
        public Boolean apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                gmail.users().messages().delete("me", request.messageId()).execute();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class MarkAsRead implements java.util.function.Function<MarkAsRead.Request, Boolean> {
        private final GoogleAuthService googleAuthService;

        public MarkAsRead(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String messageId) {}

        @Override
        public Boolean apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                gmail.users().messages().modify("me", request.messageId(), new com.google.api.services.gmail.model.ModifyMessageRequest().setRemoveLabelIds(java.util.Collections.singletonList("UNREAD"))).execute();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class MarkAsUnread implements java.util.function.Function<MarkAsUnread.Request, Boolean> {
        private final GoogleAuthService googleAuthService;

        public MarkAsUnread(GoogleAuthService googleAuthService) {
            this.googleAuthService = googleAuthService;
        }

        public record Request(String messageId) {}

        @Override
        public Boolean apply(Request request) {
            try {
                Gmail gmail = googleAuthService.getGmailClient();
                gmail.users().messages().modify("me", request.messageId(), new com.google.api.services.gmail.model.ModifyMessageRequest().setAddLabelIds(java.util.Collections.singletonList("UNREAD"))).execute();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
