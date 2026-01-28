package Main;

import java.sql.*;
import java.util.Scanner;

public class KundenArtikelTable {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/zahlen_db"; 
    private static final String USER = "root";
    private static final String PASS = "****"; 

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Verbindung hergestellt.");

            DatabaseSetup.createTables(conn);

            KundenManager kundenManager = new KundenManager(conn);
            ArtikelManager artikelManager = new ArtikelManager(conn);
            BestellManager bestellManager = new BestellManager(conn);

            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                printMenu();
                System.out.print("Deine Auswahl: ");

                if (!scanner.hasNextInt()) {
                    System.out.println(">> Bitte eine Zahl eingeben!");
                    scanner.nextLine(); continue;
                }
                int wahl = scanner.nextInt();
                scanner.nextLine();

                try {
                    switch (wahl) {
                        case 1:
                            System.out.print("Name: ");
                            String name = scanner.nextLine();
                            System.out.print("E-Mail: ");
                            String mail = scanner.nextLine();
                            kundenManager.addKunde(name, mail);
                            break;

                        case 2:
                            System.out.print("ID des Kunden zum Bearbeiten: ");
                            int kIdEdit = scanner.nextInt(); scanner.nextLine();
                            System.out.print("Neuer Name: ");
                            String newName = scanner.nextLine();
                            kundenManager.updateKunde(kIdEdit, newName);
                            break;

                        case 3:
                            System.out.print("ID des Kunden zum Löschen: ");
                            int kIdDel = scanner.nextInt();
                            kundenManager.deleteKunde(kIdDel);
                            break;

                        case 4:
                            System.out.print("Bezeichnung: ");
                            String bez = scanner.nextLine();
                            System.out.print("Preis: ");
                            double preis = scanner.nextDouble();
                            System.out.print("Anfangs-Lagerbestand (Stück): ");
                            int bestand = scanner.nextInt();
                            artikelManager.addArtikel(bez, preis, bestand);
                            break;

                        case 5:
                            System.out.print("Artikel-ID: ");
                            int aIdRestock = scanner.nextInt();
                            System.out.print("Menge zum Einlagern: ");
                            int mengePlus = scanner.nextInt();
                            artikelManager.updateLagerbestand(aIdRestock, mengePlus);
                            break;

                        case 6:
                            System.out.print("Kunden-ID: ");
                            int kId = scanner.nextInt();
                            System.out.print("Artikel-ID: ");
                            int aId = scanner.nextInt();
                            System.out.print("Anzahl kaufen: ");
                            int anzahl = scanner.nextInt();
                            bestellManager.bestelleArtikel(kId, aId, anzahl);
                            break;

                        case 7:
                            System.out.print("Kunden-ID (0 für alle): ");
                            int sId = scanner.nextInt();
                            if(sId == 0) bestellManager.zeigeAlleBestellungen();
                            else bestellManager.zeigeBestellungenVonKunde(sId);
                            break;

                        case 8:
                            bestellManager.zeigeTopSellerStatistik();
                            break;

                        case 9:
                            artikelManager.zeigeKritischenBestand();
                            break;
                        
                        case 10:
                            artikelManager.zeigeAlleArtikelMitDatum();
                            break;

                        case 11:
                            String csvPath = "C:\\eclipseworkspacenew\\INFI\\src\\Main\\artikel.csv";
                            CSVReader.readCSV(csvPath);
                            break;

                        case 0:
                            running = false;
                            System.out.println("Programm beendet. Bis bald!");
                            break;
                        default:
                            System.out.println("Ungültige Wahl.");
                    }
                } catch (SQLException e) {
                    System.out.println("SQL Fehler: " + e.getMessage());
                }
            }
            scanner.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MANAGER V3.2 (mit CSV-Autopfad) ---");
        System.out.println("1. Kunde NEU");
        System.out.println("2. Kunde ÄNDERN (Name)");
        System.out.println("3. Kunde LÖSCHEN");
        System.out.println("-------------------------");
        System.out.println("4. Artikel NEU anlegen");
        System.out.println("5. Artikel lagerbestand ERHÖHEN");
        System.out.println("-------------------------");
        System.out.println("6. WARE BESTELLEN (Kauf)");
        System.out.println("7. Bestellungen ANZEIGEN");
        System.out.println("-------------------------");
        System.out.println("8. Statistik: Umsatz pro Artikel");
        System.out.println("9. Statistik: Kritischer Lagerbestand (<5)");
        System.out.println("10. Alle Artikel anzeigen (mit Erstell-Datum)");
        System.out.println("11. CSV-Datei (Artikel) automatisch einlesen");
        System.out.println("0. Ende");
    }

    static class DatabaseSetup {
        public static void createTables(Connection conn) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS Kunde (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100)
                    );
                """);
                
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS Artikel (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        bezeichnung VARCHAR(100) NOT NULL,
                        preis DOUBLE NOT NULL,
                        lagerbestand INT DEFAULT 0,
                        erstellt_am DATE DEFAULT (CURRENT_DATE)
                    );
                """);
                
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS Bestellung (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        kundenID INT,
                        artikelID INT,
                        anzahl INT,
                        zeitpunkt DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (kundenID) REFERENCES Kunde(id) ON DELETE CASCADE,
                        FOREIGN KEY (artikelID) REFERENCES Artikel(id) ON DELETE CASCADE
                    );
                """);
            }
        }
    }

    static class KundenManager {
        Connection conn;
        public KundenManager(Connection c) { conn = c; }

        public void addKunde(String name, String email) throws SQLException {
            String sql = "INSERT INTO Kunde (name, email) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name); ps.setString(2, email);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) System.out.println("Kunde ID " + rs.getInt(1) + " angelegt.");
            }
        }

        public void updateKunde(int id, String newName) throws SQLException {
            String sql = "UPDATE Kunde SET name = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setInt(2, id);
                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("Kunde aktualisiert.");
                else System.out.println("Kunde nicht gefunden.");
            }
        }

        public void deleteKunde(int id) throws SQLException {
            String sql = "DELETE FROM Kunde WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                System.out.println(rows > 0 ? "Kunde gelöscht." : "Kunde existiert nicht.");
            }
        }
    }

    static class ArtikelManager {
        Connection conn;
        public ArtikelManager(Connection c) { conn = c; }

        public void addArtikel(String bez, double preis, int bestand) throws SQLException {
            String sql = "INSERT INTO Artikel (bezeichnung, preis, lagerbestand, erstellt_am) VALUES (?, ?, ?, CURRENT_DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, bez); ps.setDouble(2, preis); ps.setInt(3, bestand);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) System.out.println("Artikel ID " + rs.getInt(1) + " angelegt.");
            }
        }

        public void updateLagerbestand(int id, int mengeDazu) throws SQLException {
            String sql = "UPDATE Artikel SET lagerbestand = lagerbestand + ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, mengeDazu);
                ps.setInt(2, id);
                ps.executeUpdate();
                System.out.println("Lagerbestand aktualisiert.");
            }
        }

        public void zeigeKritischenBestand() throws SQLException {
            String sql = "SELECT * FROM Artikel WHERE lagerbestand < 5";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("--- WARNUNG: WENIG LAGERBESTAND (<5) ---");
                while(rs.next()) {
                    System.out.println(rs.getString("bezeichnung") + ": Nur noch " + rs.getInt("lagerbestand") + " Stück!");
                }
            }
        }

        public void zeigeAlleArtikelMitDatum() throws SQLException {
            String sql = "SELECT * FROM Artikel";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("--- ALLE ARTIKEL (mit DATE) ---");
                while(rs.next()) {
                    Date datum = rs.getDate("erstellt_am");
                    System.out.println("ID " + rs.getInt("id") + ": " + rs.getString("bezeichnung") + 
                                       " | Preis: " + rs.getDouble("preis") + 
                                       " | Bestand: " + rs.getInt("lagerbestand") +
                                       " | Erstellt am: " + datum);
                }
            }
        }
    }

    static class BestellManager {
        Connection conn;
        public BestellManager(Connection c) { conn = c; }

        public void bestelleArtikel(int kId, int aId, int anzahl) throws SQLException {
            conn.setAutoCommit(false);
            try {
                int aktuellerBestand = 0;
                String checkSql = "SELECT lagerbestand FROM Artikel WHERE id = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, aId);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        aktuellerBestand = rs.getInt("lagerbestand");
                    } else {
                        throw new SQLException("Artikel nicht gefunden!");
                    }
                }

                if (aktuellerBestand < anzahl) {
                    throw new SQLException("Nicht genug Lagerbestand! Verfügbar: " + aktuellerBestand);
                }

                String insertSql = "INSERT INTO Bestellung (kundenID, artikelID, anzahl) VALUES (?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                    psIns.setInt(1, kId); psIns.setInt(2, aId); psIns.setInt(3, anzahl);
                    psIns.executeUpdate();
                }

                String updateSql = "UPDATE Artikel SET lagerbestand = lagerbestand - ? WHERE id = ?";
                try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                    psUpd.setInt(1, anzahl);
                    psUpd.setInt(2, aId);
                    psUpd.executeUpdate();
                }

                conn.commit();
                System.out.println("Bestellung erfolgreich!");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Bestellung ABGEBROCHEN: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        }

        public void zeigeBestellungenVonKunde(int kId) throws SQLException {
            String sql = """
                SELECT k.name, a.bezeichnung, b.anzahl, b.zeitpunkt 
                FROM Bestellung b
                JOIN Kunde k ON b.kundenID = k.id
                JOIN Artikel a ON b.artikelID = a.id
                WHERE k.id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, kId);
                ResultSet rs = ps.executeQuery();
                System.out.println("--- Historie für Kunde ID " + kId + " ---");
                while(rs.next()) {
                    Timestamp ts = rs.getTimestamp("zeitpunkt");
                    System.out.println(ts + ": " + rs.getString("bezeichnung") + " (" + rs.getInt("anzahl") + "x)");
                }
            }
        }
        
        public void zeigeAlleBestellungen() throws SQLException {
             String sql = """
                SELECT k.name, a.bezeichnung, b.anzahl, b.zeitpunkt
                FROM Bestellung b
                JOIN Kunde k ON b.kundenID = k.id
                JOIN Artikel a ON b.artikelID = a.id
                ORDER BY b.zeitpunkt DESC
            """;
             try(Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
                 System.out.println("--- ALLE BESTELLUNGEN (DATETIME) ---");
                 while(rs.next()){
                     System.out.println(rs.getTimestamp("zeitpunkt") + " | " + rs.getString("name") + " kauft " + rs.getString("bezeichnung"));
                 }
             }
        }

        public void zeigeTopSellerStatistik() throws SQLException {
            String sql = """
                SELECT a.bezeichnung, SUM(b.anzahl) as verkauft_gesamt, SUM(b.anzahl * a.preis) as umsatz
                FROM Bestellung b
                JOIN Artikel a ON b.artikelID = a.id
                GROUP BY a.bezeichnung
                ORDER BY umsatz DESC
            """;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("--- TOP SELLER STATISTIK ---");
                while (rs.next()) {
                    System.out.println(rs.getString("bezeichnung") + " | Verkauft: " + rs.getInt("verkauft_gesamt") + " | Umsatz: " + rs.getDouble("umsatz"));
                }
            }
        }
    }

    static class CSVReader {
        public static void readCSV(String filePath) {
            System.out.println("--- CSV Datei wird eingelesen ---");
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length < 3) continue;
                    System.out.printf("Artikel: %-20s | Preis: %-8s | Bestand: %s%n",
                                      parts[0], parts[1], parts[2]);
                    count++;
                }
                System.out.println("\n>> Insgesamt " + count + " Zeilen gelesen.");
            } catch (java.io.IOException e) {
                System.out.println("Fehler beim Lesen der CSV: " + e.getMessage());
            }
        }
    }
}

