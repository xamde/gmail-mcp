package de.xam.vibe.gmailmcp;

import com.google.api.services.gmail.model.Message;
import de.xam.vibe.gmailmcp.model.LocalEmail;
import de.xam.vibe.gmailmcp.repository.LocalEmailRepository;
import de.xam.vibe.gmailmcp.service.GmailService;
import de.xam.vibe.gmailmcp.util.EmailConverterGmail;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@SpringBootApplication
public class RunLocalRepo {

    private static final Logger log = getLogger(RunLocalRepo.class);
    private final GmailService gmailService;
    LocalEmailRepository localEmailRepo;
    @Value("${local.year}") String year;

    public RunLocalRepo(LocalEmailRepository localEmailRepo, GmailService gmailService) {
        this.localEmailRepo = localEmailRepo;
        this.gmailService = gmailService;
    }

    /**
     * Use GMailService to download all mail to a {@link LocalEmailRepository}. Auth with {@link GmailService}.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RunLocalRepo.class, args);
        // get RunLocalRepo instance
        try (ctx) {
            RunLocalRepo localRepo = ctx.getBean(RunLocalRepo.class);
            try {
                localRepo.run();
            } catch (GeneralSecurityException | IOException e) {
                log.warn("Failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void run() throws GeneralSecurityException, IOException {
        // Test search works , should be more than 0 results
        // TODO query = "Rechnung" works.
        // filename:pdf
        // in:anywhere
        // https://support.google.com/mail/answer/7190?hl=de
        String query = "subject:(Rechnung) after:" + year + "/01/01 before:" + year + "/12/31";
        List<Message> res = gmailService.searchEmails(query);
        log.info("Query '{}' => {}",query, res.size());

        // download some new mail for a given date range to also test repo
        List<Message> gmailMessages = gmailService.searchEmails("in:inbox newer_than:2d");
        log.info("Found {} messages", gmailMessages.size());
        for (Message gmailMessage : gmailMessages) {
            try {
                LocalEmail localEmail = EmailConverterGmail.toLocalEmailWithAttachments(gmailMessage, gmailService);
                localEmailRepo.add(localEmail);
            } catch (jakarta.mail.MessagingException e) {
                log.warn("Failed to add email with ID: {}", gmailMessage.getId(), e);
            }
        }

    }

}
