package de.xam.vibe.gmailmcp.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import de.xam.vibe.gmailmcp.auth.GoogleAuthService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Service
public class GmailService {

    private static final Logger log = LoggerFactory.getLogger(GmailService.class);
    private final GoogleAuthService googleAuthService;
    private final long maxSearchResults;

    public GmailService(GoogleAuthService googleAuthService, @Value("${google.api.max-search-results}") long maxSearchResults) {
        this.googleAuthService = googleAuthService;
        this.maxSearchResults = maxSearchResults;
    }

    public Message fetchContentAndAttachmentsFor(Message gmailMessage) {
        try {
            // fetch the message with full payload to get content & attachment details
            gmailMessage = getEmail(gmailMessage.getId());
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to fetch content for message with ID: {}", gmailMessage.getId(), e);
            throw new RuntimeException(e);
        }
        log.info("Fetching attachments for message with ID: {}", gmailMessage.getId());
        try {
            // fetch attachments, if any
            MessagePart payload = gmailMessage.getPayload();
            if (payload != null && payload.getParts() != null) {
                for (MessagePart part : payload.getParts()) {
                    if (part.getFilename() != null && !part.getFilename().isEmpty() && part.getBody() != null && part.getBody().getAttachmentId() != null) {
                        // This is an attachment, fetch its content
                        byte[] attachmentData = getAttachment(gmailMessage.getId(), part.getBody().getAttachmentId());
                        // Set the fetched content to the message part
                        part.getBody().setData(Base64.getUrlEncoder().encodeToString(attachmentData));
                    }
                }
            }
            log.info("Attachments fetched for message with ID: {}", gmailMessage.getId());
            return gmailMessage;
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to fetch attachments for message with ID: {}", gmailMessage.getId(), e);
            // Depending on desired error handling, you might rethrow, return original, or return null
            return gmailMessage; // Return original message if fetching attachments fails
        }
    }

    public byte[] getAttachment(String messageId, String attachmentId) throws GeneralSecurityException, IOException {
        log.info("Getting attachment with ID: {} from message: {}", attachmentId, messageId);
        Gmail gmail = googleAuthService.getGmailClient();
        return gmail.users().messages().attachments().get("me", messageId, attachmentId).execute().decodeData();
    }

    public Message getEmail(String messageId) throws GeneralSecurityException, IOException {
        log.info("Getting email with ID: {}", messageId);
        Gmail gmail = googleAuthService.getGmailClient();
        return gmail.users().messages().get("me", messageId).execute();
    }

    /**
     * @param query The Gmail web interface is designed to be user-friendly and has a much more forgiving search parser.
     *              It often corrects common mistakes, like swapped month/day values, behind the scenes. APIs, on the
     *              other hand, require precise, machine-readable input and are not as lenient.Co
     * @return found messages
     */
    public List<Message> searchEmails(String query) throws GeneralSecurityException, IOException {
        log.info("Searching emails with query: {}", query);
        Gmail gmail = googleAuthService.getGmailClient();
        List<Message> messages = new ArrayList<>();

        try {
            ListMessagesResponse res = gmail.users().messages().list("me").setQ(query).setMaxResults(maxSearchResults).execute();

            Long resultSizeEstimate = res.getResultSizeEstimate();
            if (resultSizeEstimate != null && resultSizeEstimate > 2000) {
                log.warn("This is not going to end well. Expect {} results for query: {}.", resultSizeEstimate, query);
            }
            while (res.getNextPageToken() != null) {
                messages.addAll(res.getMessages());
                // get a new res which will have a new nextPageToken
                String nextPageToken = res.getNextPageToken();
                res = gmail.users().messages().list("me").setQ(query).setPageToken(nextPageToken).setMaxResults(maxSearchResults).execute();
            }
            if(res.getMessages()!=null)
                messages.addAll(res.getMessages());
            return messages;
        } catch (com.google.api.client.http.HttpResponseException e) {
            int statusCode = e.getStatusCode();
            String statusMessage = e.getStatusMessage();
            log.error("Failed to search emails. Status: {} - {}. Content: {}", statusCode, statusMessage, e.getContent());
            // Re-throw the exception or handle it as needed
            throw e;
        }
    }

    public void sendEmail(String to, String subject, String body, List<String> attachmentPaths) throws GeneralSecurityException, IOException, MessagingException {
        log.info("Sending email to: {}, subject: {}", to, subject);
        Gmail gmail = googleAuthService.getGmailClient();
        MimeMessage mimeMessage = createEmail(to, subject, body, attachmentPaths);
        Message message = createMessageWithEmail(mimeMessage);
        gmail.users().messages().send("me", message).execute();
        log.info("Email sent successfully.");
    }

    private MimeMessage createEmail(String to, String subject, String bodyText, List<String> attachmentPaths) throws MessagingException {
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

}
