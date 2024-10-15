package com.yourpackage.server;

import com.yourpackage.database.Database;
import com.yourpackage.models.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestHandlerTest {

    private UserService userService;
    private RequestHandler requestHandler;

    @BeforeEach
    public void setUp() throws IOException {
        userService = new UserService();
        requestHandler = new RequestHandler(userService);

        // Erstelle einen Testbenutzer
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /users HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        requestHandler.handlePostRequest("/users", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode
    }

    @AfterEach
    public void tearDown() {
        try (Connection conn = Database.connect()) {
            // Lösche den Testbenutzer
            String sql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(sql)) {
                deleteStmt.setString(1, "testUser");
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandlePostUserCreation() throws IOException {
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /users HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = requestHandler.handlePostRequest("/users", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals("HTTP/1.1 409 Conflict - User already exists.", response); // Erwartet, dass der Benutzer bereits existiert
    }

    @Test
    public void testHandlePostUserLogin() throws IOException {
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /sessions HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = requestHandler.handlePostRequest("/sessions", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode
        assertEquals("HTTP/1.1 200 OK\n\nToken: testUser-mtcgToken", response);
    }

    @Test
    public void testHandlePutUserUpdate() throws IOException {
        // Jetzt aktualisiere die Benutzerdaten
        String jsonInputUpdate = "{\"Name\":\"Updated Name\",\"Bio\":\"Updated bio.\",\"Image\":\"updated_image_url.jpg\"}";
        BufferedReader inUpdate = new BufferedReader(new StringReader("PUT /users/testUser HTTP/1.1\r\nContent-Length: " + jsonInputUpdate.length() + "\r\n\r\n" + jsonInputUpdate));
        String response = requestHandler.handlePutRequest("/users/testUser", new ObjectMapper().readTree(jsonInputUpdate)); // Übergebe das JsonNode

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals("HTTP/1.1 204", response); // Erwartet, dass das Update erfolgreich ist
    }
}
