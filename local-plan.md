Here is a project plan to build a local IMAP repository with full-text search capabilities using Jakarta Mail and Apache Lucene.

The plan is broken down into 11 features, ordered by increasing complexity to build confidence and skills progressively.

-----

### **Feature 1: Project Setup and Dependencies**

* **Feature Background:** This initial step ensures the project is correctly configured with all the necessary tools, forming a stable foundation for all future work.
* **Acceptance Criteria:**
    * ✅ `jakarta.mail:jakarta.mail-api` is added as a dependency.
    * ✅ `org.eclipse.angus:angus-mail` is added as a runtime dependency (the implementation for Jakarta Mail).
    * ✅ `org.apache.lucene:lucene-core` is added as a dependency.
    * ✅ `org.apache.lucene:lucene-queryparser` is added as a dependency.
    * ✅ A basic `LocalImap.java` class exists and runs without errors.
* **Unit Tests:**
    * No specific unit tests are required, but the project should compile and run successfully.
* **Documentation Links:**
    * [Jakarta Mail Dependencies](https://www.google.com/search?q=https://eclipse-ee4j.github.io/mail/docs/api/jakarta.mail/module-summary.html)

-----

### **Feature 2: Create the Core Email Data Model**

* **Feature Background:** Creating a custom Plain Old Java Object (POJO) decouples our application from the Jakarta Mail library, making it easier to manage, test, and use with Lucene.
* **Acceptance Criteria:**
    * ✅ A `LocalEmail.java` class is created.
    * ✅ The class has fields for `id` (String), `from` (String), `subject` (String), `bodyText` (String), and `sentDate` (java.time.ZonedDateTime).
    * ✅ The class has appropriate constructors, getters, and setters.
* **Unit Tests:**
    * Write a simple test to ensure the `LocalEmail` object can be instantiated and its properties can be set and retrieved correctly.
* **Hints and Guidance:**
    * **Complexity/Criticality:** This is a foundational, low-complexity task. Getting this right is important for all subsequent features.
    * **Suggested Approach:** Create a new Java record or class. Consider using `final` fields with a constructor and only getters for immutability, which is a good practice.
* **Documentation Links:**
    * [Java Records](https://docs.oracle.com/en/java/javase/17/language/records.html)

-----

### **Feature 3: Initialize the Lucene Index**

* **Feature Background:** This feature involves setting up the on-disk directory and core components that Lucene will use to store its search index.
* **Acceptance Criteria:**
    * ✅ A `SearchService.java` class is created.
    * ✅ The class has a method `initializeIndex(Path indexPath)` that creates the directory if it doesn't exist.
    * ✅ The class can successfully create an `IndexWriter` object pointing to the specified path.
    * ✅ The `IndexWriter` is properly closed to prevent resource leaks.
* **Unit Tests:**
    * `testInitializeIndex_CreatesDirectory()`: Verify that if a non-existent path is provided, the directory is created on the file system.
    * `testInitializeIndex_DoesNotThrowErrorForExistingDirectory()`: Verify that initializing on an existing directory works without errors.
* **Hints and Guidance:**
    * **Pointers to Codebase:** This logic will live inside your new `SearchService.java`.
    * **Suggested Approach:** Use a `StandardAnalyzer` for simplicity. The core components you'll need are `Directory`, `FSDirectory`, `IndexWriterConfig`, and `IndexWriter`. Remember to wrap your `IndexWriter` in a `try-with-resources` block to ensure it's always closed.
* **Documentation Links:**
    * [Apache Lucene `IndexWriter`](https://www.google.com/search?q=%5Bhttps://lucene.apache.org/core/9_11_0/core/org/apache/lucene/index/IndexWriter.html%5D\(https://lucene.apache.org/core/9_11_0/core/org/apache/lucene/index/IndexWriter.html\))

-----

### **Feature 4: Index a Basic Email**

* **Feature Background:** This connects our email model to the search engine, forming the foundation of our search functionality.
* **Acceptance Criteria:**
    * ✅ The `SearchService` has a method `addEmail(LocalEmail email)`.
    * ✅ When called, this method creates a Lucene `Document`.
    * ✅ The Lucene `Document` contains fields for `id`, `from`, `subject`, and `bodyText`.
    * ✅ The document is successfully added to the Lucene index via the `IndexWriter`.
* **Unit Tests:**
    * `testAddEmail_AddsDocumentWithCorrectFields()`: After indexing an email, use an `IndexReader` and `IndexSearcher` to verify the document exists and its fields match the input `LocalEmail` object.
    * `testAddEmail_HandlesNullFieldsGracefully()`: Ensure the method doesn't crash if an email with a `null` subject or body is passed.
* **Hints and Guidance:**
    * **Complexity/Criticality:** This is the first real interaction with Lucene's core indexing API. Pay attention to field types.
    * **Pointers to Codebase:** This is a new method in `SearchService.java`.
    * **Suggested Approach:** Use `StringField` for the `id` and `from` fields (as you don't want them tokenized) and `TextField` for `subject` and `bodyText` (as these should be full-text searchable).
* **Documentation Links:**
    * [Lucene `Field` Types](https://www.google.com/search?q=%5Bhttps://www.baeldung.com/lucene-indexing-document-field%5D\(https://www.baeldung.com/lucene-indexing-document-field\))

-----

### **Feature 5: Implement Basic Search**

* **Feature Background:** With emails indexed, this feature delivers the primary value of the application: finding emails based on a query.
* **Acceptance Criteria:**
    * ✅ `SearchService` has a method `search(String queryString)` that returns a `List<String>` of email IDs.
    * ✅ The method correctly parses a user query string.
    * ✅ It executes the search against the Lucene index.
    * ✅ It returns the `id` field of the documents that match.
* **Unit Tests:**
    * `testSearch_FindsExistingEmailBySubject()`: Index an email and verify that searching for a word in its subject returns its ID.
    * `testSearch_FindsExistingEmailByBody()`: Index an email and verify that searching for a word in its body returns its ID.
    * `testSearch_ReturnsEmptyListForNoMatches()`: Verify that an empty list is returned when no documents match the query.
* **Hints and Guidance:**
    * **Pointers to Codebase:** New method in `SearchService.java`.
    * **Suggested Approach:** You'll need to open an `IndexReader` using `DirectoryReader.open()`. Create an `IndexSearcher` from the reader. Use a `QueryParser` to parse the `queryString` into a `Query` object. Then, use `indexSearcher.search()` to get the `TopDocs`. Iterate through the `scoreDocs` to retrieve each matching `Document` and extract its ID.
* **Documentation Links:**
    * [Lucene `QueryParser`](https://www.google.com/search?q=%5Bhttps://lucene.apache.org/core/9_11_0/queryparser/org/apache/lucene/queryparser/classic/QueryParser.html%5D\(https://lucene.apache.org/core/9_11_0/queryparser/org/apache/lucene/queryparser/classic/QueryParser.html\))

-----

### **Feature 6: Create a Jakarta Mail to LocalEmail Converter**

* **Feature Background:** This utility is a crucial bridge that translates emails fetched from the live server into the application's internal format.
* **Acceptance Criteria:**
    * ✅ A new `EmailConverter.java` utility class is created.
    * ✅ It has a static method `toLocalEmail(jakarta.mail.Message message)`.
    * ✅ The method correctly extracts the subject, sender (`From` header), and sent date.
    * ✅ The method extracts the plain text body part of the message.
* **Unit Tests:**
    * Create mock `jakarta.mail.internet.MimeMessage` objects for testing.
    * `testToLocalEmail_ConvertsSimpleMessage()`: Test with a basic message containing a subject, from address, and text body.
    * `testToLocalEmail_HandlesMultipartMessage()`: Test with a multipart message and ensure only the `text/plain` part is extracted for the body.
* **Hints and Guidance:**
    * **Complexity/Criticality:** Parsing a `MimeMessage` can be complex because emails can have nested parts. The `message.getContent()` can return a `String` (for simple messages) or a `Multipart` object. You'll need to recursively parse the `Multipart` object to find the content you need.
    * **Pointers to Codebase:** This is a new, self-contained utility class.
    * **Suggested Approach:**
        1.  Check `message.isMimeType("text/plain")`. If so, `getContent()` is your body.
        2.  If not, check `message.isMimeType("multipart/*")`. If so, cast `getContent()` to `Multipart` and loop through its `BodyPart`s, looking for the one with the `text/plain` MIME type.
* **Documentation Links:**
    * [Jakarta Mail FAQ: Reading Multipart Messages](https://www.google.com/search?q=https://eclipse-ee4j.github.io/mail/docs/faq.html%23getpart)

-----

### **Feature 7: Add Attachments to the Data Model and Converter**

* **Feature Background:** This extends the core functionality to handle attachments, a fundamental part of modern email.
* **Acceptance Criteria:**
    * ✅ The `LocalEmail.java` class is updated with a `List<LocalAttachment>` field.
    * ✅ A `LocalAttachment.java` class/record is created with fields for `filename` (String), `contentType` (String), and `content` (`byte[]`).
    * ✅ The `EmailConverter` is updated to find attachment parts in a `MimeMessage`.
    * ✅ For each attachment, it extracts the filename, content type, and content as a byte array.
* **Unit Tests:**
    * `testToLocalEmail_ExtractsAttachments()`: Test with a mock `MimeMessage` that includes one or more attachments. Verify the `LocalEmail` object contains the correct attachment data.
* **Hints and Guidance:**
    * **Suggested Approach:** In your `EmailConverter`'s multipart parsing logic, look for `BodyPart`s where the disposition is `Part.ATTACHMENT`. You can get the content as an `InputStream` and read it into a `byte[]`.
* **Documentation Links:**
    * [Jakarta `Part` Interface](https://www.google.com/search?q=%5Bhttps://eclipse-ee4j.github.io/mail/docs/api/jakarta.mail/jakarta/mail/Part.html%5D\(https://eclipse-ee4j.github.io/mail/docs/api/jakarta.mail/jakarta/mail/Part.html\)) (Check the `ATTACHMENT` and `INLINE` constants).

-----

### **Feature 8: Implement Local Email Storage and Retrieval**

* **Feature Background:** To function as a true local repository, we need to save the emails (and their attachments) to disk, not just in the search index.
* **Acceptance Criteria:**
    * ✅ A `RepositoryService.java` class is created.
    * ✅ It has a `saveEmail(LocalEmail email)` method.
    * ✅ This method saves the email's raw content or metadata to a structured directory (e.g., `/repo/{email-id}/message.json`).
    * ✅ It also saves any attachments to the same directory (e.g., `/repo/{email-id}/attachments/{filename}`).
    * ✅ A `retrieveEmail(String emailId)` method exists that can reconstruct the `LocalEmail` object from the file system.
* **Unit Tests:**
    * `testSaveAndRetrieveEmail()`: Save a `LocalEmail` object (with attachments), then immediately retrieve it and assert that the retrieved object is identical to the original.
* **Hints and Guidance:**
    * **Suggested Approach:** A simple approach is to serialize the `LocalEmail` metadata (everything but attachment content) as a JSON file using a library like Gson or Jackson. Then, save each attachment's byte array to its own file. The `emailId` can be used as the parent folder name.
* **Documentation Links:**
    * [Baeldung: Introduction to Jackson](https://www.baeldung.com/jackson-object-mapper-tutorial)

-----

### **Feature 9: Integrate PDF Attachment Indexing**

* **Feature Background:** This is a key requirement, enabling users to search for text *inside* PDF attachments.
* **Acceptance Criteria:**
    * ✅ Add a dependency for Apache PDFBox (`org.apache.pdfbox:pdfbox`).
    * ✅ `SearchService.addEmail()` is updated to check for PDF attachments.
    * ✅ If a PDF attachment is found (`contentType` is `application/pdf`), its text content is extracted using PDFBox.
    * ✅ The extracted text is added to a new, dedicated `attachmentText` field in the Lucene `Document`.
    * ✅ The `search()` method is updated to also search this new field by default.
* **Unit Tests:**
    * `testAddEmail_IndexesPdfText()`: Create a test `LocalEmail` with a sample PDF as an attachment. Index the email. Verify that a search for a unique word inside the PDF returns the email's ID.
* **Hints and guidance:**
    * **Complexity/Criticality:** This introduces a new, powerful dependency. Parsing PDFs can be resource-intensive.
    * **Pointers to Codebase:** Modify `SearchService.addEmail()`. You'll need a new utility method for parsing.
    * **Suggested Approach:** Create a helper method `extractTextFromPdf(byte[] pdfContent)`. Inside, use `PDDocument.load(pdfContent)` and `PDFTextStripper` to get the text. In `addEmail`, loop through attachments, and if it's a PDF, call this helper and add the result to a new `TextField` in your Lucene `Document`. To search multiple fields, use a `MultiFieldQueryParser`.
* **Documentation links:**
    * [Apache PDFBox - Extracting Text](https://www.google.com/search?q=https://pdfbox.apache.org/docs/2.0.x/manual.html%23text-extraction)
    * [Lucene `MultiFieldQueryParser`](https://www.google.com/search?q=%5Bhttps://lucene.apache.org/core/9_11_0/queryparser/org/apache/lucene/queryparser/classic/MultiFieldQueryParser.html%5D\(https://lucene.apache.org/core/9_11_0/queryparser/org/apache/lucene/queryparser/classic/MultiFieldQueryParser.html\))

-----

### **Feature 10: Implement Update and Delete**

* **Feature Background:** A full-featured repository must allow for emails to be updated (e.g., marked as read) or removed entirely.
* **Acceptance Criteria:**
    * ✅ `SearchService` has a `deleteEmail(String emailId)` method that removes the document from the Lucene index.
    * ✅ `RepositoryService` has a `deleteEmail(String emailId)` method that deletes the email's directory from the file system.
    * ✅ `SearchService` has an `updateEmail(LocalEmail email)` method that updates the existing document in the index.
* **Unit Tests:**
    * `testDeleteEmail_RemovesFromIndexAndRepo()`: Add an email, then delete it. Verify it can no longer be found via search and its files are gone.
    * `testUpdateEmail_ReplacesDocumentInIndex()`: Add an email, then update it (e.g., change the subject). Verify a search for the new subject finds it, and a search for the old subject does not.
* **Hints and Guidance:**
    * **Complexity/Criticality:** Deleting and updating are critical operations. An incorrect implementation could corrupt the index.
    * **Suggested Approach:** For both update and delete in Lucene, you use the `IndexWriter`. `indexWriter.deleteDocuments(new Term("id", emailId))` is used for deletion. For an update, you use `indexWriter.updateDocument(new Term("id", emailId), newDocument)`, which is an atomic delete-then-add operation.
* **Documentation Links:**
    * [Lucene `IndexWriter.updateDocument`](https://www.google.com/search?q=%5Bhttps://lucene.apache.org/core/9_11_0/core/org/apache/lucene/index/IndexWriter.html%23updateDocument-org.apache.lucene.index.Term-java.lang.Iterable-%5D\(https://lucene.apache.org/core/9_11_0/core/org/apache/lucene/index/IndexWriter.html%23updateDocument-org.apache.lucene.index.Term-java.lang.Iterable-\))

-----

### **Feature 11: Create the Main Repository API**

* **Feature Background:** This final feature ties everything together into a clean, easy-to-use public API, hiding the underlying complexity of the repository and search services.
* **Acceptance Criteria:**
    * ✅ A `LocalEmailRepository.java` facade class is created.
    * ✅ It is initialized with the paths for the file repository and the Lucene index.
    * ✅ It exposes public methods: `add(jakarta.mail.Message message)`, `search(String query)`, `get(String emailId)`, `delete(String emailId)`.
    * ✅ The `add` method correctly calls the converter, repository service, and search service in the correct order.
* **Unit Tests:**
    * These will be integration tests rather than unit tests.
    * `testAddAndSearchIntegration()`: Use the facade to add a mock `MimeMessage`, then immediately use the facade's search method to find it.
    * `testAddAndDeleteIntegration()`: Use the facade to add a message, then delete it, and verify it's no longer searchable.
* **Hints and Guidance:**
    * **Pointers to Codebase:** This is a new, top-level class that will instantiate and use `RepositoryService` and `SearchService`.
    * **Suggested Approach:** This class acts as a "facade." Its constructor should initialize the other services. Its methods will orchestrate calls to the other components. For example, the `add` method will first call `EmailConverter`, then call `repositoryService.saveEmail()`, and finally `searchService.addEmail()`.
* **Documentation Links:**
    * [Facade Design Pattern](https://en.wikipedia.org/wiki/Facade_pattern)
