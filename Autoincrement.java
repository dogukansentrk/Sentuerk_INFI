package Main;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Autoincrement {

    private static final String DB_URL = "jdbc:sqlite:zahlen.db";

    public static void main(String[] args) {
        try (Connection conn = connect()) {
            createTable(conn);           
            int anzahl = userInput();   
            insertRandomValues(conn, anzahl); // Zufallszahlen generieren
            showStats(conn);              
            handleDeleteChoice(conn);    
            showStats(conn);            
        } catch (Exception e) {
            e.printStackTrace();       
        }
    }

    // Verbindung zur SQL-Datenbank
    private static Connection connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Verbindung zur Datenbank hergestellt.");
        return conn;
    }

   // Tabelle WERTE erstellen
    private static void createTable(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS WERTE (
                        id INTEGER PRIMARY KEY AUTOINCREMENT, -- Eindeutige ID, die automatisch hochzählt
                        value INTEGER,                        -- Die eigentliche Zufallszahl
                        value2 INTEGER                        -- Hilfsspalte (0 für gerade, 1 für ungerade)
                    );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Benutzereingabe optimieren nur positive ganze Zahlen annimmt
    private static int userInput() {
        Scanner scanner = new Scanner(System.in);
        int anzahl = 0;
        while (true) {
            System.out.print("Wie viele Zahlen sollen eingefügt werden? ");
            if (scanner.hasNextInt()) {
                anzahl = scanner.nextInt();
                if (anzahl > 0) break;
                System.out.println("Bitte eine Zahl größer als 0 eingeben!");
            } else {
                System.out.println("Bitte eine ganze Zahl eingeben!");
                scanner.next();
            }
        }
        return anzahl;
    }

	// Zufallszahlen einfügen
	private static void insertRandomValues(Connection conn, int anzahl) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM werte;"); // alte Daten löschen
        }

        Random rand = new Random();
        String sql = "INSERT INTO werte(value, value2) VALUES(?, ?)";
        int inserted = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < anzahl; i++) {
                int value = rand.nextInt(10) + 1; 
                int value2 = value % 2;           // Berechnet gerade oder ungerade
                
                pstmt.setInt(1, value);        
                pstmt.setInt(2, value2);     
                pstmt.executeUpdate();
                inserted++;
            }
        }

        if (inserted > 0)
            System.out.println(inserted + " Zahlen wurden eingefügt.");
        else
            System.out.println("Es wurden keine Zahlen eingefügt!");
    }

    // Nutzer wird gefragt ob even oder odd gelöscht werden soll
    private static void handleDeleteChoice(Connection conn) throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Was löschen? (even/odd): ");
        String choice = sc.next().trim().toLowerCase();

        switch (choice) {
            case "even" -> deleteEvenOdd(conn, 0);
            case "odd"  -> deleteEvenOdd(conn, 1);
            default     -> System.out.println("Keine gültige Wahl. Nichts gelöscht.");
        }
    }

    //DELETE-Befehl wird ausgeführt
    private static void deleteEvenOdd(Connection conn, int x) throws SQLException {
        String sql = "DELETE FROM WERTE WHERE value2 = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, x);
            int deleted = ps.executeUpdate();
            System.out.println(deleted + (x == 0 ? " gerade" : " ungerade") + " Zahlen gelöscht.");
        }
    }

    // Datenbank rechnet mit SUM  Anzahl ungerader und ungerader Zahlen
    private static void showStats(Connection conn) throws SQLException {
        String sql = "SELECT SUM(value2=0) AS gerade, SUM(value2=1) AS ungerade FROM WERTE";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int gerade = rs.getInt("gerade");
            int ungerade = rs.getInt("ungerade");
            System.out.println("Aktuelle Statistik -> Gerade: " + gerade + ", Ungerade: " + ungerade);
        }
    }
}
