package com.yourpackage.models;

import java.util.ArrayList;
import java.util.List;

public class BattleLog {
    private final List<String> logs;

    public BattleLog() {
        this.logs = new ArrayList<>();
    }

    // Methode, um einen neuen Log-Eintrag hinzuzufügen
    public void addEntry(String entry) {
        logs.add(entry);
    }

    // Methode, um alle Logs zurückzugeben
    public List<String> getLogs() {
        return logs;
    }

    // Methode zum Leeren des BattleLogs
    public void clear() {
        logs.clear();
    }
}
