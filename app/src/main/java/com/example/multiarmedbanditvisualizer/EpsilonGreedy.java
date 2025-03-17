package com.example.multiarmedbanditvisualizer; // Paketdeklaration

import java.util.ArrayList; // Für dynamische Arrays
import java.util.List;      // Für die Listenschnittstelle
import java.util.Random;    // Für Zufallszahlen

public class EpsilonGreedy { // Steuert den Epsilon-Greedy-Algorithmus

    private int numArms; // Anzahl der Banditen
    private double epsilon; // Wahrscheinlichkeit für Exploration
    private int limit; // Iterationslimit pro Bandit

    private double[] trueRates; // Die echten Gewinnwahrscheinlichkeiten (unbekannt für den Algorithmus)
    private int[] armCounts;    // Anzahl der Ziehungen pro Bandit
    private int[] armSuccesses; // Anzahl der Erfolge pro Bandit

    private boolean experimentFinished = false; // Flag: Experiment beendet
    private boolean finalRowAppended = false;     // Flag: Finale Wahrscheinlichkeitszeile bereits hinzugefügt

    private Random random = new Random(); // Zufallszahlengenerator

    // Klasse für eine Zeile in der Log-Tabelle
    public static class LogRow {
        public String firstCol; // Erste Spalte (Zeitpunkt)
        public String[] values; // Formatierte Erfolgszahlen je Bandit
        public String[] colors; // Grün bei Erfolg, rot bei Misserfolg, schwarz  sonst

        public LogRow(int arms) { // Konstruktor: Initialisiert Arrays basierend auf der Anzahl der Banditen
            values = new String[arms];
            colors = new String[arms];
        }
    }
    private List<LogRow> logRows = new ArrayList<>(); // Liste aller Log-Zeilen
    private int lastChosenArm = -1; // Index des zuletzt gewählten Banditen


    // Konstruktor: Initialisiert den Algorithmus mit Anzahl der Banditen, Epsilon und Limit
    public EpsilonGreedy(int numArms, double epsilon, int limit) {
        this.numArms = numArms; // Speichert die Anzahl der Banditen
        this.epsilon = epsilon; // Speichert den Epsilon-Wert
        this.limit = limit;     // Speichert das Iterationslimit

        trueRates = new double[numArms]; // Array für die wahren Wahrscheinlichkeiten
        armCounts = new int[numArms];    // Array für die Ziehungsanzahl
        armSuccesses = new int[numArms]; // Array für die Erfolgsanzahl

        for (int i = 0; i < numArms; i++) { // Generiere für jeden Banditen eine zufällige Wahrscheinlichkeit zwischen 0.1 und 0.9
            double r = 0.1 + 0.8 * random.nextDouble();
            trueRates[i] = r;
        }
        doInitialization(); // Starte die Initialisierungsphase
    }

    // Initialisierungsphase: Testet jeden Banditen mindestens einmal
    private void doInitialization() {
        int initRound = 1; // Zähler der Initialisierungsrunden
        while (true) {
            // Falls ein Bandit das Limit erreicht hat, Experiment beenden
            for (int arm = 0; arm < numArms; arm++) {
                if (armCounts[arm] >= limit) {
                    experimentFinished = true;
                    appendFinalProbRow();
                    return;
                }
            }
            LogRow row = new LogRow(numArms); // Erstelle eine neue Log-Zeile
            row.firstCol = "Initialization " + initRound; // Beschrifte die Zeile
            boolean anySuccess = false; // Flag: Mindestens ein Erfolg erzielt?

            for (int arm = 0; arm < numArms; arm++) { // Teste jeden Banditen einmal
                boolean success = (random.nextDouble() < trueRates[arm]); // Simuliere Erfolg
                armCounts[arm]++; // Erhöhe die Ziehungsanzahl
                if (success) {
                    armSuccesses[arm]++; // Erhöhe die Erfolgsanzahl
                    anySuccess = true;
                }
                row.values[arm] = formatValue(arm); // Formatiere den aktuellen Wert
                row.colors[arm] = success ? "GREEN" : "RED"; // Setze Farbe: GRÜN bei Erfolg, ROT bei Misserfolg

                // Falls das Limit erreicht wurde, beende das Experiment
                if (armCounts[arm] >= limit) {
                    logRows.add(row);
                    experimentFinished = true;
                    appendFinalProbRow();
                    return;
                }
            }
            logRows.add(row); // Füge die Zeile der Log-Liste hinzu

            if (anySuccess) { // Falls mindestens ein Erfolg erzielt wurde, beende die Initialisierung
                break;
            } else { // Sonst wiederhole mit neuer Initialisierungsrunde
                initRound++;
            }
        }
    }

