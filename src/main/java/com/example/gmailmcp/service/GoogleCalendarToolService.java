package com.example.gmailmcp.service;

import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarToolService {

    // TODO: Implement createCalendarEvent tool
    // This tool should create a new event in the user's primary calendar.
    // It should accept the following parameters:
    // - summary (String): The title of the event.
    // - startTime (String): The start time of the event in RFC3339 format.
    // - endTime (String): The end time of the event in RFC3339 format.
    // - description (String): The description of the event.
    // - attendees (List<String>): A list of email addresses of the attendees.
    //
    // The tool should use the GoogleAuthService to get an authenticated Google Calendar client.
    // It should then create a new event and insert it into the user's primary calendar.
    // The tool should return a representation of the created event.
}
