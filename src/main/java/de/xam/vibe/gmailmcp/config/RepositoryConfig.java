package de.xam.vibe.gmailmcp.config;

import de.xam.vibe.gmailmcp.repository.LocalEmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class RepositoryConfig {

    @Bean
    public LocalEmailRepository localEmailRepository(
            @Value("${local.repo}") String repositoryPath,
            @Value("${local.index}") String indexPath) throws IOException {

        // The @Bean method is the perfect place to handle logic
        // that might throw exceptions during initialization.
        return new LocalEmailRepository(Path.of(repositoryPath), Path.of(indexPath));
    }
}
