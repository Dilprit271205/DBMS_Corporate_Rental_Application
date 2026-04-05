import java.sql.*;
import javax.swing.*;

public class Utils {

    public static void loadComboBox(JComboBox<String> combo, String query, String column) {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            combo.removeAllItems();

            while (rs.next()) {
                combo.addItem(rs.getString(column));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getId(String table, String nameColumn, String value, String idColumn) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT " + idColumn + " FROM " + table + " WHERE " + nameColumn + " = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, value);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(idColumn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}