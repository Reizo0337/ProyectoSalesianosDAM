import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.io.FileInputStream;

public class Migrate {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/gestion_compras?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "admin";
        String password = "admin123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Adding Anio column to presupuesto...");
            stmt.executeUpdate("ALTER TABLE presupuesto ADD COLUMN Anio INT DEFAULT 2026");
            System.out.println("Updating existing records to year 2026...");
            stmt.executeUpdate("UPDATE presupuesto SET Anio = 2026 WHERE Anio IS NULL");
            
            System.out.println("Migration successful!");
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            // If it already exists, it might fail, which is fine
        }
    }
}
