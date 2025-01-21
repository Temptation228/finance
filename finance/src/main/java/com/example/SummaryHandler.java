package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class SummaryHandler implements HttpHandler {
    private final DatabaseManager databaseManager;
    private final Gson gson;

    public SummaryHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); 
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            JsonObject requestJson = gson.fromJson(requestBody, JsonObject.class);
            String uuid = requestJson.has("uuid") ? requestJson.get("uuid").getAsString() : null;
            int walletId = requestJson.has("walletId") ? requestJson.get("walletId").getAsInt() : -1;

            List<Integer> categoryIds = new ArrayList<>();
            if (requestJson.has("categoryIds")) {
                JsonArray categoryIdsArray = requestJson.getAsJsonArray("categoryIds");
                for (int i = 0; i < categoryIdsArray.size(); i++) {
                    categoryIds.add(categoryIdsArray.get(i).getAsInt());
                }
            }

            Map<String, Object> response = new HashMap<>();

            if (walletId != -1 && categoryIds.isEmpty()) {
                Map<String, Double> summary = databaseManager.getWalletSummary(uuid, walletId);
                response.put("summary", summary);

            } else if (walletId != -1 && !categoryIds.isEmpty()) {
                Map<String, Double> summary = databaseManager.getCategorySummary(uuid, walletId, categoryIds);
                response.put("summary", summary);

            } else {
                response.put("error", "Invalid request parameters.");
            }

            sendResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Internal server error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(errorResponse));
        }
    }
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = responseBody.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
