package com.yourpackage.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;

public class RequestHandlerTest {

    @Test
    public void testHandleGetRequest() {
        // Teste die GET-Anfrage für die Benutzerliste
        String response = RequestHandler.handleRequest("GET", "/users", new Socket(), new BufferedReader(new StringReader("")));
        assertEquals("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"message\":\"List of users\"}", response);
    }

    @Test
    public void testHandlePostUserCreation() throws IOException {
        // Simuliere eine POST-Anfrage zur Benutzerauswahl
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /users HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = RequestHandler.handlePostRequest("/users", new Socket(), in);

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals("HTTP/1.1 201", response);
    }

    @Test
    public void testHandlePostUserLogin() throws IOException {
        // Simuliere eine POST-Anfrage zur Benutzerauswahl
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /sessions HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = RequestHandler.handlePostRequest("/sessions", new Socket(), in);

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals("HTTP/1.1 200 OK\n\nToken: testUser-mtcgToken", response);
    }

    // Füge hier weitere Tests hinzu, um die verschiedenen Szenarien abzudecken.
}
