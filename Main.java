package Main;

import java.sql.*;

public class Main {

    private static final String DB_URL = "jdbc:sqlite:test.db";

    public static void main(String[] args) {
        try (Connection conn = connect()) {
            createTable(conn);
            insertData(conn);
            selectData(conn);
            updateData(conn);
            deleteData(conn);
            dropTable(conn);
            System.out.println("Alle Operationen wurden erfolgreich ausgeführt!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Verbindung zur Datenbank hergestellt.");
        return conn;
    }

    private static void createTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS COMPANY (
                    ID INTEGER PRIMARY KEY NOT NULL,
                    NAME TEXT NOT NULL,
                    AGE INT NOT NULL,
                    ADDRESS CHAR(50),
                    SALARY REAL
                );
                """;
 
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Tabelle COMPANY wurde erstell.");
        }
    }

    private static void insertData(Connection conn) throws SQLException {
        String[] inserts = {
                "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (1, 'Paul', 32, 'California', 2000);",
                "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (2, 'Allen', 25, 'Texas', 1500);",
                "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (3, 'Teddy', 23, 'Norway', 1000);",
                "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (4, 'Mark', 25, 'Richmond', 6500);"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : inserts) stmt.executeUpdate(sql);
            System.out.println("Datensätze wurden erfolgreich eingefügt.");
        }
    }

    private static void selectData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            System.out.println("Alle Datensätze:");
            printResultSet(stmt.executeQuery("SELECT * FROM COMPANY;"));
            printResultSet(stmt.executeQuery("SELECT * FROM COMPANY WHERE AGE > 23 AND SALARY > 2000;"));
            printResultSet(stmt.executeQuery("SELECT * FROM COMPANY WHERE AGE < 25 OR SALARY > 5000;"));
	        printResultSet(stmt.executeQuery("SELECT * FROM COMPANY WHERE NAME LIKE 'M%';"));
        }
    }

    private static void updateData(Connection conn) throws SQLException {
        String sql = "UPDATE COMPANY SET SALARY = 2500 WHERE ID = 1;";
        try (Statement stmt = conn.createStatement()) {
        int rows = stmt.executeUpdate(sql);
        }
    }

    private static void deleteData(Connection conn) throws SQLException {
        String sql = "DELETE FROM COMPANY WHERE ID = 2;";
        try (Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
        }
    }

    private static void dropTable(Connection conn) throws SQLException {
        String sql = "DROP TABLE IF EXISTS COMPANY;";
        System.out.println("Tabelle wurde gelöscht");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            int id = rs.getInt("ID");
            String name = rs.getString("NAME");
            int age = rs.getInt("AGE");
            String address = rs.getString("ADDRESS");
            double salary = rs.getDouble("SALARY");

            System.out.printf("ID=%d | NAME=%s | AGE=%d | ADDRESS=%s | SALARY=%.2f%n",
                    id, name, age, address, salary);
        }
    }
}
