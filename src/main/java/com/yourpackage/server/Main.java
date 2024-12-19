package com.yourpackage.server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        Server server = new Server(10001, 20);
        try {
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
