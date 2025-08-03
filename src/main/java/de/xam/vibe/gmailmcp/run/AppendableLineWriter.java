package de.xam.vibe.gmailmcp.run;

import java.io.IOException;

class AppendableLineWriter implements ILineWriter {

    private final Appendable a;

    AppendableLineWriter(Appendable a) {this.a = a;}

    @Override
    public void line(String line) throws IOException {
        a.append(line);
        a.append("\n");
    }

}
