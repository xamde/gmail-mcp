package de.xam.vibe.gmailmcp.run;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;


public class Cleaner {

    private static void appendTextNodesLineByLine(Node node, StringBuilder b) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).getWholeText();
            if (!text.trim().isEmpty()) {
                b.append(text).append("\n");
            }
        }
        for (Node child : node.childNodes()) {
            appendTextNodesLineByLine(child, b);
        }
    }

    public static String cleanAsciiDoc(String content) {
        StringBuilder b = new StringBuilder();
        for (String line : content.split("\n")) {
            if (line.startsWith("____") || line.startsWith("----")) {
                b.append("\n");
            } else {
                b.append(line.trim()).append("\n");
            }
        }
        return b.toString();
    }

    /**
     * Use JSoup to clean up
     *
     * @param content with angle brackets
     * @return a jsoup-cleaned string
     */
    public static String removeAllAngleBracketTags(String content) {
        if (content == null) return null;
        Document doc = Jsoup.parse(content);
        StringBuilder b = new StringBuilder();
        appendTextNodesLineByLine(doc.body(), b);
        return b.toString();
    }

}
