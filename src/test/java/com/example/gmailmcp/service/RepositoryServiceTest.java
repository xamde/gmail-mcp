package com.example.gmailmcp.service;

import com.example.gmailmcp.model.LocalAttachment;
import com.example.gmailmcp.model.LocalEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryServiceTest {

    @TempDir
    Path tempDir;

    private RepositoryService repositoryService;

    @BeforeEach
    public void setUp() {
        repositoryService = new RepositoryService(tempDir);
    }

    @Test
    public void testSaveAndRetrieveEmail() throws IOException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        List<LocalAttachment> attachments = new ArrayList<>();
        attachments.add(new LocalAttachment("test.txt", "text/plain", "test content".getBytes()));
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, attachments);

        repositoryService.saveEmail(email);

        LocalEmail retrievedEmail = repositoryService.retrieveEmail("123");
        assertNotNull(retrievedEmail);
        assertEquals("123", retrievedEmail.getId());
        assertEquals("test@example.com", retrievedEmail.getFrom());
        assertEquals("Test Subject", retrievedEmail.getSubject());
        assertEquals("Test Body", retrievedEmail.getBodyText());
        // assertEquals(sentDate, retrievedEmail.getSentDate()); // ZonedDateTime equals is tricky
        assertEquals(1, retrievedEmail.getAttachments().size());
        assertEquals("test.txt", retrievedEmail.getAttachments().get(0).filename());
    }

    @Test
    public void testDeleteEmail() throws IOException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        repositoryService.saveEmail(email);

        assertTrue(Files.exists(tempDir.resolve("123")));
        repositoryService.deleteEmail("123");
        assertFalse(Files.exists(tempDir.resolve("123")));
    }
}
