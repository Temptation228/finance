package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletHandler implements HttpHandler {
    private final DatabaseManager databaseManager;
    private final Gson gson = new Gson();

    public WalletHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleCreateWallet(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            handleGetWallets(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleCreateWallet(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

        String uuid = json.get("uuid").getAsString();
        String walletName = json.get("name").getAsString();
        double initialBalance = json.get("initialBalance").getAsDouble();

        boolean success = databaseManager.createWallet(uuid, walletName, initialBalance);

        JsonObject responseJson = new JsonObject();
        if (success) {
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Кошелек успешно создан.");
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Ошибка при создании кошелька. Возможно, пользователь не найден.");
        }

        sendJsonResponse(exchange, responseJson.toString());
    }

    private void handleGetWallets(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        String uuid = queryParams.get("uuid");

        List<Map<String, Object>> wallets = databaseManager.getWalletsByUUID(uuid);

        JsonObject responseJson = new JsonObject();
        if (wallets != null) {
            responseJson.addProperty("status", "success");
            responseJson.add("wallets", JsonParser.parseString(gson.toJson(wallets)));
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Ошибка при получении кошельков.");
        }

        sendJsonResponse(exchange, responseJson.toString());
    }

    

    private void sendJsonResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
}

