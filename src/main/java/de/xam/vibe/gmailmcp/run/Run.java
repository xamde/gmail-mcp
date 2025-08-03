package de.xam.vibe.gmailmcp.run;

import com.google.api.services.gmail.model.Message;
import de.xam.vibe.gmailmcp.GmailMcpApplication;
import de.xam.vibe.gmailmcp.model.LocalEmail;
import de.xam.vibe.gmailmcp.service.GmailService;
import de.xam.vibe.gmailmcp.util.EmailConverterGmail;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class Run {

    // 306-7899223-1604360 AMZN Mktp DE 618BM3A7F6ESZ821||FOLGELASTSCHRIFT||105

    private static final Logger log = getLogger(Run.class);

    private static void execute(GmailService app, String line, String query, ILineWriter a) throws IOException, GeneralSecurityException {
        String[] split = line.split("[| ]");
        List<String> tokens = new ArrayList<>(Stream.of(split).filter(s -> s.length() > 3)
                // sort longest first
                .sorted((s1, s2) -> s2.length() - s1.length()).toList());
        tokens.remove("FOLGELASTSCHRIFT");

        a.line();
        a.line("== Line");
        a.line("`"+ line + "`");
        for (String token : tokens) {
            String q = token + " " + query;
            List<Message> msgs = app.searchEmails(q);
            if (msgs.size() < 10 && !msgs.isEmpty()) {
                a.line();
                a.line("=== Query: `" + token + "`");
                for (Message msg : msgs) {
                    a.line();
                    a.line("==== Message");
                    // are there attached pdfs ?
                    boolean hasPdfAttachment = msg.getPayload() != null && //
                            msg.getPayload().getParts().stream().anyMatch(part ->
                                part.getFilename() != null && part.getFilename().endsWith(".pdf")
                            );
                    if (hasPdfAttachment) {
                        a.line("HAS PDF attachment.");
                    }
                    Message email = app.getEmail(msg.getId());
                    LocalEmail local = EmailConverterGmail.toLocalEmail(email);
                    a.line("FROM: **" + local.getFrom() + "**");
                    a.line("DATE: **" + local.getSentDate().toString() + "**");
                    a.line("SUBJECT: **" + local.getSubject() + "**");
                    a.line("BODY: ");

                    a.line("// BODY START");
                    String[] bodyLines = local.getBodyText().split("\n");
                    for (String bodyLine : bodyLines) {
                        a.line(bodyLine);
                    }
                    a.line("// BODY END");

                    a.line("");
                }
                return;
            }
        }
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String query = "after:2024-01-01 before:2024-12-31";
        // read /data/input.txt line by line
        List<String> lines = FileUtils.readLines(new File("./data/input.txt"), "UTF-8");

        ConfigurableApplicationContext ctx = SpringApplication.run(GmailMcpApplication.class, args);
        GmailService app = ctx.getBean(GmailService.class);

        File result = new File("./data/result-noisy.adoc");
        try (FileWriter fwr = new FileWriter(result)) {
            AppendableLineWriter lineWriter = new AppendableLineWriter(fwr);
            lineWriter.line("= Results");
            lineWriter.line(":toc:");
            lineWriter.line(":hardbreaks:");
            lineWriter.line();
            for (String line : lines) {
                log.info("Query " + line);
                execute(app, line, query, lineWriter);
            }
        }
        ctx.close();

        Denoiser.main(null);
    }

}