    // Führt einen Zeitschritt (eine Iteration) aus und entscheidet zwischen Exploration und Exploitation
    public boolean doTimeStep() {
        if (experimentFinished) return false; // Wenn das Experiment beendet ist, nichts tun

        // Prüfe, ob ein Bandit das Limit erreicht hat
        for (int arm = 0; arm < numArms; arm++) {
            if (armCounts[arm] >= limit) {
                experimentFinished = true;
                appendFinalProbRow();
                return false;
            }
        }

        double r = random.nextDouble(); // Generiere einen Zufallswert
        boolean explore = (r <= epsilon); // Entscheide: Exploration (wenn r <= epsilon) oder Exploitation
        int chosen = (explore) ? random.nextInt(numArms) : bestArm(); // Wähle zufällig oder den besten Banditen

        lastChosenArm = chosen; // Speichere den gewählten Banditen
        boolean success = (random.nextDouble() < trueRates[chosen]); // Simuliere Erfolg am gewählten Banditen
        armCounts[chosen]++; // Erhöhe Ziehungsanzahl für den gewählten Banditen
        if (success) armSuccesses[chosen]++; // Erhöhe Erfolgsanzahl bei Erfolg

        int stepIndex = getTotalSteps() + 1; // Berechne den aktuellen Schrittindex (ohne Initialisierung)
        String firstC = String.format("%d) %.2f %s ε", stepIndex, r, (explore ? "≤" : ">")); // Formatiere die Zeileneinleitung

        LogRow row = new LogRow(numArms); // Erstelle eine neue Log-Zeile
        row.firstCol = firstC;
        for (int a = 0; a < numArms; a++) { // Für jeden Banditen:
            row.values[a] = formatValue(a); // Füge den formatierten Wert hinzu
            row.colors[a] = (a == chosen) ? (success ? "GREEN" : "RED") : "BLACK"; // Setze Farbe: Gewählter Bandit farbig, sonst SCHWARZ
        }
        logRows.add(row); // Füge die Zeile zur Log-Liste hinzu

        if (armCounts[chosen] >= limit) { // Falls das Limit erreicht wurde, beende das Experiment
            experimentFinished = true;
            appendFinalProbRow();
            return false;
        }
        return true; // Zeitschritt war erfolgreich
    }

    // Bestimmt den aktuell besten Banditen (mit höchster Erfolgsquote)
    private int bestArm() {
        double best = -1; // Initialisiere beste Quote
        List<Integer> ties = new ArrayList<>(); // Liste für Banditen bei Gleichstand
        for (int arm = 0; arm < numArms; arm++) {
            double ratio = (armCounts[arm] == 0) ? 0 : (double) armSuccesses[arm] / armCounts[arm]; // Berechne Erfolgsquote
            if (ratio > best) { // Falls Quote besser als bisher:
                best = ratio; // Aktualisiere beste Quote
                ties.clear(); // Leere Liste
                ties.add(arm); // Füge aktuellen Banditen hinzu
            } else if (ratio == best) { // Bei Gleichstand:
                ties.add(arm); // Füge Bandit hinzu
            }
        }
        return ties.get(random.nextInt(ties.size())); // Zufällige Auswahl bei Gleichstand
    }

    // Formatiert die Erfolgsquote eines Banditen als "X/Y = Z%"
    private String formatValue(int arm) {
        double ratio = (armCounts[arm] == 0) ? 0 : (double) armSuccesses[arm] / armCounts[arm];
        return String.format("%d/%d = %.0f%%", armSuccesses[arm], armCounts[arm], ratio * 100);
    }

