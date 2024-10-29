package com.yourpackage.server;
import com.yourpackage.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;


public class RequestHandler {

    private final UserService userService;
    private final GetRequestHandler getRequestHandler;
    private final PostRequestHandler postRequestHandler;
    private final PutRequestHandler putRequestHandler;
    private final DeleteRequestHandler deleteRequestHandler;


    public RequestHandler(UserService userService) {
        this.userService = new UserService();                       // Einmalige Instanzierung
        this.getRequestHandler = new GetRequestHandler(userService);
        this.postRequestHandler = new PostRequestHandler(userService);
        this.putRequestHandler = new PutRequestHandler(userService);
        this.deleteRequestHandler = new DeleteRequestHandler(userService);
    }

    public String handleRequest(String method, String path, Socket clientSocket, BufferedReader in) throws IOException {
        String response;
        String authorization;
        JsonNode jsonNode;
        try {
            String[] parts = readRequest(in);
            String header = parts[0];
            //System.out.println("HEADER: " + header);
            String body = parts[1];
            //System.out.println("BODY: " + body);
            jsonNode = new ObjectMapper().readTree(body);
            authorization = getAuthorizationHeader(header);

        } catch (IOException e) {
            return "HTTP/1.1 400 Bad Request\n\nInvalid JSON format.";
        }
        switch (method) {
            case "GET":
                response = getRequestHandler.handleGetRequest(path, authorization);
                break;
            case "POST":
                response = postRequestHandler.handlePostRequest(path, jsonNode, authorization); // Pass jsonNode
                break;
            case "PUT":
                response = putRequestHandler.handlePutRequest(path, jsonNode); // Pass jsonNode
                break;
            case "DELETE":
                response = "HTTP/1.1 501 Method Not Implemented";
                break;
            default:
                response = "HTTP/1.1 405 Method Not Allowed";
                break;
        }
        return response;
    }

    private String[] readRequest(BufferedReader in) throws IOException {
        StringBuilder requestHeaders = new StringBuilder();
        StringBuilder requestBody = new StringBuilder();

        // Header lesen
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                break; // Ende der Header
            }
            requestHeaders.append(line).append("\n");
        }
        // Content-Length ermitteln
        int contentLength = 0;
        for (String header : requestHeaders.toString().split("\n")) {
            if (header.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(header.substring(16).trim());
            }
        }
        // Body lesen
        if (contentLength > 0) {
            char[] bodyBuffer = new char[contentLength];
            in.read(bodyBuffer, 0, contentLength);
            String body = new String(bodyBuffer);
            requestBody.append(body);
        }

        // Header und Body zurückgeben
        return new String[]{requestHeaders.toString(), requestBody.toString()};
    }

    // Methode zum Auslesen des Authorization-Headers
    private String getAuthorizationHeader(String header) {
        String[] lines = header.split("\n");
        for (String line : lines) {
            if (line.startsWith("Authorization: ")) {
                return line.substring("Authorization: ".length()).trim();
            }
        }
        return null; // Kein Authorization-Header gefunden
    }
}