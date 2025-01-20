package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final String url = "jdbc:sqlite:finance.db";

    public DatabaseManager() {
        createNewDatabase();
    }

    private void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String createUsersTable = "CREATE TABLE IF NOT EXISTS users (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " username TEXT NOT NULL UNIQUE,\n"
                        + " password TEXT NOT NULL,\n"
                        + " uuid TEXT NOT NULL UNIQUE\n"
                        + ");";

                String createWalletsTable = "CREATE TABLE IF NOT EXISTS wallets (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " user_id INTEGER NOT NULL,\n"
                        + " name TEXT NOT NULL,\n"
                        + " balance REAL DEFAULT 0,\n"
                        + " FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE\n"
                        + ");";

                String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " user_id INTEGER NOT NULL,\n"
                        + " wallet_id INTEGER NOT NULL,\n"
                        + " name TEXT NOT NULL,\n"
                        + " budget REAL NOT NULL DEFAULT 0,\n"
                        + " remaining_budget REAL NOT NULL DEFAULT 0,\n"
                        + " FOREIGN KEY(wallet_id) REFERENCES wallets(id) ON DELETE CASCADE\n"
                        + ");";

                String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " category_id INTEGER NOT NULL,\n"
                        + " wallet_id INTEGER NOT NULL,\n"
                        + " user_id INTEGER NOT NULL,\n"
                        + " type TEXT NOT NULL CHECK(type IN ('income', 'expense')),\n"
                        + " amount REAL NOT NULL,\n"
                        + " description TEXT,\n"
                        + " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
                        + " FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,\n"
                        + " FOREIGN KEY(wallet_id) REFERENCES wallets(id) ON DELETE CASCADE\n"
                        + ");";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createUsersTable);
                    stmt.execute(createWalletsTable);
                    stmt.execute(createCategoriesTable);
                    stmt.execute(createTransactionsTable);
                }
                System.out.println("База данных успешно создана!");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании БД: " + e.getMessage());
        }
    }


    public String registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, uuid) VALUES (?, ?, ?)";
        String userUuid = UUID.randomUUID().toString();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userUuid);
            pstmt.executeUpdate();
            System.out.println("Пользователь зарегистрирован: " + username);
            return userUuid; 
        } catch (SQLException e) {
            System.err.println("Ошибка при регистрации пользователя: " + e.getMessage());
            return null;
        }
    }

    public String authenticateUser(String username, String password) {
        String sql = "SELECT uuid FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("uuid");
            } else {
                System.out.println("Ошибка аутентификации: пользователь не найден.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при аутентификации пользователя: " + e.getMessage());
            return null;
        }
    }

    public boolean createWallet(String uuid, String walletName, double initialBalance) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String insertWalletSql = "INSERT INTO wallets (user_id, name, balance) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
            PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
            PreparedStatement insertWalletStmt = conn.prepareStatement(insertWalletSql)) {

            getUserIdStmt.setString(1, uuid);
            ResultSet rs = getUserIdStmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");

                insertWalletStmt.setInt(1, userId);
                insertWalletStmt.setString(2, walletName);
                insertWalletStmt.setDouble(3, initialBalance);
                insertWalletStmt.executeUpdate();

                return true;
            } else {
                return false; 
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании кошелька: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getWalletsByUUID(String uuid) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String getWalletsSql = "SELECT id, name, balance FROM wallets WHERE user_id = ?";
    
        List<Map<String, Object>> wallets = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement getWalletsStmt = conn.prepareStatement(getWalletsSql)) {
    
            getUserIdStmt.setString(1, uuid);
            ResultSet rsUser = getUserIdStmt.executeQuery();
    
            if (rsUser.next()) {
                int userId = rsUser.getInt("id");
    
                getWalletsStmt.setInt(1, userId);
                ResultSet rsWallets = getWalletsStmt.executeQuery();
    
                while (rsWallets.next()) {
                    Map<String, Object> wallet = new HashMap<>();
                    wallet.put("id", rsWallets.getInt("id"));
                    wallet.put("name", rsWallets.getString("name"));
                    wallet.put("balance", rsWallets.getDouble("balance"));
                    wallets.add(wallet);
                }
    
                return wallets;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении кошельков: " + e.getMessage());
            return null;
        }
    }

    public boolean createCategory(String uuid, int walletId, String name, double budget) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String sql = "INSERT INTO categories (user_id, wallet_id, name, budget, remaining_budget) VALUES (?, ?, ?, ?, ?)";
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            getUserIdStmt.setString(1, uuid);
            ResultSet rs = getUserIdStmt.executeQuery();
    
            if (rs.next()) {
                int userId = rs.getInt("id");
    
                pstmt.setInt(1, userId);
                pstmt.setInt(2, walletId);
                pstmt.setString(3, name);
                pstmt.setDouble(4, budget);
                pstmt.setDouble(5, budget);
                pstmt.executeUpdate();
                return true;
            } else {
                System.err.println("Ошибка: пользователь не найден.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании категории: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getCategoriesByWalletId(String uuid, int walletId) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String getCategoriesSql = "SELECT id, name, budget, remaining_budget FROM categories WHERE user_id = ? AND wallet_id = ?";
    
        List<Map<String, Object>> categories = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement pstmt = conn.prepareStatement(getCategoriesSql)) {
    
            getUserIdStmt.setString(1, uuid);
            ResultSet rsUser = getUserIdStmt.executeQuery();
    
            if (rsUser.next()) {
                int userId = rsUser.getInt("id");
                pstmt.setInt(1, userId);
                pstmt.setInt(2, walletId);
                ResultSet rs = pstmt.executeQuery();
    
                while (rs.next()) {
                    Map<String, Object> category = new HashMap<>();
                    category.put("id", rs.getInt("id"));
                    category.put("name", rs.getString("name"));
                    category.put("budget", rs.getDouble("budget"));
                    category.put("remainingBudget", rs.getDouble("remaining_budget"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении категорий: " + e.getMessage());
        }
        return categories;
    }

    public boolean updateCategoryBudget(String uuid, int categoryId, double newBudget) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String sql = "UPDATE categories SET budget = ?, remaining_budget = ? WHERE id = ? AND user_id = ?";
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            getUserIdStmt.setString(1, uuid);
            ResultSet rs = getUserIdStmt.executeQuery();
    
            if (rs.next()) {
                int userId = rs.getInt("id");
    
                pstmt.setDouble(1, newBudget);
                pstmt.setDouble(2, newBudget);
                pstmt.setInt(3, categoryId);
                pstmt.setInt(4, userId);
                int rowsUpdated = pstmt.executeUpdate();
                return rowsUpdated > 0;
            } else {
                System.err.println("Ошибка: пользователь не найден.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении бюджета категории: " + e.getMessage());
            return false;
        }
    }

    public boolean createTransaction(String uuid, int categoryId, int walletId, String type, double amount, String description) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String insertTransaction = "INSERT INTO transactions (category_id, wallet_id, user_id, type, amount, description) VALUES (?, ?, ?, ?, ?, ?)";
        String updateWalletBalance = "UPDATE wallets SET balance = balance + ? WHERE id = ?";
        String updateCategoryBudget = "UPDATE categories SET remaining_budget = remaining_budget + ? WHERE id = ?";
    
        double adjustedAmount = type.equals("income") ? amount : -amount;
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertTransaction);
             PreparedStatement updateWalletStmt = conn.prepareStatement(updateWalletBalance);
             PreparedStatement updateCategoryStmt = conn.prepareStatement(updateCategoryBudget)) {
    
            conn.setAutoCommit(false);
    
            getUserIdStmt.setString(1, uuid);
            ResultSet rs = getUserIdStmt.executeQuery();
    
            if (rs.next()) {
                int userId = rs.getInt("id");
    
                insertStmt.setInt(1, categoryId);
                insertStmt.setInt(2, walletId);
                insertStmt.setInt(3, userId);
                insertStmt.setString(4, type);
                insertStmt.setDouble(5, amount);
                insertStmt.setString(6, description);
                insertStmt.executeUpdate();
    
                updateWalletStmt.setDouble(1, adjustedAmount);
                updateWalletStmt.setInt(2, walletId);
                updateWalletStmt.executeUpdate();
    
                updateCategoryStmt.setDouble(1, adjustedAmount);
                updateCategoryStmt.setInt(2, categoryId);
                updateCategoryStmt.executeUpdate();
    
                conn.commit();
                return true;
            } else {
                System.err.println("Ошибка: пользователь не найден.");
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании транзакции: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getTransactionsByWalletId(String uuid, int walletId) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String sql = "SELECT t.id, t.type, t.amount, t.description, t.date, "
                   + "c.name AS category_name "
                   + "FROM transactions t "
                   + "JOIN categories c ON t.category_id = c.id "
                   + "WHERE t.wallet_id = ? AND t.user_id = ? "
                   + "ORDER BY t.date DESC";
    
        List<Map<String, Object>> transactions = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            getUserIdStmt.setString(1, uuid);
            ResultSet rsUser = getUserIdStmt.executeQuery();
    
            if (rsUser.next()) {
                int userId = rsUser.getInt("id");
                pstmt.setInt(1, walletId);
                pstmt.setInt(2, userId);
                ResultSet rs = pstmt.executeQuery();
    
                while (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", rs.getInt("id"));
                    transaction.put("type", rs.getString("type"));
                    transaction.put("amount", rs.getDouble("amount"));
                    transaction.put("description", rs.getString("description"));
                    transaction.put("date", rs.getString("date"));
                    transaction.put("category", rs.getString("category_name"));
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении транзакций: " + e.getMessage());
        }
        return transactions;
    }

    public List<Map<String, Object>> getTransactionsByCategoryId(String uuid, int categoryId) {
        String getUserIdSql = "SELECT id FROM users WHERE uuid = ?";
        String sql = "SELECT t.id, t.type, t.amount, t.description, t.date "
                   + "FROM transactions t "
                   + "WHERE t.category_id = ? AND t.user_id = ? "
                   + "ORDER BY t.date DESC";
    
        List<Map<String, Object>> transactions = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            getUserIdStmt.setString(1, uuid);
            ResultSet rsUser = getUserIdStmt.executeQuery();
    
            if (rsUser.next()) {
                int userId = rsUser.getInt("id");
                pstmt.setInt(1, categoryId);
                pstmt.setInt(2, userId);
                ResultSet rs = pstmt.executeQuery();
    
                while (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", rs.getInt("id"));
                    transaction.put("type", rs.getString("type"));
                    transaction.put("amount", rs.getDouble("amount"));
                    transaction.put("description", rs.getString("description"));
                    transaction.put("date", rs.getString("date"));
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении транзакций: " + e.getMessage());
        }
        return transactions;
    }
}
