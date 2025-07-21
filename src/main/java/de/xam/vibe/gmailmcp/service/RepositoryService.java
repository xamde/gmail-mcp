package de.xam.vibe.gmailmcp.service;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.xam.vibe.gmailmcp.model.LocalAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A service for storing and retrieving emails from the local file system.
 * This service is responsible for saving the email metadata as a JSON file and the attachments as raw files.
 *
 * @see com.example.gmailmcp.model.LocalEmail
 * @see com.example.gmailmcp.repository.LocalEmailRepository
 */
public class RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);
    private final Path repositoryPath;
    private final ObjectMapper objectMapper;

    public RepositoryService(Path repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        log.info("Initialized RepositoryService with path: {}", repositoryPath);
    }

    public void saveEmail(LocalEmail email) throws IOException {
        log.info("Saving email with ID: {}", email.getId());
        Path emailDir = repositoryPath.resolve(email.getId());
        Files.createDirectories(emailDir);

        Path metadataFile = emailDir.resolve("message.json");
        objectMapper.writeValue(metadataFile.toFile(), email);

        Path attachmentsDir = emailDir.resolve("attachments");
        Files.createDirectories(attachmentsDir);

        if (email.getAttachments() != null) {
            for (LocalAttachment attachment : email.getAttachments()) {
                Path attachmentFile = attachmentsDir.resolve(attachment.filename());
                Files.write(attachmentFile, attachment.content());
                log.debug("Saved attachment: {}", attachment.filename());
            }
        }
        log.info("Email with ID: {} saved successfully.", email.getId());
    }

    public LocalEmail retrieveEmail(String emailId) throws IOException {
        log.info("Retrieving email with ID: {}", emailId);
        Path emailDir = repositoryPath.resolve(emailId);
        Path metadataFile = emailDir.resolve("message.json");
        return objectMapper.readValue(metadataFile.toFile(), LocalEmail.class);
    }

    public void deleteEmail(String emailId) throws IOException {
        log.info("Deleting email with ID: {}", emailId);
        Path emailDir = repositoryPath.resolve(emailId);
        if (Files.exists(emailDir)) {
            try (Stream<Path> walk = Files.walk(emailDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.debug("Deleted file: {}", path);
                            } catch (IOException e) {
                                log.error("Failed to delete file: {}", path, e);
                            }
                        });
            }
            log.info("Email with ID: {} deleted successfully.", emailId);
        } else {
            log.warn("Attempted to delete non-existent email with ID: {}", emailId);
        }
    }
}
