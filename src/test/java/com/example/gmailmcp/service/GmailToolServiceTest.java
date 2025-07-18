package com.example.gmailmcp.service;

import com.example.gmailmcp.config.GoogleApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GmailToolServiceTest {

    @Mock
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleApiConfig googleApiConfig;

    private GmailToolService gmailToolService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        gmailToolService = new GmailToolService(googleAuthService);
        com.google.api.services.gmail.Gmail mockGmail = org.mockito.Mockito.mock(com.google.api.services.gmail.Gmail.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(googleAuthService.getGmailClient()).thenReturn(mockGmail);
    }

    @Test
    public void testSendEmail() throws Exception {
        var sendEmailFunction = gmailToolService.sendEmail();
        var request = new GmailToolService.SendEmail.Request("test@example.com", "Test Subject", "Test Body", null);
        sendEmailFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testReadEmail() throws Exception {
        var readEmailFunction = gmailToolService.readEmail();
        var request = new GmailToolService.ReadEmail.Request("testMessageId");
        readEmailFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testSearchEmails() throws Exception {
        var searchEmailsFunction = gmailToolService.searchEmails();
        var request = new GmailToolService.SearchEmails.Request("test query");
        searchEmailsFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testTrashEmail() throws Exception {
        var trashEmailFunction = gmailToolService.trashEmail();
        var request = new GmailToolService.TrashEmail.Request("testMessageId");
        trashEmailFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testDeleteEmail() throws Exception {
        var deleteEmailFunction = gmailToolService.deleteEmail();
        var request = new GmailToolService.DeleteEmail.Request("testMessageId");
        deleteEmailFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testMarkAsRead() throws Exception {
        var markAsReadFunction = gmailToolService.markAsRead();
        var request = new GmailToolService.MarkAsRead.Request("testMessageId");
        markAsReadFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testMarkAsUnread() throws Exception {
        var markAsUnreadFunction = gmailToolService.markAsUnread();
        var request = new GmailToolService.MarkAsUnread.Request("testMessageId");
        markAsUnreadFunction.apply(request);
        // TODO: Add verification
    }

    @Test
    public void testDownloadAttachment() throws Exception {
        com.google.api.services.gmail.model.MessagePartBody mockAttachment = new com.google.api.services.gmail.model.MessagePartBody();
        mockAttachment.setData(Base64.getUrlEncoder().encodeToString("test data".getBytes()));
        when(googleAuthService.getGmailClient().users().messages().attachments().get(anyString(), anyString(), anyString()).execute()).thenReturn(mockAttachment);

        var downloadAttachmentFunction = gmailToolService.downloadAttachment();
        var request = new GmailToolService.DownloadAttachment.Request("testMessageId", "testAttachmentId", "test.txt");
        downloadAttachmentFunction.apply(request);
        // TODO: Add verification
    }
}
