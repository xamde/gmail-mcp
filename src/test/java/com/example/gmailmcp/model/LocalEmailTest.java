package com.example.gmailmcp.model;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalEmailTest {

    @Test
    public void testLocalEmail() {
        ZonedDateTime sentDate = ZonedDateTime.now();
        LocalEmail email = new LocalEmail("123", "test@example.com", "Test Subject", "Test Body", sentDate, new ArrayList<>());

        assertEquals("123", email.getId());
        assertEquals("test@example.com", email.getFrom());
        assertEquals("Test Subject", email.getSubject());
        assertEquals("Test Body", email.getBodyText());
        assertEquals(sentDate, email.getSentDate());
    }
}
