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

public class CategoryHandler implements HttpHandler {
    private final DatabaseManager databaseManager;
    private final Gson gson = new Gson();

    public CategoryHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleCreateCategory(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            handleGetCategories(exchange);
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            handleUpdateCategoryBudget(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleCreateCategory(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
    
        String uuid = json.get("uuid").getAsString();
        int walletId = json.get("walletId").getAsInt();
        String name = json.get("name").getAsString();
        double budget = json.get("budget").getAsDouble();
    
        boolean success = databaseManager.createCategory(uuid, walletId, name, budget);
    
        JsonObject responseJson = new JsonObject();
        if (success) {
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Категория успешно создана.");
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Ошибка при создании категории.");
        }
    
        sendJsonResponse(exchange, responseJson.toString());
    }

    private void handleGetCategories(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = WalletHandler.parseQueryParams(query);
    
        String uuid = queryParams.get("uuid");
        int walletId = Integer.parseInt(queryParams.get("walletId"));
    
        List<Map<String, Object>> categories = databaseManager.getCategoriesByWalletId(uuid, walletId);
    
        JsonObject responseJson = new JsonObject();
        if (categories.isEmpty()) {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Категории не найдены.");
        } else {
            responseJson.addProperty("status", "success");
            responseJson.add("categories", JsonParser.parseString(gson.toJson(categories)));
        }
    
        sendJsonResponse(exchange, responseJson.toString());
    }

    
    private void handleUpdateCategoryBudget(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

        String uuid = json.get("uuid").getAsString();
        int categoryId = json.get("categoryId").getAsInt();
        double newBudget = json.get("newBudget").getAsDouble();

        boolean success = databaseManager.updateCategoryBudget(uuid, categoryId, newBudget);

        JsonObject responseJson = new JsonObject();
        if (success) {
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Бюджет категории успешно обновлен.");
        } else {
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Не удалось обновить бюджет категории.");
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
