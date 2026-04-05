import javax.swing.*;
import java.sql.*;

public class BookingForm {

    public BookingForm() {

        JFrame f = new JFrame("Create Booking");
        f.setSize(450, 400);

        JComboBox<String> companyBox = new JComboBox<>();
        JComboBox<String> driverBox = new JComboBox<>();
        JComboBox<String> carBox = new JComboBox<>();
        JComboBox<String> pickupBox = new JComboBox<>();
        JComboBox<String> dropBox = new JComboBox<>();
        JComboBox<String> statusBox = new JComboBox<>();

        JTextField tripType = new JTextField();

        // Load data
        Utils.loadComboBox(companyBox, "SELECT name FROM Company", "name");
        Utils.loadComboBox(driverBox, "SELECT name FROM Driver", "name");
        Utils.loadComboBox(carBox, "SELECT car_number FROM Car", "car_number");
        Utils.loadComboBox(pickupBox, "SELECT location_name FROM Location", "location_name");
        Utils.loadComboBox(dropBox, "SELECT location_name FROM Location", "location_name");
        Utils.loadComboBox(statusBox, "SELECT status_name FROM BookingStatus", "status_name");

        JButton save = new JButton("Create Booking");

        // Layout
        int y = 30;

        f.add(new JLabel("Company")).setBounds(30, y, 120, 25);
        f.add(companyBox).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Driver")).setBounds(30, y, 120, 25);
        f.add(driverBox).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Car")).setBounds(30, y, 120, 25);
        f.add(carBox).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Pickup")).setBounds(30, y, 120, 25);
        f.add(pickupBox).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Drop")).setBounds(30, y, 120, 25);
        f.add(dropBox).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Trip Type")).setBounds(30, y, 120, 25);
        f.add(tripType).setBounds(160, y, 200, 25); y+=40;

        f.add(new JLabel("Status")).setBounds(30, y, 120, 25);
        f.add(statusBox).setBounds(160, y, 200, 25); y+=50;

        save.setBounds(160, y, 150, 30);

        // Action
        save.addActionListener(e -> {
            try {

                int companyId = Utils.getId("Company", "name",
                        (String) companyBox.getSelectedItem(), "company_id");

                int driverId = Utils.getId("Driver", "name",
                        (String) driverBox.getSelectedItem(), "driver_id");

                int carId = Utils.getId("Car", "car_number",
                        (String) carBox.getSelectedItem(), "car_id");

                int pickupId = Utils.getId("Location", "location_name",
                        (String) pickupBox.getSelectedItem(), "location_id");

                int dropId = Utils.getId("Location", "location_name",
                        (String) dropBox.getSelectedItem(), "location_id");

                int statusId = Utils.getId("BookingStatus", "status_name",
                        (String) statusBox.getSelectedItem(), "status_id");

                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO Booking(company_id, pickup_location, drop_location, booking_date, trip_type, status_id, car_id, driver_id) VALUES (?,?,?,?,?,?,?,?)";

                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setInt(1, companyId);
                pst.setInt(2, pickupId);
                pst.setInt(3, dropId);
                pst.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                pst.setString(5, tripType.getText());
                pst.setInt(6, statusId);
                pst.setInt(7, carId);
                pst.setInt(8, driverId);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(f, "Booking Created!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        f.setLayout(null);
        f.add(save);

        f.setVisible(true);
    }
}