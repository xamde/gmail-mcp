package de.xam.vibe.gmailmcp.run;

import de.xam.vibe.gmailmcp.GmailMcpApplication;
import de.xam.vibe.gmailmcp.service.GmailService;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class Run {

    private static final Logger log = getLogger(Run.class);

    public static void withGmail(Consumer<GmailService> consumer) {
        ConfigurableApplicationContext ctx = SpringApplication.run(GmailMcpApplication.class);
        GmailService app = ctx.getBean(GmailService.class);
        consumer.accept(app);
        ctx.close();
    }

}
