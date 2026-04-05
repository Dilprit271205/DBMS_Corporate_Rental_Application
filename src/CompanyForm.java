import javax.swing.*;
import java.sql.*;

public class CompanyForm {

    public CompanyForm() {

        JFrame f = new JFrame("Add Company");
        f.setSize(400, 300);

        JLabel nameLabel = new JLabel("Name:");
        JLabel phoneLabel = new JLabel("Phone:");

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();

        JButton saveBtn = new JButton("Save");

        nameLabel.setBounds(50, 50, 100, 30);
        nameField.setBounds(150, 50, 150, 30);

        phoneLabel.setBounds(50, 100, 100, 30);
        phoneField.setBounds(150, 100, 150, 30);

        saveBtn.setBounds(150, 160, 100, 30);

        saveBtn.addActionListener(e -> {
            try {
                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO Company(name, phone) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, nameField.getText());
                pst.setString(2, phoneField.getText());

                pst.executeUpdate();

                JOptionPane.showMessageDialog(f, "Company Added!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        f.setLayout(null);
        f.add(nameLabel);
        f.add(nameField);
        f.add(phoneLabel);
        f.add(phoneField);
        f.add(saveBtn);

        f.setVisible(true);
    }
}