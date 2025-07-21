package de.xam.vibe.gmailmcp.model;

public record LocalAttachment(String filename, String contentType, byte[] content) {
}
