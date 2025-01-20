package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws IOException {
        DatabaseManager databaseManager = new DatabaseManager();
        RequestController requestController = new RequestController(databaseManager);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", requestController);

        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на порту 8000");
    }
}
