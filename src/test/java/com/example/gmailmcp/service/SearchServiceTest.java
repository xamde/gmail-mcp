package com.example.gmailmcp.service;

import com.example.gmailmcp.model.LocalAttachment;
import com.example.gmailmcp.model.LocalEmail;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchServiceTest {

    @TempDir
    Path tempDir;

    private SearchService searchService;
    private Path indexPath;

    @BeforeEach
    public void setUp() throws IOException {
        indexPath = tempDir.resolve("index");
        searchService = new SearchService(indexPath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        searchService.close();
    }

    @Test
    public void testAddEmail_AddsDocumentWithCorrectFields() throws IOException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath))) {
            assertEquals(1, reader.numDocs());
            assertEquals("123", reader.document(0).get("id"));
            assertEquals("test@example.com", reader.document(0).get("from"));
            assertEquals("Test Subject", reader.document(0).get("subject"));
            assertEquals("Test Body", reader.document(0).get("bodyText"));
        }
    }

    @Test
    public void testAddEmail_HandlesNullFieldsGracefully() throws IOException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("456", null, null, null, sentDate, new ArrayList<>());
        assertDoesNotThrow(() -> searchService.addEmail(email));

        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath))) {
            assertEquals(1, reader.numDocs());
            assertEquals("456", reader.document(0).get("id"));
            assertNull(reader.document(0).get("from"));
            assertNull(reader.document(0).get("subject"));
            assertNull(reader.document(0).get("bodyText"));
        }
    }

    @Test
    public void testSearch_FindsExistingEmailBySubject() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        List<String> ids = searchService.search("subject:Test");
        assertEquals(1, ids.size());
        assertEquals("123", ids.get(0));
    }

    @Test
    public void testSearch_FindsExistingEmailByBody() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        List<String> ids = searchService.search("bodyText:Body");
        assertEquals(1, ids.size());
        assertEquals("123", ids.get(0));
    }

    @Test
    public void testSearch_ReturnsEmptyListForNoMatches() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        List<String> ids = searchService.search("subject:NonExistent");
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testAddEmail_IndexesPdfText() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        List<LocalAttachment> attachments = new ArrayList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("This is a test PDF document.");
            contentStream.endText();
            contentStream.close();
            doc.save(baos);
        }
        attachments.add(new LocalAttachment("test.pdf", "application/pdf", baos.toByteArray()));
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, attachments);
        searchService.addEmail(email);

        List<String> ids = searchService.search("attachmentText:PDF");
        assertEquals(1, ids.size());
        assertEquals("123", ids.get(0));
    }

    @Test
    public void testDeleteEmail_RemovesFromIndex() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        List<String> ids = searchService.search("subject:Test");
        assertEquals(1, ids.size());

        searchService.deleteEmail("123");
        ids = searchService.search("subject:Test");
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testUpdateEmail_ReplacesDocumentInIndex() throws IOException, ParseException {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());
        searchService.addEmail(email);

        List<String> ids = searchService.search("subject:Test");
        assertEquals(1, ids.size());

        email.setSubject("New Subject");
        searchService.updateEmail(email);

        ids = searchService.search("subject:\"New Subject\"");
        assertEquals(1, ids.size());
        assertEquals("123", ids.get(0));

        ids = searchService.search("subject:\"Test Subject\"");
        assertTrue(ids.isEmpty());
    }
}