    // Gibt zurück, ob das Experiment fortgesetzt werden kann
    public boolean canProceed() {
        return !experimentFinished;
    }

    // Berechnet die Gesamtzahl der durchgeführten Aktionen
    public int getTotalActionsDone() {
        int s = 0;
        for (int c : armCounts) s += c;
        return s;
    }

    // Berechnet die Gesamtzahl der erzielten Erfolge
    public int getTotalSuccesses() {
        int sum = 0;
        for (int s : armSuccesses) {
            sum += s;
        }
        return sum;
    }

    // Ermittelt die Gesamtzahl der Aktionen der Banditen mit der höchsten wahren Gewinnwahrscheinlichkeit
    public int getOptimalActionCount() {
        double best = -1;
        for (double r : trueRates) { // Finde die höchste trueRate
            if (r > best) best = r;
        }
        int sum = 0;
        for (int i = 0; i < numArms; i++) { // Summiere die Aktionen der Banditen mit dieser trueRate
            if (Math.abs(trueRates[i] - best) < 1e-9) {
                sum += armCounts[i];
            }
        }
        return sum;
    }

    // Berechnet die Anzahl der Zeitschritte (ohne Initialisierungs- und finale Zeilen)
    public int getTotalSteps() {
        int stepCount = 0;
        for (LogRow row : logRows) {
            if (row.firstCol.startsWith("Initialization") || row.firstCol.startsWith("Actual"))
                ; // Überspringe diese Zeilen
            else
                stepCount++;
        }
        return stepCount;
    }

    // Gibt den Index des zuletzt gewählten Banditen zurück
    public int getLastChosenArm() {
        return lastChosenArm;
    }

    // Liefert die gesamte Log-Tabelle
    public List<LogRow> getLogRows() {
        return logRows;
    }

    // Gibt die Anzahl der Banditen zurück
    public int getNumArms() {
        return numArms;
    }

    // Gibt das Iterationslimit pro Bandit zurück
    public int getLimit() {
        return limit;
    }

    // Fügt die finale Zeile mit den tatsächlichen Wahrscheinlichkeiten hinzu (nur einmal)
    private void appendFinalProbRow() {
        if (finalRowAppended) return;
        finalRowAppended = true;

        LogRow row = new LogRow(numArms);
        row.firstCol = "Actual probabilities:";
        for (int i = 0; i < numArms; i++) {
            double p = trueRates[i] * 100;
            row.values[i] = String.format("%.0f%%", p);
            row.colors[i] = "BLACK";
        }
        logRows.add(row);
    }

    // Bereitet die Daten für die graphische Darstellung vor
    public float[][] getGraphData() {
        List<Float>[] lists = new ArrayList[numArms];
        for (int i = 0; i < numArms; i++) {
            lists[i] = new ArrayList<>();
        }

        int betCount = 0;
        for (LogRow row : logRows) {
            if (row.firstCol.startsWith("Actual")) continue; // Überspringe finale Zeile
            for (int arm = 0; arm < numArms; arm++) {
                if (!row.colors[arm].equals("BLACK")) { // Nur registrierte Versuche berücksichtigen
                    float val = parseRate(row.values[arm]);
                    lists[arm].add(val);
                }
            }
            betCount++;
        }

        float[][] data = new float[numArms][];
        for (int i = 0; i < numArms; i++) {
            data[i] = new float[lists[i].size()];
            for (int k = 0; k < lists[i].size(); k++) {
                data[i][k] = lists[i].get(k);
            }
        }
        return data;
    }

    // Parst einen formatierten String, um den Erfolgswert als float zu extrahieren
    private float parseRate(String txt) {
        int eq = txt.indexOf('=');
        int pc = txt.indexOf('%');
        if (eq < 0 || pc < 0) return 0f;
        String sub = txt.substring(eq + 1, pc).trim();
        try {
            return Float.parseFloat(sub) / 100f;
        } catch (Exception e) {
            return 0f;
        }
    }
}
