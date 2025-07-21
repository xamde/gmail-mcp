package de.xam.vibe.gmailmcp.repository;

import de.xam.vibe.gmailmcp.model.LocalEmail;
import de.xam.vibe.gmailmcp.service.RepositoryService;
import de.xam.vibe.gmailmcp.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * A facade class for the local email repository. This class provides a simple API for interacting with the repository,
 * hiding the underlying complexity of the {@link de.xam.vibe.gmailmcp.service.RepositoryService} and
 * {@link de.xam.vibe.gmailmcp.service.SearchService}.
 *
 * @see de.xam.vibe.gmailmcp.service.RepositoryService
 * @see de.xam.vibe.gmailmcp.service.SearchService
 */
public class LocalEmailRepository {

    private final RepositoryService repositoryService;
    private final SearchService searchService;

    public LocalEmailRepository(Path repoPath, Path indexPath) throws IOException {
        this.repositoryService = new RepositoryService(repoPath);
        this.searchService = new SearchService(indexPath);
    }

    public void add(LocalEmail localEmail) throws IOException, jakarta.mail.MessagingException {
        localEmail.setId(UUID.randomUUID().toString());
        repositoryService.saveEmail(localEmail);
        searchService.addEmail(localEmail);
    }

    public void close() throws IOException {
        searchService.close();
    }

    public void delete(String emailId) throws IOException {
        repositoryService.deleteEmail(emailId);
        searchService.deleteEmail(emailId);
    }

    public LocalEmail get(String emailId) throws IOException {
        return repositoryService.retrieveEmail(emailId);
    }

    public List<String> search(String query) throws IOException, ParseException {
        return searchService.search(query);
    }

}
