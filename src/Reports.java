import javax.swing.*;
import java.sql.*;

public class Reports {

    public static void showRevenue() {

        JFrame f = new JFrame("Revenue Report");
        f.setSize(400, 300);

        JTextArea area = new JTextArea();

        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM RevenueView");

            while (rs.next()) {
                area.append(rs.getString(1) + " → " + rs.getDouble(2) + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        f.add(new JScrollPane(area));
        f.setVisible(true);
    }
}