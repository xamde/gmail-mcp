package de.xam.vibe.gmailmcp.service;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A service for indexing and searching emails using Apache Lucene.
 * This service is responsible for creating and maintaining the Lucene index,
 * and for providing search functionality.
 *
 * @see com.example.gmailmcp.model.LocalEmail
 * @see com.example.gmailmcp.repository.LocalEmailRepository
 */
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final Path indexPath;
    private final IndexWriter writer;

    public SearchService(Path indexPath) throws IOException {
        this.indexPath = indexPath;
        log.info("Initializing SearchService with index path: {}", indexPath);
        if (!java.nio.file.Files.exists(indexPath)) {
            java.nio.file.Files.createDirectories(indexPath);
            log.info("Created index directory: {}", indexPath);
        }
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(FSDirectory.open(indexPath), config);
        log.info("SearchService initialized successfully.");
    }

    public void addEmail(LocalEmail email) throws IOException {
        log.info("Indexing email with ID: {}", email.getId());
        Document doc = createDocument(email);
        writer.addDocument(doc);
        writer.commit();
        log.info("Email with ID: {} indexed successfully.", email.getId());
    }

    public List<String> search(String queryString) throws IOException, ParseException {
        log.info("Searching for: '{}'", queryString);
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexPath))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"subject", "bodyText", "attachmentText"}, new StandardAnalyzer());
            Query query = parser.parse(queryString);
            TopDocs results = searcher.search(query, 10);
            List<String> ids = new ArrayList<>();
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = reader.storedFields().document(scoreDoc.doc);
                ids.add(doc.get("id"));
            }
            log.info("Found {} results for query: '{}'", ids.size(), queryString);
            return ids;
        } catch (IOException | ParseException e) {
            log.error("Error while searching for query: '{}'", queryString, e);
            throw e;
        }
    }

    public void close() throws IOException {
        log.info("Closing SearchService...");
        writer.close();
        log.info("SearchService closed successfully.");
    }

    public void deleteEmail(String emailId) throws IOException {
        log.info("Deleting email with ID: {} from index.", emailId);
        writer.deleteDocuments(new Term("id", emailId));
        writer.commit();
        log.info("Email with ID: {} deleted from index successfully.", emailId);
    }

    public void updateEmail(LocalEmail email) throws IOException {
        log.info("Updating email with ID: {} in index.", email.getId());
        Document doc = createDocument(email);
        writer.updateDocument(new Term("id", email.getId()), doc);
        writer.commit();
        log.info("Email with ID: {} updated in index successfully.", email.getId());
    }

    private Document createDocument(LocalEmail email) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", email.getId(), Field.Store.YES));
        if (email.getFrom() != null) {
            doc.add(new StringField("from", email.getFrom(), Field.Store.YES));
        }
        if (email.getSubject() != null) {
            doc.add(new TextField("subject", email.getSubject(), Field.Store.YES));
        }
        if (email.getBodyText() != null) {
            doc.add(new TextField("bodyText", email.getBodyText(), Field.Store.YES));
        }
        if (email.getAttachments() != null) {
            for (var attachment : email.getAttachments()) {
                if ("application/pdf".equals(attachment.contentType())) {
                    try (PDDocument pdfDocument = Loader.loadPDF(attachment.content())) {
                        String text = new PDFTextStripper().getText(pdfDocument);
                        doc.add(new TextField("attachmentText", text, Field.Store.NO));
                    }
                }
            }
        }
        return doc;
    }
}
