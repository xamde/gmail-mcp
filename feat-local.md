# local IMAP repository with search functionality

Make a project plan for a junior developer.
Aim for 10-15 features. 

Clear Acceptance Criteria: Define precisely what "done" looks like for each task. Use a checklist format (e.g., "User can click X and see Y," "Data Z is saved to the database").

Unit Tests: Each feature must be accompanied by a set of unit tests that the developer will need to write or pass. This reinforces a test-driven mindset from day one.

Hints and Guidance:

Complexity/Criticality: Highlight parts of the codebase that are complex, critical, or have non-obvious dependencies.

Pointers to Codebase: Include links or references to specific files, modules, or functions they should look at.

Suggested Approach: Briefly outline a potential way to tackle the problem without being overly prescriptive.

Gradual Complexity: Please order the features so they ramp up in difficulty. The first few should be simple, isolated changes that build confidence. Deeper, more complex tasks should come later.

Feature Background: For each feature, add a short sentence explaining why it's important. (e.g., "This feature is crucial for improving user navigation on the dashboard.")

Documentation Links: Include links to relevant internal or external documentation, architectural diagrams, or even external articles/tutorials that might help them understand the concepts involved.

## Goal
We want to download a remote GMail account to a local IMAP repo.
The GMail access has already been built.
Now we need a local IMAP repository with search functionality in Java. Using two key libraries:

1.  **Jakarta Mail (formerly JavaMail):** For connecting to the IMAP server and fetching emails.
2.  **Apache Lucene:** For creating a powerful, local, full-text search index of the downloaded emails.

The local repo should have a nice API for adding email, searching email. Attachments should be addable, retrievable, and searchable, too. We only need to index PDF attachments.



## Background

To create a local IMAP repository with search functionality in Java, you'll primarily use two key libraries:

1.  **Jakarta Mail (formerly JavaMail):** For connecting to the IMAP server and fetching emails.
2.  **Apache Lucene:** For creating a powerful, local, full-text search index of the downloaded emails.

This approach is highly efficient because searching a local Lucene index is significantly faster than performing searches on a remote IMAP server, especially for complex queries.

-----

### \#\# Core Components

* **Jakarta Mail:** This API provides the classes needed to connect to an email server. You'll use an `IMAPStore` to connect, get a `Folder` (like "INBOX"), and retrieve `Message` objects.
* **Apache Lucene:** This is a high-performance, full-text search engine library. You'll convert each email message into a Lucene `Document`, which is a collection of `Field`s (e.g., "from", "subject", "body"). These documents are then added to a local `IndexWriter`. Searching is done using an `IndexSearcher` and a `QueryParser`.

-----

### \#\# Implementation Steps

Here's a step-by-step guide to building the system.

#### 1\. Add Dependencies

If you're using Maven, add the following to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.eclipse.angus</groupId>
        <artifactId>angus-mail</artifactId>
        <version>2.0.3</version>
    </dependency>
    <dependency>
        <groupId>jakarta.mail</groupId>
        <artifactId>jakarta.mail-api</artifactId>
        <version>2.1.3</version>
    </dependency>

    <dependency>
        <groupId>org.apache.lucene</groupId>
        <artifactId>lucene-core</artifactId>
        <version>9.11.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.lucene</groupId>
        <artifactId>lucene-queryparser</artifactId>
        <version>9.11.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.lucene</groupId>
        <artifactId>lucene-analysis-common</artifactId>
        <version>9.11.1</version>
    </dependency>
</dependencies>
```

#### 2\. Fetching and Indexing Emails

This process involves connecting to the IMAP server, fetching messages, and adding them to a Lucene index.

```java
import jakarta.mail.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class EmailIndexer {

    // Path to store the Lucene index
    private static final String INDEX_PATH = "./email_index";

    public void indexEmails(String host, String user, String password) throws MessagingException, IOException {
        // --- 1. Setup Lucene IndexWriter ---
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(indexDir, config);
        writer.deleteAll(); // Clear previous index for this example

        // --- 2. Connect to IMAP Server ---
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props, null);
        Store store = session.getStore();
        store.connect(host, user, password);

        // --- 3. Open Folder and Fetch Messages ---
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        System.out.println("Indexing " + messages.length + " messages...");

        // --- 4. Process and Index Each Message ---
        for (Message msg : messages) {
            Document doc = new Document();
            
            // Add fields to the document. Store key fields and index content fields.
            doc.add(new StringField("from", Address.toString(msg.getFrom()), Field.Store.YES));
            doc.add(new StringField("subject", msg.getSubject(), Field.Store.YES));
            doc.add(new TextField("body", getTextFromMessage(msg), Field.Store.NO)); // Index body but don't store
            doc.add(new StoredField("messageId", msg.getHeader("Message-ID")[0])); // Store for retrieval

            writer.addDocument(doc);
        }

        // --- 5. Cleanup ---
        writer.close();
        inbox.close(false);
        store.close();
        System.out.println("Indexing complete.");
    }

    // Helper to extract text content from a message (handles multipart)
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result.append("\n").append(bodyPart.getContent());
                }
            }
            return result.toString();
        }
        return "";
    }
}
```

#### 3\. Searching the Local Index

Once the index is created, you can perform fast, local searches.

```java
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class EmailSearcher {

    private static final String INDEX_PATH = "./email_index";

    public void search(String queryString) throws Exception {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDir));
        
        // The QueryParser needs to know which field to search by default
        QueryParser parser = new QueryParser("body", new StandardAnalyzer());
        Query query = parser.parse(queryString);

        TopDocs hits = searcher.search(query, 10); // Find top 10 hits

        System.out.println("Found " + hits.totalHits + " hits for query: '" + queryString + "'");
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(" - From: " + doc.get("from") + ", Subject: " + doc.get("subject"));
        }
    }
}
```

-----

### \#\# How to Use

You can put it all together in a main method.

```java
public class Main {
    public static void main(String[] args) throws Exception {
        // --- STEP 1: INDEX EMAILS (only need to do this once or periodically) ---
        EmailIndexer indexer = new EmailIndexer();
        // Replace with your actual IMAP server and credentials
        indexer.indexEmails("imap.example.com", "user@example.com", "your-password");

        // --- STEP 2: SEARCH THE LOCAL INDEX ---
        EmailSearcher searcher = new EmailSearcher();
        
        // Example searches
        System.out.println("\n--- Searching for 'report' in the body ---");
        searcher.search("report");

        System.out.println("\n--- Searching for emails from a specific sender ---");
        searcher.search("from:\"some.sender@example.com\"");
        
        System.out.println("\n--- Searching for a specific subject ---");
        searcher.search("subject:\"Weekly Update\"");
    }
}
```

### \#\# Important Considerations

* **Synchronization:** For a real application, you shouldn't re-index everything each time. Use the IMAP `UID` (Unique ID) of each message. Store the `UID`s of indexed messages and only fetch new messages (those with a `UID` greater than the highest one you have stored).
* **Error Handling:** The example code uses `throws` for simplicity. A production application should have robust `try-catch` blocks for `IOException` and `MessagingException`.
* **Attachments:** The `getTextFromMessage` helper only handles plain text. To index content from attachments (like PDFs or Word documents), you would need to integrate a library like **Apache Tika**.
* **Performance:** For very large mailboxes, run the indexing process in a background thread to keep your application's UI responsive.
