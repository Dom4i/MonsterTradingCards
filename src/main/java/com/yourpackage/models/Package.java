package com.yourpackage.models;
import com.yourpackage.database.Database;
import java.sql.*;
import java.util.UUID; //für UUID


import java.io.IOException; // Importiere IOException
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


    public class Package {
        private UUID packageId;
        private List<Card> cards;

        public Package(UUID packageId) {
            this.packageId = packageId;
        }

        public boolean addPackageToDatabase() {
            try (Connection conn = Database.connect()) {
                String insertPackageSql = "INSERT INTO packages (package_id) VALUES (?)";
                try (PreparedStatement insertPackageStmt = conn.prepareStatement(insertPackageSql)) {
                    insertPackageStmt.setObject(1, this.packageId); // setObject für UUID verwenden
                    insertPackageStmt.executeUpdate(); // Verwende executeUpdate für INSERT-Anfragen
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false; // Fehler bei der Datenbankoperation
            }
            return true; // Paket erfolgreich erstellt
        }



        // Getter und Setter
        public List<Card> getCards() {
            return cards;
        }

        public UUID getPackageId() {
            return packageId;
        }

    }


