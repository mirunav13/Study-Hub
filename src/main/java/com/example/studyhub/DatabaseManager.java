package com.example.studyhub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:studyhub.db";

    public static void initDB() {
        String sqlStats = "CREATE TABLE IF NOT EXISTS user_stats (id INTEGER PRIMARY KEY, xp INTEGER);";
        String sqlTasks = "CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, status TEXT);";

        String sqlProgress = "CREATE TABLE IF NOT EXISTS user_progress (id INTEGER PRIMARY KEY, total_tasks INTEGER, total_minutes INTEGER);";
        String sqlAchievements = "CREATE TABLE IF NOT EXISTS achievements (name TEXT PRIMARY KEY, description TEXT, is_unlocked INTEGER);";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlStats);
            stmt.execute(sqlTasks);
            stmt.execute(sqlProgress);
            stmt.execute(sqlAchievements);

            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM user_stats");
            if (rs.getInt(1) == 0) stmt.execute("INSERT INTO user_stats (id, xp) VALUES (1, 0)");

            ResultSet rsProg = stmt.executeQuery("SELECT count(*) FROM user_progress");
            if (rsProg.getInt(1) == 0) stmt.execute("INSERT INTO user_progress (id, total_tasks, total_minutes) VALUES (1, 0, 0)");

            String[] initialBadges = {
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('First Study Session', 'Complete your very first Pomodoro session.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Marathon', 'Study for a total of 120 minutes.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Deep Work', 'Study for a total of 500 minutes.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Time Lord', 'Study for a total of 1000 minutes.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Task Master', 'Complete 10 tasks.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Productivity Ninja', 'Complete 50 tasks.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Task Annihilator', 'Complete 100 tasks.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Rising Star', 'Reach Level 5.', 0)",
                    "INSERT OR IGNORE INTO achievements (name, description, is_unlocked) VALUES ('Scholar', 'Reach Level 10.', 0)"
            };

            for (String query : initialBadges) {
                stmt.execute(query);
            }
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }

    public static int loadXP() {
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT xp FROM user_stats WHERE id = 1")) {
            if (rs.next()) return rs.getInt("xp");
        } catch (SQLException e) { System.out.println("Error loading XP: " + e.getMessage()); }
        return 0;
    }

    public static void saveXP(int xp) {
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement("UPDATE user_stats SET xp = ? WHERE id = 1")) {
            pstmt.setInt(1, xp); pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("Error saving XP: " + e.getMessage()); }
    }

    public static void updateTaskStatus(String name, String status) {
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO tasks (name, status) VALUES (?, ?)")) {
            pstmt.setString(1, name); pstmt.setString(2, status); pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("Error saving task: " + e.getMessage()); }
    }

    public static List<String> loadTasksByStatus(String status) {
        List<String> tasks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM tasks WHERE status = ?")) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) tasks.add(rs.getString("name"));
        } catch (SQLException e) { System.out.println("Error loading tasks: " + e.getMessage()); }
        return tasks;
    }

    public static void addCompletedTaskCount(int count) {
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE user_progress SET total_tasks = total_tasks + " + count + " WHERE id = 1");
        } catch (SQLException e) { System.out.println("Error updating tasks count: " + e.getMessage()); }
    }

    public static void addStudiedMinutes(int minutes) {
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE user_progress SET total_minutes = total_minutes + " + minutes + " WHERE id = 1");
        } catch (SQLException e) { System.out.println("Error updating minutes count: " + e.getMessage()); }
    }

    public static int getStat(String columnName) {
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT " + columnName + " FROM user_progress WHERE id = 1")) {
            if (rs.next()) return rs.getInt(columnName);
        } catch (SQLException e) { System.out.println("Error getting stat: " + e.getMessage()); }
        return 0;
    }

    public static boolean isAchievementUnlocked(String name) {
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement("SELECT is_unlocked FROM achievements WHERE name = ?")) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("is_unlocked") == 1;
        } catch (SQLException e) { System.out.println("Error checking achievement: " + e.getMessage()); }
        return false;
    }

    public static void unlockAchievement(String name) {
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement("UPDATE achievements SET is_unlocked = 1 WHERE name = ?")) {
            pstmt.setString(1, name); pstmt.executeUpdate();
        } catch (SQLException e) { System.out.println("Error unlocking achievement: " + e.getMessage()); }
    }

    public static String getAllAchievementsText() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM achievements")) {
            while (rs.next()) {
                String status = rs.getInt("is_unlocked") == 1 ? "✅ UNLOCKED" : "🔒 LOCKED";
                sb.append(status).append(" - ").append(rs.getString("name"))
                        .append("\n    ").append(rs.getString("description")).append("\n\n");
            }
        } catch (SQLException e) { return "Could not load achievements."; }
        return sb.toString();
    }
}