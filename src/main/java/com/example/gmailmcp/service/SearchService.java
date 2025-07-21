package com.example.gmailmcp.service;

import com.example.gmailmcp.model.LocalEmail;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private final Path indexPath;
    private final IndexWriter writer;

    public SearchService(Path indexPath) throws IOException {
        this.indexPath = indexPath;
        if (!java.nio.file.Files.exists(indexPath)) {
            java.nio.file.Files.createDirectories(indexPath);
        }
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(FSDirectory.open(indexPath), config);
    }

    public void addEmail(LocalEmail email) throws IOException {
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
        writer.addDocument(doc);
        writer.commit();
    }

    public List<String> search(String queryString) throws IOException, ParseException {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexPath))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("bodyText", new StandardAnalyzer());
            Query query = parser.parse(queryString);
            TopDocs results = searcher.search(query, 10);
            List<String> ids = new ArrayList<>();
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = reader.storedFields().document(scoreDoc.doc);
                ids.add(doc.get("id"));
            }
            return ids;
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}
