import javax.swing.*;
import java.sql.*;

public class DriverForm {

    public DriverForm() {

        JFrame f = new JFrame("Add Driver");
        f.setSize(400, 300);

        JTextField name = new JTextField();
        JTextField phone = new JTextField();

        JButton save = new JButton("Save");

        name.setBounds(150, 50, 150, 30);
        phone.setBounds(150, 100, 150, 30);
        save.setBounds(150, 160, 100, 30);

        save.addActionListener(e -> {
            try {
                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO Driver(name, phone, status) VALUES (?, ?, ?)";

                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name.getText());
                pst.setString(2, phone.getText());
                pst.setString(3, "Available");

                pst.executeUpdate();

                JOptionPane.showMessageDialog(f, "Driver Added!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        f.setLayout(null);

        f.add(new JLabel("Name")).setBounds(50, 50, 100, 30);
        f.add(name);

        f.add(new JLabel("Phone")).setBounds(50, 100, 100, 30);
        f.add(phone);

        f.add(save);

        f.setVisible(true);
    }
}