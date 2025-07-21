package com.example.gmailmcp.repository;

import com.example.gmailmcp.model.LocalEmail;
import com.example.gmailmcp.service.RepositoryService;
import com.example.gmailmcp.service.SearchService;
import com.example.gmailmcp.util.EmailConverter;
import jakarta.mail.Message;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * A facade class for the local email repository.
 * This class provides a simple API for interacting with the repository, hiding the underlying complexity of the
 * {@link com.example.gmailmcp.service.RepositoryService} and {@link com.example.gmailmcp.service.SearchService}.
 *
 * @see com.example.gmailmcp.service.RepositoryService
 * @see com.example.gmailmcp.service.SearchService
 */
public class LocalEmailRepository {

    private final RepositoryService repositoryService;
    private final SearchService searchService;

    public LocalEmailRepository(Path repositoryPath, Path indexPath) throws IOException {
        this.repositoryService = new RepositoryService(repositoryPath);
        this.searchService = new SearchService(indexPath);
    }

    public void add(Message message) throws IOException, jakarta.mail.MessagingException {
        LocalEmail localEmail = EmailConverter.toLocalEmail(message);
        localEmail.setId(UUID.randomUUID().toString());
        repositoryService.saveEmail(localEmail);
        searchService.addEmail(localEmail);
    }

    public List<String> search(String query) throws IOException, ParseException {
        return searchService.search(query);
    }

    public LocalEmail get(String emailId) throws IOException {
        return repositoryService.retrieveEmail(emailId);
    }

    public void delete(String emailId) throws IOException {
        repositoryService.deleteEmail(emailId);
        searchService.deleteEmail(emailId);
    }

    public void close() throws IOException {
        searchService.close();
    }
}
