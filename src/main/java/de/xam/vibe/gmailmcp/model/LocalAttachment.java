package de.xam.vibe.gmailmcp.model;

/**
 * Represents an email attachment stored locally.
 * This is a simple record that holds the filename, content type, and content of an attachment.
 * It is used by the {@link LocalEmail} class.
 *
 * @param filename the name of the attachment file
 * @param contentType the MIME type of the attachment
 * @param content the raw byte content of the attachment
 */
public record LocalAttachment(String filename, String contentType, byte[] content) {
}
