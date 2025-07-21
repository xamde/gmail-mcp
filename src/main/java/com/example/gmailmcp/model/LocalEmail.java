package com.example.gmailmcp.model;

import java.time.ZonedDateTime;

import java.util.List;

/**
 * Represents an email message stored locally.
 * This class is a Plain Old Java Object (POJO) that is used to decouple the application from the Jakarta Mail library.
 * It is used by the {@link com.example.gmailmcp.service.RepositoryService} to store emails and by the
 * {@link com.example.gmailmcp.service.SearchService} to index them.
 *
 * @see com.example.gmailmcp.model.LocalAttachment
 * @see com.example.gmailmcp.util.EmailConverter
 */
public class LocalEmail {

    private String id;
    private String from;
    private String subject;
    private String bodyText;
    private ZonedDateTime sentDate;
    private List<LocalAttachment> attachments;

    public LocalEmail() {
    }

    public LocalEmail(String id, String from, String subject, String bodyText, ZonedDateTime sentDate, List<LocalAttachment> attachments) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.bodyText = bodyText;
        this.sentDate = sentDate;
        this.attachments = attachments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public ZonedDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(ZonedDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public List<LocalAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<LocalAttachment> attachments) {
        this.attachments = attachments;
    }
}
