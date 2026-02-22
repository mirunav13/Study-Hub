package com.example.studyhub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:studyhub.db";

    public static void initDB() {
        String sqlStats = "CREATE TABLE IF NOT EXISTS user_stats (id INTEGER PRIMARY KEY, xp INTEGER);";
        String sqlTasks = "CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, status TEXT);";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlStats);
            stmt.execute(sqlTasks);

            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user_stats");
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO user_stats (id, xp) VALUES (1, 0)");
            }
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }


    public static int loadXP() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT xp FROM user_stats WHERE id = 1")) {
            if (rs.next()) {
                return rs.getInt("xp");
            }
        } catch (SQLException e) {
            System.out.println("Error loading XP: " + e.getMessage());
        }
        return 0;
    }

    public static void saveXP(int xp) {
        String sql = "UPDATE user_stats SET xp = ? WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, xp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving XP: " + e.getMessage());
        }
    }


    public static void saveTask(String name, String status) {
        String sql = "INSERT OR REPLACE INTO tasks (name, status) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving task: " + e.getMessage());
        }
    }

    public static void updateTaskStatus(String name, String status) {
        saveTask(name, status);
    }

    public static List<String> loadTasksByStatus(String status) {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT name FROM tasks WHERE status = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
        return tasks;
    }
}
