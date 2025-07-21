package de.xam.vibe.gmailmcp.service;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

    private final Path repositoryPath;
    private final ObjectMapper objectMapper;

    public RepositoryService(Path repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void saveEmail(LocalEmail email) throws IOException {
        Path emailDir = repositoryPath.resolve(email.getId());
        Files.createDirectories(emailDir);

        Path metadataFile = emailDir.resolve("message.json");
        objectMapper.writeValue(metadataFile.toFile(), email);

        Path attachmentsDir = emailDir.resolve("attachments");
        Files.createDirectories(attachmentsDir);

        if (email.getAttachments() != null) {
            email.getAttachments().forEach(attachment -> {
                try {
                    Path attachmentFile = attachmentsDir.resolve(attachment.filename());
                    Files.write(attachmentFile, attachment.content());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public LocalEmail retrieveEmail(String emailId) throws IOException {
        Path emailDir = repositoryPath.resolve(emailId);
        Path metadataFile = emailDir.resolve("message.json");
        return objectMapper.readValue(metadataFile.toFile(), LocalEmail.class);
    }

    public void deleteEmail(String emailId) throws IOException {
        Path emailDir = repositoryPath.resolve(emailId);
        if (Files.exists(emailDir)) {
            try (Stream<Path> walk = Files.walk(emailDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
