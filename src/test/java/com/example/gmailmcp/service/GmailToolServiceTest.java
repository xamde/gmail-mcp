package com.example.gmailmcp.service;

import com.example.gmailmcp.config.GoogleApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// TODO: Implement more robust unit tests.
// These tests should mock the GoogleAuthService and verify the logic of the tool functions.
// For example, in testSendEmail, inspect the MimeMessage object to ensure the headers and body are set correctly.

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
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).send(anyString(), any(com.google.api.services.gmail.model.Message.class));
    }

    @Test
    public void testReadEmail() throws Exception {
        when(googleAuthService.getGmailClient().users().messages().get(anyString(), anyString()).execute()).thenReturn(new com.google.api.services.gmail.model.Message());
        var readEmailFunction = gmailToolService.readEmail();
        var request = new GmailToolService.ReadEmail.Request("testMessageId");
        readEmailFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).get("me", "testMessageId");
    }

    @Test
    public void testSearchEmails() throws Exception {
        when(googleAuthService.getGmailClient().users().messages().list(anyString()).execute()).thenReturn(new com.google.api.services.gmail.model.ListMessagesResponse());
        var searchEmailsFunction = gmailToolService.searchEmails();
        var request = new GmailToolService.SearchEmails.Request("test query");
        searchEmailsFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).list("me");
    }

    @Test
    public void testTrashEmail() throws Exception {
        var trashEmailFunction = gmailToolService.trashEmail();
        var request = new GmailToolService.TrashEmail.Request("testMessageId");
        trashEmailFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).trash("me", "testMessageId");
    }

    @Test
    public void testDeleteEmail() throws Exception {
        var deleteEmailFunction = gmailToolService.deleteEmail();
        var request = new GmailToolService.DeleteEmail.Request("testMessageId");
        deleteEmailFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).delete("me", "testMessageId");
    }

    @Test
    public void testMarkAsRead() throws Exception {
        var markAsReadFunction = gmailToolService.markAsRead();
        var request = new GmailToolService.MarkAsRead.Request("testMessageId");
        markAsReadFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).modify(anyString(), anyString(), any(com.google.api.services.gmail.model.ModifyMessageRequest.class));
    }

    @Test
    public void testMarkAsUnread() throws Exception {
        var markAsUnreadFunction = gmailToolService.markAsUnread();
        var request = new GmailToolService.MarkAsUnread.Request("testMessageId");
        markAsUnreadFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages()).modify(anyString(), anyString(), any(com.google.api.services.gmail.model.ModifyMessageRequest.class));
    }

    @Test
    public void testDownloadAttachment() throws Exception {
        com.google.api.services.gmail.model.MessagePartBody mockAttachment = new com.google.api.services.gmail.model.MessagePartBody();
        mockAttachment.setData(Base64.getUrlEncoder().encodeToString("test data".getBytes()));
        when(googleAuthService.getGmailClient().users().messages().attachments().get(anyString(), anyString(), anyString()).execute()).thenReturn(mockAttachment);

        var downloadAttachmentFunction = gmailToolService.downloadAttachment();
        var request = new GmailToolService.DownloadAttachment.Request("testMessageId", "testAttachmentId", "test.txt");
        downloadAttachmentFunction.apply(request);
        org.mockito.Mockito.verify(googleAuthService.getGmailClient().users().messages().attachments()).get("me", "testMessageId", "testAttachmentId");
    }
}
