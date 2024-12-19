package com.yourpackage.server;

import com.yourpackage.database.Database;
import com.yourpackage.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RequestHandlerTest {

    private UserService userService;
    private RequestHandler requestHandler;
    private GetRequestHandler getRequestHandler;
    private PostRequestHandler postRequestHandler;
    private PutRequestHandler putRequestHandler;
    private DeleteRequestHandler deleteRequestHandler;

    @BeforeEach
    public void setUp() throws IOException {
        userService = new UserService();
        requestHandler = new RequestHandler(userService);
        getRequestHandler = new GetRequestHandler(userService);
        postRequestHandler = new PostRequestHandler(userService);
        putRequestHandler = new PutRequestHandler(userService);
        deleteRequestHandler = new DeleteRequestHandler(userService);

        // Erstelle einen Testbenutzer
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /users HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        postRequestHandler.handlePostRequest("/users", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode
        String jsonInputPackage = "[{\"Id\":\"1\", \"Name\":\"TestCard1\", \"Damage\": 10.0}, {\"Id\":\"2\", \"Name\":\"FireTestCard2\", \"Damage\": 50.0}, {\"Id\":\"3\", \"Name\":\"WaterTestCard3\", \"Damage\": 20.0}, {\"Id\":\"4\", \"Name\":\"TestCard4\", \"Damage\": 45.0}, {\"Id\":\"5\", \"Name\":\"FireTestCard5\", \"Damage\": 25.0}]";
        postRequestHandler.handlePostRequest("/packages", new ObjectMapper().readTree(jsonInputPackage), "Bearer admin-mtcgToken\n");

    }

    @AfterEach
    public void tearDown() {
        try (Connection conn = Database.getInstance().connect()) {
            // Lösche den Testbenutzer
            String deleteUserSql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql)) {
                deleteUserStmt.setString(1, "testUser");
                deleteUserStmt.executeUpdate();
            }

            // Lösche alle Pakete
            String deletePackagesSql = "DELETE FROM packages WHERE package_id = ?";
            try (PreparedStatement deletePackagesStmt = conn.prepareStatement(deletePackagesSql)) {
                deletePackagesStmt.setObject(1, UUID.fromString("00000000-0000-0000-0000-000000000001"));
                deletePackagesStmt.executeUpdate();
            }

            // Lösche auch Karten, die zu den Paketen gehören
            String deleteCardsSql = "DELETE FROM cards WHERE card_id IN (?, ?, ?, ?, ?)";
            try (PreparedStatement deleteCardsStmt = conn.prepareStatement(deleteCardsSql)) {
                deleteCardsStmt.setString(1, "1");
                deleteCardsStmt.setString(2, "2");
                deleteCardsStmt.setString(3, "3");
                deleteCardsStmt.setString(4, "4");
                deleteCardsStmt.setString(5, "5");
                deleteCardsStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandlePostUserCreation() throws IOException {
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /users HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = postRequestHandler.handlePostRequest("/users", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals(postRequestHandler.createJsonResponse("409 Conflict", "User already exists"), response); // Erwartet, dass der Benutzer bereits existiert
    }

    @Test
    public void testHandlePostUserLogin() throws IOException {
        String jsonInput = "{\"Username\":\"testUser\",\"Password\":\"testPass\"}";
        BufferedReader in = new BufferedReader(new StringReader("POST /sessions HTTP/1.1\r\nContent-Length: " + jsonInput.length() + "\r\n\r\n" + jsonInput));
        String response = postRequestHandler.handlePostRequest("/sessions", new ObjectMapper().readTree(jsonInput), null); // Übergebe das JsonNode
        assertEquals(postRequestHandler.createJsonResponse("200 OK", "Login successful\", \"token\": \"testUser-mtcgToken"), response);
    }

    @Test
    public void testGetUserFromDatabase() throws SQLException {
        User user = userService.getUserFromDatabase("testUser");
        assertNotNull(user, "User should be retrieved from database");
        assertEquals("testUser", user.getUsername(), "Username should match testUser");
    }

    @Test
    public void testHandleBuyPackage() throws Exception {
        String authorization = "Bearer testUser-mtcgToken";
        User user = userService.getUserFromDatabase("testUser");
        String response = postRequestHandler.handlePostRequest("/transactions/packages", null, authorization);
        assertEquals(postRequestHandler.createJsonResponse("201 OK", "Package acquired!"), response, "Response should be 201 OK");
        String failedResponse = postRequestHandler.handlePostRequest("/transactions/packages", null, authorization);
        assertEquals(postRequestHandler.createJsonResponse("404 Not found", "No packages available"), failedResponse, "Response should be 404 - No Packages available");

        // Confirm coins were deducted
        User updatedUser = userService.getUserFromDatabase("testUser");
        assertTrue(updatedUser.getCoins() < user.getCoins(), "Coins should be deducted after purchase");

    }

    @Test
    public void testPrintCardStack() throws SQLException {
        postRequestHandler.handlePostRequest("/transactions/packages", null, "Bearer testUser-mtcgToken");
        String response = getRequestHandler.handleGetRequest("/cards", "Bearer testUser-mtcgToken");
        assertEquals(response, getRequestHandler.createJsonResponse("200 OK", "{\r\n" +
                "  \"username\":\"testUser\",\r\n" +
                "  \"cards\": [\r\n" +
                "    {\r\n" +
                "      \"card_id\":\"1\",\r\n" +
                "      \"name\":\"TestCard1\",\r\n" +
                "      \"damage\":10.0,\r\n" +
                "      \"element_type\":\"NORMAL\",\r\n" +
                "      \"card_type\":\"SPELL\"\r\n" +
                "    },\r\n" +
                "    {\r\n" +
                "      \"card_id\":\"2\",\r\n" +
                "      \"name\":\"FireTestCard2\",\r\n" +
                "      \"damage\":50.0,\r\n" +
                "      \"element_type\":\"FIRE\",\r\n" +
                "      \"card_type\":\"SPELL\"\r\n" +
                "    },\r\n" +
                "    {\r\n" +
                "      \"card_id\":\"3\",\r\n" +
                "      \"name\":\"WaterTestCard3\",\r\n" +
                "      \"damage\":20.0,\r\n" +
                "      \"element_type\":\"WATER\",\r\n" +
                "      \"card_type\":\"SPELL\"\r\n" +
                "    },\r\n" +
                "    {\r\n" +
                "      \"card_id\":\"4\",\r\n" +
                "      \"name\":\"TestCard4\",\r\n" +
                "      \"damage\":45.0,\r\n" +
                "      \"element_type\":\"NORMAL\",\r\n" +
                "      \"card_type\":\"SPELL\"\r\n" +
                "    },\r\n" +
                "    {\r\n" +
                "      \"card_id\":\"5\",\r\n" +
                "      \"name\":\"FireTestCard5\",\r\n" +
                "      \"damage\":25.0,\r\n" +
                "      \"element_type\":\"FIRE\",\r\n" +
                "      \"card_type\":\"SPELL\"\r\n" +
                "    }\r\n" +
                "  ]\r\n" +
                "}"));
    }


    @Test
    public void testHandlePutUserUpdate() throws IOException {
        // Jetzt aktualisiere die Benutzerdaten
        String jsonInputUpdate = "{\"Name\":\"Updated Name\",\"Bio\":\"Updated bio.\",\"Image\":\"updated_image_url.jpg\"}";
        BufferedReader inUpdate = new BufferedReader(new StringReader("PUT /users/testUser HTTP/1.1\r\nContent-Length: " + jsonInputUpdate.length() + "\r\n\r\n" + jsonInputUpdate));
        String response = putRequestHandler.handlePutRequest("/users/testUser", new ObjectMapper().readTree(jsonInputUpdate)); // Übergebe das JsonNode

        // Prüfe, ob die Antwort den gewünschten Status zurückgibt
        assertEquals(putRequestHandler.createJsonResponse("204 No Content", "User data updated"), response); // Erwartet, dass das Update erfolgreich ist
    }
}
