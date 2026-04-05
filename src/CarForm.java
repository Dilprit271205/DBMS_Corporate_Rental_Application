import javax.swing.*;
import java.sql.*;

public class CarForm {

    public CarForm() {

        JFrame f = new JFrame("Add Car");
        f.setSize(400, 350);

        JTextField number = new JTextField();
        JTextField model = new JTextField();

        JComboBox<String> typeBox = new JComboBox<>();
        JComboBox<String> fuelBox = new JComboBox<>();

        Utils.loadComboBox(typeBox, "SELECT type_name FROM CarType", "type_name");
        Utils.loadComboBox(fuelBox, "SELECT fuel_name FROM FuelType", "fuel_name");

        JButton save = new JButton("Save");

        number.setBounds(150, 50, 150, 30);
        model.setBounds(150, 100, 150, 30);
        typeBox.setBounds(150, 150, 150, 30);
        fuelBox.setBounds(150, 200, 150, 30);
        save.setBounds(150, 250, 100, 30);

        save.addActionListener(e -> {
            try {

                int typeId = Utils.getId("CarType", "type_name",
                        (String) typeBox.getSelectedItem(), "type_id");

                int fuelId = Utils.getId("FuelType", "fuel_name",
                        (String) fuelBox.getSelectedItem(), "fuel_id");

                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO Car(car_number, model, type_id, fuel_id, status) VALUES (?,?,?,?,?)";

                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, number.getText());
                pst.setString(2, model.getText());
                pst.setInt(3, typeId);
                pst.setInt(4, fuelId);
                pst.setString(5, "Available");

                pst.executeUpdate();

                JOptionPane.showMessageDialog(f, "Car Added!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        f.setLayout(null);

        f.add(new JLabel("Car No")).setBounds(50, 50, 100, 30);
        f.add(number);

        f.add(new JLabel("Model")).setBounds(50, 100, 100, 30);
        f.add(model);

        f.add(new JLabel("Type")).setBounds(50, 150, 100, 30);
        f.add(typeBox);

        f.add(new JLabel("Fuel")).setBounds(50, 200, 100, 30);
        f.add(fuelBox);

        f.add(save);

        f.setVisible(true);
    }
}