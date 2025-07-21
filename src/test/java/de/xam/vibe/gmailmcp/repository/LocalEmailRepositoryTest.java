package de.xam.vibe.gmailmcp.repository;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalEmailRepositoryTest {

    @TempDir
    Path tempDir;

    private LocalEmailRepository localEmailRepository;

    @BeforeEach
    public void setUp() throws IOException {
        localEmailRepository = new LocalEmailRepository(tempDir.resolve("repo"), tempDir.resolve("index"));
    }

    @AfterEach
    public void tearDown() throws IOException {
        localEmailRepository.close();
    }

    @Test
    public void testAddAndSearchIntegration() throws jakarta.mail.MessagingException, IOException, ParseException {
        Message message = mock(MimeMessage.class);
        when(message.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress("test@example.com")});
        when(message.getSubject()).thenReturn("Test Subject");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.isMimeType("text/plain")).thenReturn(true);
        when(message.getContent()).thenReturn("Test Body");

        localEmailRepository.add(message);

        List<String> ids = localEmailRepository.search("subject:Test");
        assertEquals(1, ids.size());
        String emailId = ids.get(0);

        LocalEmail retrievedEmail = localEmailRepository.get(emailId);
        assertNotNull(retrievedEmail);
        assertEquals(emailId, retrievedEmail.getId());
        assertEquals("test@example.com", retrievedEmail.getFrom());
        assertEquals("Test Subject", retrievedEmail.getSubject());
        assertEquals("Test Body", retrievedEmail.getBodyText());
    }

    @Test
    public void testAddAndDeleteIntegration() throws jakarta.mail.MessagingException, IOException, ParseException {
        Message message = mock(MimeMessage.class);
        when(message.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress("test@example.com")});
        when(message.getSubject()).thenReturn("Test Subject");
        when(message.getSentDate()).thenReturn(new Date());
        when(message.isMimeType("text/plain")).thenReturn(true);
        when(message.getContent()).thenReturn("Test Body");

        localEmailRepository.add(message);

        List<String> ids = localEmailRepository.search("subject:Test");
        assertEquals(1, ids.size());
        String emailId = ids.get(0);

        localEmailRepository.delete(emailId);

        ids = localEmailRepository.search("subject:Test");
        assertTrue(ids.isEmpty());
    }
}
