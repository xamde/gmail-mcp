package com.example.gmailmcp.model;

import java.time.ZonedDateTime;

public class LocalEmail {

    private String id;
    private String from;
    private String subject;
    private String bodyText;
    private ZonedDateTime sentDate;

    public LocalEmail(String id, String from, String subject, String bodyText, ZonedDateTime sentDate) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.bodyText = bodyText;
        this.sentDate = sentDate;
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
}
