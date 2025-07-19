package com.example.gmailmcp.tool;

import com.example.gmailmcp.service.GmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

class GmailToolServiceTest {

    @Mock
    private GmailService gmailService;

    private GmailToolService gmailToolService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gmailToolService = new GmailToolService(gmailService);
    }

    @Test
    void sendEmail() throws Exception {
        doNothing().when(gmailService).sendEmail(any(), any(), any(), any());
        gmailToolService.sendEmail("test@example.com", "Test Subject", "Test Body", null);
        verify(gmailService).sendEmail("test@example.com", "Test Subject", "Test Body", null);
    }
}
