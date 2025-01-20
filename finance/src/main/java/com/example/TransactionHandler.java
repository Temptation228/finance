package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class TransactionHandler implements HttpHandler {
    private final DatabaseManager databaseManager;
    private final Gson gson = new Gson();

    public TransactionHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleCreateTransaction(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            handleGetTransactions(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleCreateTransaction(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
    
        String uuid = json.get("uuid").getAsString();
        int categoryId = json.get("categoryId").getAsInt();
        int walletId = json.get("walletId").getAsInt();
        String type = json.get("type").getAsString();
        double amount = json.get("amount").getAsDouble();
        String description = json.get("description").getAsString();
    
        boolean success = databaseManager.createTransaction(uuid, categoryId, walletId, type, amount, description);
    
        JsonObject responseJson = new JsonObject();
        if (success) {
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Транзакция успешно добавлена.");
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Ошибка при создании транзакции.");
        }
    
        sendJsonResponse(exchange, responseJson.toString());
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = WalletHandler.parseQueryParams(query);
    
        JsonObject responseJson = new JsonObject();
    
        if (queryParams.containsKey("uuid") && queryParams.containsKey("walletId")) {
            String uuid = queryParams.get("uuid");
            int walletId = Integer.parseInt(queryParams.get("walletId"));
    
            List<Map<String, Object>> transactions = databaseManager.getTransactionsByWalletId(uuid, walletId);
    
            if (!transactions.isEmpty()) {
                responseJson.addProperty("status", "success");
                responseJson.add("transactions", JsonParser.parseString(gson.toJson(transactions)));
            } else {
                responseJson.addProperty("status", "error");
                responseJson.addProperty("message", "Транзакции для кошелька не найдены.");
            }
        } else if (queryParams.containsKey("uuid") && queryParams.containsKey("categoryId")) {
            String uuid = queryParams.get("uuid");
            int categoryId = Integer.parseInt(queryParams.get("categoryId"));
    
            List<Map<String, Object>> transactions = databaseManager.getTransactionsByCategoryId(uuid, categoryId);
    
            if (!transactions.isEmpty()) {
                responseJson.addProperty("status", "success");
                responseJson.add("transactions", JsonParser.parseString(gson.toJson(transactions)));
            } else {
                responseJson.addProperty("status", "error");
                responseJson.addProperty("message", "Транзакции для категории не найдены.");
            }
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
}
