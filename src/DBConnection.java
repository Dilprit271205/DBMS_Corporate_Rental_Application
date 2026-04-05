import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/car_rental_system?useSSL=false&serverTimezone=UTC",
                "root",
                "M@@L@XMIt@1962@*#"
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}