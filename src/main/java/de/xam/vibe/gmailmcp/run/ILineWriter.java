package de.xam.vibe.gmailmcp.run;

import java.io.IOException;

interface ILineWriter {

    void line(String line) throws IOException;

    default void line() throws IOException {
        line("");
    }

}
