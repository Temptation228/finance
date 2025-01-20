package com.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class RegisterHandler {
    private final DatabaseManager databaseManager;

    public RegisterHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();

        String uuid = databaseManager.registerUser(username, password);

        JsonObject responseJson = new JsonObject();
        if (uuid != null) {
            responseJson.addProperty("status", "success");
            responseJson.addProperty("uuid", uuid);
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Пользователь с таким логином уже существует.");
        }

        String response = responseJson.toString();
        exchange.sendResponseHeaders(200, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

