package de.xam.vibe.gmailmcp.run;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class Denoiser implements ILineWriter {

    enum Mode {Fix, Noisy}

    static class Line {

        final Mode mode;
        final String line;

        Line(Mode mode, String line) {
            this.mode = mode;
            this.line = line;
        }

    }

    private static final int NOISE = 3;
    List<Line> allLines = new ArrayList<>();
    Map<String, Integer> counts = new HashMap<>();
    private Mode mode = Mode.Fix;

    private static final Logger log = getLogger(Denoiser.class);

    public static void main(String [] args) throws IOException {
        log.info("Denoising...");
        File result = new File("./data/result-noisy.adoc");
        File resultDenoised = new File("./data/result-denoised.adoc");
        try (FileWriter fwr = new FileWriter(resultDenoised)) {
            AppendableLineWriter lineWriter = new AppendableLineWriter(fwr);

            // 1) read string into memory
            String content = FileUtils.readFileToString(result, "UTF-8");
            // 2) parse line by line
            Denoiser denoiser = new Denoiser();
            for (String line : content.split("\n")) {
                denoiser.line(line);
            }
            denoiser.writeTo(lineWriter);
        }
    }

    public void fixLine(String line) {
        allLines.add(new Line(Mode.Fix, line));
    }

    StringBuilder noisyBuffer = new StringBuilder();

    @Override
    public void line(String line) {
        if (line.contains("BODY START")) {
            mode = Mode.Noisy;
            fixLine(line);
        } else if (line.contains("BODY END")) {
            mode = Mode.Fix;
            fixLine(line);
            // process noise buffer

            String htmlFreeBuffer = Cleaner.removeAllAngleBracketTags(noisyBuffer.toString());
            String asciiDocFree = Cleaner.cleanAsciiDoc(htmlFreeBuffer);
            for(String noisyLine : asciiDocFree.split("\n")) {
                noisyLine(noisyLine);
            }
            noisyBuffer.setLength(0);

        } else {
            switch (mode) {
                case Fix:
                    fixLine(line);
                    break;
                case Noisy:
                    noisyBuffer.append(line).append("\n");
                    break;
            }
        }

    }

    public void noisyLine(String line) {
        allLines.add(new Line(Mode.Noisy, line));
        counts.compute(line, (k, c) -> c == null ? 1 : c + 1);
        if (counts.get(line) > NOISE) {
            // remove all these from lines, but only within BODY parts
            allLines.removeIf(l -> l.mode == Mode.Noisy && l.line.equals(line));
        }
    }

    public void writeTo(ILineWriter w) throws IOException {
        for (Line line : allLines) {
            w.line(line.line);
        }
    }

}
