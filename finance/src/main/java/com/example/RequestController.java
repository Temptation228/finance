package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.logging.Logger;

public class RequestController implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(RequestController.class.getName());
    private final RegisterHandler registerHandler;
    private final AuthHandler authHandler;
    private final WalletHandler walletHandler;
    private final CategoryHandler categoryHandler;
    private final TransactionHandler transactionHandler;
    private final SummaryHandler summaryHandler;

    public RequestController(DatabaseManager databaseManager) {
        this.registerHandler = new RegisterHandler(databaseManager);
        this.authHandler = new AuthHandler(databaseManager);
        this.walletHandler = new WalletHandler(databaseManager); 
        this.categoryHandler = new CategoryHandler(databaseManager);  
        this.transactionHandler = new TransactionHandler(databaseManager);  
        this.summaryHandler = new SummaryHandler(databaseManager); 
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOGGER.info("Полученый запрос: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
        CORSHandler.addCORSHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if ("/register".equals(path)) {
            registerHandler.handle(exchange);
        } else if ("/auth".equals(path)) {
            authHandler.handle(exchange);
        } else if ("/wallet".equals(path)) {
            walletHandler.handle(exchange);
        } else if ("/category".equals(path)) { 
            categoryHandler.handle(exchange);
        } else if ("/transaction".equals(path)) {
            transactionHandler.handle(exchange);
        } else if ("/summary".equals(path)) {
            summaryHandler.handle(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}
