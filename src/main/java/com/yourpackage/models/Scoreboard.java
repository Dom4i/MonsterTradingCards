package com.yourpackage.models;
import java.sql.*;
import com.yourpackage.database.Database;

public class Scoreboard {
    public String print() {
        String sql = "SELECT username, score FROM users ORDER BY score DESC, username ASC";
        StringBuilder scoreboard = new StringBuilder();

        scoreboard.append("\nRank | Username       | Score\n");
        scoreboard.append("----------------------------\n");

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");

                // Formatierte Ausgabe für die Tabelle (Rauskopiert)
                scoreboard.append(String.format("%-4d | %-14s | %d\n", rank, username, score));
                rank++;
            }
            return scoreboard.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Error retrieving scoreboard.";
    }
}
