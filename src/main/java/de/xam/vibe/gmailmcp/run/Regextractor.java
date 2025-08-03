package de.xam.vibe.gmailmcp.run;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.readAllBytes;
import static org.slf4j.LoggerFactory.getLogger;

public class Regextractor {


    private static final Logger log = getLogger(Regextractor.class);

    /**
     * Run regex on text file contents, for each match: create a map of named capture groups, and add extracted value.
     *
     * @param f to parse
     * @return list of matches
     */
    private static List<Map<String, String>> extract(File f, String regexOnce, String regexDetails) throws IOException {
        List<Map<String, String>> results = new java.util.ArrayList<>();
        String content = new String(readAllBytes(f.toPath()));

        // 1) use regexOnce to bind bestellnummer and blocks
        // 2) use regexDetails to parse details
        Pattern oncePattern = Pattern.compile(regexOnce);
        Matcher onceMatcher = oncePattern.matcher(content);
        String once = null;
        Set<String> details = Pattern.compile(regexDetails).namedGroups().keySet();
        while (onceMatcher.find()) {
            Map<String, String> baseMap = new HashMap<>();
            String bestellnummer = onceMatcher.group("bestellnummer");
            String productsBlock = onceMatcher.group("details");
            log.info("===================================== Bestellnummer: {}\n{}", bestellnummer, productsBlock);
            if (bestellnummer != null) {
                baseMap.put("bestellnummer", bestellnummer.replace("\n", " "));
            }
            if (productsBlock != null) {
                Pattern detailsPattern = Pattern.compile(regexDetails);
                Matcher detailsMatcher = detailsPattern.matcher(productsBlock);

                while (detailsMatcher.find()) {
                    Map<String, String> productMap = new HashMap<>(baseMap); // Start with base info

                    for (String groupName : detailsPattern.namedGroups().keySet()) {
                        String value = detailsMatcher.group(groupName);
                        if (value != null) {
                            productMap.put(groupName, value.replace("\n", " "));
                        }
                    }
                    results.add(productMap);
                }
            } else {
                results.add(baseMap); // Add if no details regex or no products block
            }
        }
        results.sort((m1, m2) -> m1.get("bestellnummer").compareTo(m2.get("bestellnummer")));
        return results;
    }

    public static void main(String[] args) throws IOException {
        File f = new File("./data/result-denoised.adoc");

        // Use this regex to capture the main order number and a single block containing all products.
        String primaryRegex = "(?s)Bestellung #(?<bestellnummer>[\\d-]+)\\s*(?<details>(?:(?!BODY END).)*)";
        // Use this regex in a loop on the 'details'. It captures details for one product at a time.


        String productRegex = "(?s)" +
                // Captures all text (including newlines) up to the price, non-greedily.
                "(?<bestellinhalt>.+?)" +
                // Matches the whitespace before the price.
                "\\s+" +
                // Captures the numeric price value.
                "EUR (?<preis>[\\d,]+)" +
                // Matches the "Verkauft von:" label and subsequent whitespace.
                "\\s+Verkauft von:\\s+" +
                // Captures the seller's name, which can contain spaces and commas.
                "(?<seller>.+?)" +
                // Matches the "Endbetrag" line.
                "\\s+Endbetrag" +
                // An optional group to check if VAT is included. Will be null if not present.
                "(?<ust>\\s*inkl\\. USt\\.)?" +
                // Matches the final colon.
                ":";
        List<Map<String, String>> results = extract(f, primaryRegex, productRegex);
        // dump results
        Appendable a  = new StringBuilder();
        for (Map<String, String> result : results) {
            a.append("Order _____________________ "+result.get("bestellnummer")+"\n");
            result.forEach((k, v) -> {
                try {
                    a.append(k+": "+v+"\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // write results to file. When coming from filename.ext now write to filename"-result".ext
        File resultFile = new File(f.getParentFile(), f.getName().replace(".", "-result."));
        FileUtils.writeStringToFile(resultFile, a.toString(), "UTF-8");
        log.info("Results written to file: {}", resultFile.getAbsolutePath());
    }


}
