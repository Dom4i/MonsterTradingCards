package com.yourpackage.server;

import java.io.*;
import java.net.*;

public class Server {

    private ServerSocket serverSocket;
//sadfds
    public void start() throws IOException {
        serverSocket = new ServerSocket(10001);
        System.out.println("Server listening on port 10001");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            // Handle client in a new thread
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            // Read request
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            // Split the request line into method, path, and HTTP version
            if (requestLine != null && !requestLine.isEmpty()) {
                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0]; // e.g., "GET", "POST"
                String path = requestParts[1];   // e.g., "/users"
                System.out.println("Method: " + method);  // Debug-Ausgabe
                System.out.println("Path: " + path);      // Debug-Ausgabe

                // Handle the request using RequestHandler
                String response = RequestHandler.handleRequest(method, path, clientSocket, in);
                System.out.println("Response: " + response);  // Debug-Ausgabe

                // Write response
                OutputStream output = clientSocket.getOutputStream();
                String httpResponse = response + "\r\n";
                output.write(httpResponse.getBytes("UTF-8"));
                output.flush();
            }

            // Close connection
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}