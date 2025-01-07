package com.yourpackage.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BattleLogTest {

    @Test
    public void testAddEntry() {
        BattleLog battleLog = new BattleLog();

        // Log-Eintrag hinzufügen
        battleLog.addEntry("Player 1 attacks Player 2");

        // Überprüfen, ob der Log-Eintrag hinzugefügt wurde
        assertEquals(1, battleLog.getLogs().size());
        assertEquals("Player 1 attacks Player 2", battleLog.getLogs().get(0));
    }

    @Test
    public void testGetLogs() {
        BattleLog battleLog = new BattleLog();

        // Mehrere Einträge hinzufügen
        battleLog.addEntry("Player 1 attacks Player 2");
        battleLog.addEntry("Player 2 counterattacks Player 1");

        // Alle Logs abrufen und sicherstellen, dass sie korrekt sind
        List<String> logs = battleLog.getLogs();
        assertEquals(2, logs.size());
        assertEquals("Player 1 attacks Player 2", logs.get(0));
        assertEquals("Player 2 counterattacks Player 1", logs.get(1));
    }
}
