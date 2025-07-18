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
}
