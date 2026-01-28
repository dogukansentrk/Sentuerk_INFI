package Main;

import java.sql.*;

public class foreignkey {

	private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/zahlen_db";
	private static final String root = "root";
	private static final String password = "*****";

	public static void main(String[] args) {
		try (Connection conn = DriverManager.getConnection(DB_URL, root, password)) {

			createTables(conn);
			insertData(conn);
			showAllOrders(conn);
			testCascadeDelete(conn);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void createTables(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.execute("DROP TABLE IF EXISTS Bestellung;");
			stmt.execute("DROP TABLE IF EXISTS Kunde;");

			stmt.execute("""
				CREATE TABLE Kunde (
					id_kunde INT AUTO_INCREMENT PRIMARY KEY,
					name VARCHAR(100) NOT NULL,
					stadt VARCHAR(100)
				);
			""");

			stmt.execute("""
				CREATE TABLE Bestellung (
					id_bestellung INT AUTO_INCREMENT PRIMARY KEY,
					datum DATE NOT NULL,
					betrag DOUBLE,
					id_kunde INT,
					FOREIGN KEY (id_kunde)
						REFERENCES Kunde(id_kunde)
						ON DELETE CASCADE
						ON UPDATE CASCADE
				);
			""");
			System.out.println("Tabellen erstellt.");
		}
	}

	private static void insertData(Connection conn) throws SQLException {
		String sqlKunde = "INSERT INTO Kunde (name, stadt) VALUES (?, ?)";
		String sqlBestellung = "INSERT INTO Bestellung (datum, betrag, id_kunde) VALUES (?, ?, ?)";

		try (PreparedStatement psKunde = conn.prepareStatement(sqlKunde);
			 PreparedStatement psBestellung = conn.prepareStatement(sqlBestellung)) {

			psKunde.setString(1, "Anna Meier");
			psKunde.setString(2, "Wien");
			psKunde.executeUpdate();

			psKunde.setString(1, "Lukas Bauer");
			psKunde.setString(2, "Graz");
			psKunde.executeUpdate();

			psBestellung.setString(1, "2025-11-18");
			psBestellung.setDouble(2, 120.50);
			psBestellung.setInt(3, 1);
			psBestellung.executeUpdate();

			psBestellung.setString(1, "2025-11-19");
			psBestellung.setDouble(2, 80.00);
			psBestellung.setInt(3, 1);
			psBestellung.executeUpdate();

			psBestellung.setString(1, "2025-11-20");
			psBestellung.setDouble(2, 230.99);
			psBestellung.setInt(3, 2);
			psBestellung.executeUpdate();

			System.out.println("Testdaten eingefügt.");
		}
	}

	private static void showAllOrders(Connection conn) throws SQLException {
		String sql = """
			SELECT k.name, b.datum, b.betrag
			FROM Kunde k
			JOIN Bestellung b ON k.id_kunde = b.id_kunde;
		""";

		try (PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				System.out.println(rs.getString("name") + " hat am " +
					rs.getDate("datum") + " etwas für " +
					rs.getDouble("betrag") + "€ bestellt.");
			}
		}
	}

	private static void testCascadeDelete(Connection conn) throws SQLException {
		String deleteSQL = "DELETE FROM Kunde WHERE id_kunde = ?";
		try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
			ps.setInt(1, 1);
			ps.executeUpdate();
			System.out.println("Kunde 1 gelöscht (CASCADE).");
		}

		try (Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT * FROM Bestellung")) {
			while (rs.next()) {
				System.out.println("Bestellung " + rs.getInt("id_bestellung") +
					" gehört zu Kunde " + rs.getInt("id_kunde"));
			}
		}
	}
}
