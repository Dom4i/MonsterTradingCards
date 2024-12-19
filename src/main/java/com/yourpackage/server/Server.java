package com.yourpackage.server;

import com.yourpackage.models.UserService;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private UserService userService;
    private RequestHandler requestHandler;
    private ExecutorService threadPool;

    public Server(int port, int threadPoolSize) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port + "\n");
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port);
            e.printStackTrace();
        }

        userService = new UserService();
        requestHandler = new RequestHandler(userService);
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() throws IOException {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            } catch (IOException e) {
                throw e; // Weiterwerfen der IOException
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            // Read request
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String requestLine = in.readLine();
            //System.out.println("Request: " + requestLine);

            // Split the request line into method, path, and HTTP version
            if (requestLine != null && !requestLine.isEmpty()) {
                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0]; // e.g., "GET", "POST"
                String path = requestParts[1];   // e.g., "/users"
                //System.out.println("Method: " + method);
                //System.out.println("Path: " + path);

                // Handle the request using RequestHandler
                String response = requestHandler.handleRequest(method, path, clientSocket, in);
                System.out.println(response);

                // Write response
                OutputStream output = clientSocket.getOutputStream();
                String httpResponse = response + "\r\n";
                output.write(httpResponse.getBytes("UTF-8"));
                output.flush();
            }

            // Close connection
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client request");
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.err.println("Error stopping the server");
            e.printStackTrace();
        }
    }
}
