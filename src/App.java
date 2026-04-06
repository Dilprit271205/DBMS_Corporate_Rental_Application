import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * CORPORATE CAR RENTAL SYSTEM - FULL JDBC DATABASE UI
 * ----------------------------------------------------
 * Includes Premium Dashboard, Full CRUD (Create, Read, Update, DELETE),
 * strict Duplicate Validation checks, and synchronized UI models.
 */
public class App extends Application {

    // =========================================================================
    // ⚙️ DATABASE CONFIGURATION (Update these to match your MySQL setup)
    // =========================================================================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/corporate_rental?createDatabaseIfNotExist=true";
    private static final String DB_USER = "root";       // <-- CHANGE TO YOUR DB USER
    private static final String DB_PASS = "M@@L@XMIt@1962@*#";   // <-- CHANGE TO YOUR DB PASSWORD

    // --- OBSERVABLE LISTS (UI Binding) ---
    private final ObservableList<Company> companyData = FXCollections.observableArrayList();
    private final ObservableList<Car> carData = FXCollections.observableArrayList();
    private final ObservableList<Driver> driverData = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();
    private final ObservableList<Invoice> invoiceData = FXCollections.observableArrayList();
    private final ObservableList<RateCard> rateCardData = FXCollections.observableArrayList();
    
    private final ObservableList<String> serviceTypeList = FXCollections.observableArrayList("8hrs - 80km", "12hrs - 120km", "Airport Transfer", "Local Fixed");

    // --- UI COMPONENTS ---
    private StackPane contentArea;
    private Label headerTitle;
    private Label revLabel; 
    private Label pendingLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        setupDatabaseTables();
        loadAllDataFromDB();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        root.setLeft(createSidebar());
        root.setTop(createHeader());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        switchView("Dashboard");

        Scene scene = new Scene(root, 1450, 850);
        scene.getStylesheets().add(createInlineCSS());

        primaryStage.setTitle("Corporate Car Rental ERP System (Database Connected)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // =========================================================================
    // 🗄️ DATABASE CONNECTION & SETUP METHODS
    // =========================================================================

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void setupDatabaseTables() {
        String createCompany = "CREATE TABLE IF NOT EXISTS Company (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), contact VARCHAR(255), email VARCHAR(255) UNIQUE, phone VARCHAR(50), address TEXT)";
        String createCar = "CREATE TABLE IF NOT EXISTS Car (id INT AUTO_INCREMENT PRIMARY KEY, regNo VARCHAR(100) UNIQUE, make VARCHAR(100), model VARCHAR(100), year VARCHAR(10), capacity VARCHAR(10), luggage VARCHAR(10), type VARCHAR(50), fuel VARCHAR(50), status VARCHAR(50))";
        String createDriver = "CREATE TABLE IF NOT EXISTS Driver (id INT AUTO_INCREMENT PRIMARY KEY, firstName VARCHAR(100), lastName VARCHAR(100), phone VARCHAR(50), license VARCHAR(100) UNIQUE, shift VARCHAR(50), status VARCHAR(50))";
        String createBooking = "CREATE TABLE IF NOT EXISTS Booking (id INT AUTO_INCREMENT PRIMARY KEY, company VARCHAR(255), employee VARCHAR(255), serviceType VARCHAR(100), carRegNo VARCHAR(100), carType VARCHAR(50), fuelType VARCHAR(50), driver VARCHAR(150), pickup VARCHAR(255), dropLoc VARCHAR(255), date VARCHAR(50), time VARCHAR(50), status VARCHAR(50))";
        String createRateCard = "CREATE TABLE IF NOT EXISTS RateCard (id INT AUTO_INCREMENT PRIMARY KEY, company VARCHAR(255), serviceType VARCHAR(100), carType VARCHAR(50), fuelType VARCHAR(50), baseFare VARCHAR(50), inclKm VARCHAR(50), inclHrs VARCHAR(50), extraKmRate VARCHAR(50), extraHrRate VARCHAR(50))";
        String createInvoice = "CREATE TABLE IF NOT EXISTS Invoice (id INT AUTO_INCREMENT PRIMARY KEY, bookingRef VARCHAR(50), company VARCHAR(255), carRegNo VARCHAR(100), distance VARCHAR(50), hours VARCHAR(50), baseFare VARCHAR(50), distCharge VARCHAR(50), hrCharge VARCHAR(50), tolls VARCHAR(50), tax VARCHAR(50), total VARCHAR(50), payMode VARCHAR(50), status VARCHAR(50), date VARCHAR(100))";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCompany); stmt.execute(createCar); stmt.execute(createDriver);
            stmt.execute(createBooking); stmt.execute(createRateCard); stmt.execute(createInvoice);
            System.out.println("✅ Database tables verified/created successfully.");
        } catch (SQLException e) {
            showAlert("Database Connection Error", "Could not connect or create tables. Check MySQL credentials.\n" + e.getMessage());
        }
    }

    private void loadAllDataFromDB() {
        companyData.clear(); carData.clear(); driverData.clear();
        bookingData.clear(); rateCardData.clear(); invoiceData.clear();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Company");
            while(rs.next()) companyData.add(new Company(rs.getString("id"), rs.getString("name"), rs.getString("contact"), rs.getString("email"), rs.getString("phone"), rs.getString("address")));

            rs = stmt.executeQuery("SELECT * FROM Car");
            while(rs.next()) carData.add(new Car(rs.getString("id"), rs.getString("regNo"), rs.getString("make"), rs.getString("model"), rs.getString("year"), rs.getString("capacity"), rs.getString("luggage"), rs.getString("type"), rs.getString("fuel"), rs.getString("status")));

            rs = stmt.executeQuery("SELECT * FROM Driver");
            while(rs.next()) driverData.add(new Driver(rs.getString("id"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("phone"), rs.getString("license"), rs.getString("shift"), rs.getString("status")));

            rs = stmt.executeQuery("SELECT * FROM Booking");
            while(rs.next()) bookingData.add(new Booking(rs.getString("id"), rs.getString("company"), rs.getString("employee"), rs.getString("serviceType"), rs.getString("carRegNo"), rs.getString("carType"), rs.getString("fuelType"), rs.getString("driver"), rs.getString("pickup"), rs.getString("dropLoc"), rs.getString("date"), rs.getString("time"), rs.getString("status")));

            rs = stmt.executeQuery("SELECT * FROM RateCard");
            while(rs.next()) rateCardData.add(new RateCard(rs.getString("id"), rs.getString("company"), rs.getString("serviceType"), rs.getString("carType"), rs.getString("fuelType"), rs.getString("baseFare"), rs.getString("inclKm"), rs.getString("inclHrs"), rs.getString("extraKmRate"), rs.getString("extraHrRate")));

            rs = stmt.executeQuery("SELECT * FROM Invoice");
            while(rs.next()) invoiceData.add(new Invoice(rs.getString("id"), rs.getString("bookingRef"), rs.getString("company"), rs.getString("carRegNo"), rs.getString("distance"), rs.getString("hours"), rs.getString("baseFare"), rs.getString("distCharge"), rs.getString("hrCharge"), rs.getString("tolls"), rs.getString("tax"), rs.getString("total"), rs.getString("payMode"), rs.getString("status"), rs.getString("date")));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardStats() {
        if(pendingLabel == null || revLabel == null) return;
        long pending = bookingData.stream().filter(b -> b.getStatus().equals("Pending")).count();
        double totalRev = invoiceData.stream().filter(inv -> "Paid".equals(inv.getStatus())).mapToDouble(inv -> parseDoubleSafe(inv.getTotal())).sum();
        pendingLabel.setText(String.valueOf(pending));
        revLabel.setText("₹" + String.format("%.2f", totalRev));
    }

    // =========================================================================
    // LAYOUT COMPONENTS
    // =========================================================================

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(220);

        Label logo = new Label("CORP RENTAL");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 22));
        logo.setPadding(new Insets(0, 0, 30, 10));
        sidebar.getChildren().add(logo);

        String[] menuItems = {"Dashboard", "Companies", "Vehicles", "Drivers", "Bookings", "Rate Cards", "Trip & Billing"};
        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-alignment: center-left; -fx-font-size: 15px; -fx-padding: 10 15;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-alignment: center-left; -fx-font-size: 15px; -fx-padding: 10 15;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-alignment: center-left; -fx-font-size: 15px; -fx-padding: 10 15;"));
            btn.setOnAction(e -> { switchView(item); updateDashboardStats(); });
            sidebar.getChildren().add(btn);
        }
        return sidebar;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);

        headerTitle = new Label("Dashboard");
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerTitle.setTextFill(Color.web("#333333"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("System Administrator");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        header.getChildren().addAll(headerTitle, spacer, userLabel);
        return header;
    }

    private void switchView(String viewName) {
        contentArea.getChildren().clear();
        headerTitle.setText(viewName);

        switch (viewName) {
            case "Dashboard": contentArea.getChildren().add(createDashboardView()); break;
            case "Companies": contentArea.getChildren().add(createCompanyView()); break;
            case "Vehicles": contentArea.getChildren().add(createVehicleView()); break;
            case "Drivers": contentArea.getChildren().add(createDriverView()); break;
            case "Bookings": contentArea.getChildren().add(createBookingView()); break;
            case "Rate Cards": contentArea.getChildren().add(createRateCardView()); break;
            case "Trip & Billing": contentArea.getChildren().add(createBillingView()); break;
        }
    }

    // =========================================================================
    // MODULE VIEWS WITH JDBC INTEGRATION
    // =========================================================================

    private VBox createDashboardView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(10));

        VBox welcomeBox = new VBox(5);
        Label welcomeTitle = new Label("Welcome back, System Administrator 👋");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeTitle.setTextFill(Color.web("#2c3e50"));
        Label welcomeSub = new Label("Live Database Metrics & Overview.");
        welcomeSub.setFont(Font.font("System", FontWeight.NORMAL, 16));
        welcomeSub.setTextFill(Color.web("#7f8c8d"));
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSub);

        HBox statsBox = new HBox(25);
        
        long pending = bookingData.stream().filter(b -> b.getStatus().equals("Pending")).count();
        double totalRev = invoiceData.stream().filter(inv -> "Paid".equals(inv.getStatus())).mapToDouble(inv -> parseDoubleSafe(inv.getTotal())).sum();

        Label compLab = createLabel(String.valueOf(companyData.size()), 36, "#2c3e50");
        Label carLab = createLabel(String.valueOf(carData.size()), 36, "#2c3e50");
        pendingLabel = createLabel(String.valueOf(pending), 36, "#2c3e50");
        revLabel = createLabel("₹" + String.format("%.2f", totalRev), 36, "#2c3e50");

        statsBox.getChildren().addAll(
            createStatCard("🏢 Companies", compLab, "#2980b9", "#ebf5fb"),
            createStatCard("🚗 Active Fleet", carLab, "#27ae60", "#eafaf1"),
            createStatCard("📅 Pending Bookings", pendingLabel, "#e74c3c", "#fdedec"),
            createStatCard("💰 Total Revenue", revLabel, "#8e44ad", "#f5eef8")
        );

        VBox recentBox = new VBox(10);
        Label recentTitle = new Label("Recent Bookings Overview");
        recentTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        TableView<Booking> recentTable = new TableView<>(bookingData);
        recentTable.getColumns().addAll(
            createCol("BKG ID", "id", 100), createCol("Company", "company", 250),
            createCol("Service Type", "serviceType", 200), createCol("Date", "date", 150), createCol("Status", "status", 150)
        );
        recentTable.setPrefHeight(300);
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recentTable.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        recentBox.getChildren().addAll(recentTitle, recentTable);
        layout.getChildren().addAll(welcomeBox, statsBox, recentBox);
        return layout;
    }

    private Label createLabel(String txt, int size, String color) {
        Label l = new Label(txt); l.setFont(Font.font("System", FontWeight.BOLD, size)); l.setTextFill(Color.web(color)); return l;
    }

    private VBox createStatCard(String title, Label valLabel, String color, String bgColor) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, " + bgColor + "); " +
                      "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                      "-fx-border-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
        card.setPadding(new Insets(20)); card.setPrefWidth(260);
        Label titleLabel = new Label(title); titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15)); titleLabel.setTextFill(Color.web("#7f8c8d"));
        card.getChildren().addAll(titleLabel, valLabel);
        return card;
    }

    // --- COMPANY VIEW ---
    private VBox createCompanyView() {
        VBox layout = new VBox(15);
        TableView<Company> table = new TableView<>(companyData);
        table.getColumns().addAll(createCol("ID", "id", 50), createCol("Company Name", "name", 200), createCol("Contact Person", "contact", 150), createCol("Email", "email", 200), createCol("Phone", "phone", 150), createCol("Billing Address", "address", 250));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField nameIn = new TextField(); TextField contactIn = new TextField(); TextField emailIn = new TextField(); TextField phoneIn = new TextField(); TextField addressIn = new TextField(); addressIn.setPrefWidth(300);
        
        Button addBtn = new Button("Add Company"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);
        
        form.addRow(0, new Label("Name:"), nameIn, new Label("Contact:"), contactIn);
        form.addRow(1, new Label("Email:"), emailIn, new Label("Phone:"), phoneIn);
        form.addRow(2, new Label("Address:"), addressIn, new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                nameIn.setText(newSel.getName()); contactIn.setText(newSel.getContact()); emailIn.setText(newSel.getEmail()); phoneIn.setText(newSel.getPhone()); addressIn.setText(newSel.getAddress()); 
                updateBtn.setDisable(false); deleteBtn.setDisable(false); addBtn.setDisable(true); 
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if (companyData.stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(emailIn.getText()))) {
                showAlert("Duplicate Error", "A company with this email already exists!"); return;
            }
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Company (name, contact, email, phone, address) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nameIn.getText()); pstmt.setString(2, contactIn.getText()); pstmt.setString(3, emailIn.getText()); pstmt.setString(4, phoneIn.getText()); pstmt.setString(5, addressIn.getText());
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) companyData.add(new Company(String.valueOf(rs.getInt(1)), nameIn.getText(), contactIn.getText(), emailIn.getText(), phoneIn.getText(), addressIn.getText()));
                nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Company sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Company SET name=?, contact=?, email=?, phone=?, address=? WHERE id=?")) {
                    pstmt.setString(1, nameIn.getText()); pstmt.setString(2, contactIn.getText()); pstmt.setString(3, emailIn.getText()); pstmt.setString(4, phoneIn.getText()); pstmt.setString(5, addressIn.getText()); pstmt.setInt(6, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate();
                    sel.setName(nameIn.getText()); sel.setContact(contactIn.getText()); sel.setEmail(emailIn.getText()); sel.setPhone(phoneIn.getText()); sel.setAddress(addressIn.getText()); 
                    table.refresh(); table.getSelectionModel().clearSelection();
                    nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Company sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Company WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    companyData.remove(sel); table.getSelectionModel().clearSelection();
                    nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Corporate Clients Registry"), table, form); return layout;
    }

    // --- VEHICLE VIEW ---
    private VBox createVehicleView() {
        VBox layout = new VBox(15);
        TableView<Car> table = new TableView<>(carData);
        table.getColumns().addAll(createCol("Car ID", "id", 60), createCol("Reg No", "regNo", 120), createCol("Make", "make", 120), createCol("Model", "model", 120), createCol("Year", "year", 60), createCol("Seats", "capacity", 60), createCol("Luggage", "luggage", 80), createCol("Type", "type", 100), createCol("Fuel", "fuel", 80), createCol("Status", "status", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField regIn = new TextField(); TextField makeIn = new TextField(); TextField modelIn = new TextField(); TextField yearIn = new TextField(); TextField capIn = new TextField(); TextField lugIn = new TextField();
        ComboBox<String> typeIn = new ComboBox<>(FXCollections.observableArrayList("Sedan", "SUV", "Luxury", "Hatchback"));
        ComboBox<String> fuelIn = new ComboBox<>(FXCollections.observableArrayList("Petrol", "Diesel", "EV"));
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Available", "On Trip", "Maintenance")); statusIn.setValue("Available");
        
        Button addBtn = new Button("Add Vehicle"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("Reg No:"), regIn, new Label("Make:"), makeIn, new Label("Model:"), modelIn);
        form.addRow(1, new Label("Year:"), yearIn, new Label("Seats:"), capIn, new Label("Luggage:"), lugIn);
        form.addRow(2, new Label("Type:"), typeIn, new Label("Fuel:"), fuelIn, new Label("Status:"), statusIn);
        form.addRow(3, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                regIn.setText(newSel.getRegNo()); makeIn.setText(newSel.getMake()); modelIn.setText(newSel.getModel()); yearIn.setText(newSel.getYear()); capIn.setText(newSel.getCapacity()); lugIn.setText(newSel.getLuggage()); typeIn.setValue(newSel.getType()); fuelIn.setValue(newSel.getFuel()); statusIn.setValue(newSel.getStatus()); 
                updateBtn.setDisable(false); deleteBtn.setDisable(false); addBtn.setDisable(true); 
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if (carData.stream().anyMatch(c -> c.getRegNo().equalsIgnoreCase(regIn.getText()))) {
                showAlert("Duplicate Error", "A vehicle with Reg No " + regIn.getText() + " already exists!"); return;
            }
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Car (regNo, make, model, year, capacity, luggage, type, fuel, status) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, regIn.getText().toUpperCase()); pstmt.setString(2, makeIn.getText()); pstmt.setString(3, modelIn.getText()); pstmt.setString(4, yearIn.getText()); pstmt.setString(5, capIn.getText()); pstmt.setString(6, lugIn.getText()); pstmt.setString(7, typeIn.getValue()); pstmt.setString(8, fuelIn.getValue()); pstmt.setString(9, statusIn.getValue());
                pstmt.executeUpdate(); ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) carData.add(new Car(String.valueOf(rs.getInt(1)), regIn.getText().toUpperCase(), makeIn.getText(), modelIn.getText(), yearIn.getText(), capIn.getText(), lugIn.getText(), typeIn.getValue(), fuelIn.getValue(), statusIn.getValue()));
                regIn.clear(); makeIn.clear(); modelIn.clear(); yearIn.clear(); capIn.clear(); lugIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Car sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Car SET regNo=?, make=?, model=?, year=?, capacity=?, luggage=?, type=?, fuel=?, status=? WHERE id=?")) {
                    pstmt.setString(1, regIn.getText().toUpperCase()); pstmt.setString(2, makeIn.getText()); pstmt.setString(3, modelIn.getText()); pstmt.setString(4, yearIn.getText()); pstmt.setString(5, capIn.getText()); pstmt.setString(6, lugIn.getText()); pstmt.setString(7, typeIn.getValue()); pstmt.setString(8, fuelIn.getValue()); pstmt.setString(9, statusIn.getValue()); pstmt.setInt(10, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate(); 
                    sel.setRegNo(regIn.getText().toUpperCase()); sel.setMake(makeIn.getText()); sel.setModel(modelIn.getText()); sel.setYear(yearIn.getText()); sel.setCapacity(capIn.getText()); sel.setLuggage(lugIn.getText()); sel.setType(typeIn.getValue()); sel.setFuel(fuelIn.getValue()); sel.setStatus(statusIn.getValue()); 
                    table.refresh(); table.getSelectionModel().clearSelection();
                    regIn.clear(); makeIn.clear(); modelIn.clear(); yearIn.clear(); capIn.clear(); lugIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Car sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Car WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    carData.remove(sel); table.getSelectionModel().clearSelection();
                    regIn.clear(); makeIn.clear(); modelIn.clear(); yearIn.clear(); capIn.clear(); lugIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Fleet Management"), table, form); return layout;
    }

    // --- DRIVER VIEW ---
    private VBox createDriverView() {
        VBox layout = new VBox(15);
        TableView<Driver> table = new TableView<>(driverData);
        table.getColumns().addAll(createCol("ID", "id", 50), createCol("First Name", "firstName", 150), createCol("Last Name", "lastName", 150), createCol("Phone", "phone", 120), createCol("License", "license", 150), createCol("Shift", "shift", 100), createCol("Status", "status", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField fNameIn = new TextField(); TextField lNameIn = new TextField(); TextField phoneIn = new TextField(); TextField licIn = new TextField();
        ComboBox<String> shiftIn = new ComboBox<>(FXCollections.observableArrayList("Morning", "Night", "Split")); shiftIn.setValue("Morning");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Active", "On Leave", "Suspended")); statusIn.setValue("Active");
        
        Button addBtn = new Button("Add Driver"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("First Name:"), fNameIn, new Label("Last Name:"), lNameIn, new Label("Shift:"), shiftIn);
        form.addRow(1, new Label("Phone:"), phoneIn, new Label("License:"), licIn, new Label("Status:"), statusIn);
        form.addRow(2, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                fNameIn.setText(newSel.getFirstName()); lNameIn.setText(newSel.getLastName()); phoneIn.setText(newSel.getPhone()); licIn.setText(newSel.getLicense()); shiftIn.setValue(newSel.getShift()); statusIn.setValue(newSel.getStatus()); 
                updateBtn.setDisable(false); deleteBtn.setDisable(false); addBtn.setDisable(true); 
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if (driverData.stream().anyMatch(d -> d.getLicense().equalsIgnoreCase(licIn.getText()))) {
                showAlert("Duplicate Error", "A driver with this License Number already exists!"); return;
            }
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Driver (firstName, lastName, phone, license, shift, status) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, fNameIn.getText()); pstmt.setString(2, lNameIn.getText()); pstmt.setString(3, phoneIn.getText()); pstmt.setString(4, licIn.getText()); pstmt.setString(5, shiftIn.getValue()); pstmt.setString(6, statusIn.getValue());
                pstmt.executeUpdate(); ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) driverData.add(new Driver(String.valueOf(rs.getInt(1)), fNameIn.getText(), lNameIn.getText(), phoneIn.getText(), licIn.getText(), shiftIn.getValue(), statusIn.getValue()));
                fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Driver sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Driver SET firstName=?, lastName=?, phone=?, license=?, shift=?, status=? WHERE id=?")) {
                    pstmt.setString(1, fNameIn.getText()); pstmt.setString(2, lNameIn.getText()); pstmt.setString(3, phoneIn.getText()); pstmt.setString(4, licIn.getText()); pstmt.setString(5, shiftIn.getValue()); pstmt.setString(6, statusIn.getValue()); pstmt.setInt(7, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate(); 
                    sel.setFirstName(fNameIn.getText()); sel.setLastName(lNameIn.getText()); sel.setPhone(phoneIn.getText()); sel.setLicense(licIn.getText()); sel.setShift(shiftIn.getValue()); sel.setStatus(statusIn.getValue()); 
                    table.refresh(); table.getSelectionModel().clearSelection();
                    fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Driver sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Driver WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    driverData.remove(sel); table.getSelectionModel().clearSelection();
                    fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Driver Management"), table, form); return layout;
    }

    // --- BOOKING VIEW ---
    private VBox createBookingView() {
        VBox layout = new VBox(15);
        TableView<Booking> table = new TableView<>(bookingData);
        table.getColumns().addAll(createCol("ID", "id", 50), createCol("Company", "company", 130), createCol("Employee", "employee", 100), createCol("Service", "serviceType", 110), createCol("Car", "carRegNo", 120), createCol("Fuel", "fuelType", 70), createCol("Driver", "driver", 110), createCol("Date", "date", 90), createCol("Status", "status", 90));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        ComboBox<String> compIn = new ComboBox<>(); companyData.forEach(c -> compIn.getItems().add(c.getName()));
        TextField empIn = new TextField(); ComboBox<String> serviceIn = new ComboBox<>(serviceTypeList); serviceIn.setEditable(true);
        DatePicker dateIn = new DatePicker(LocalDate.now()); ComboBox<String> carIn = new ComboBox<>(); 
        TextField typeIn = new TextField(); typeIn.setEditable(false); TextField fuelIn = new TextField(); fuelIn.setEditable(false);
        ComboBox<String> driverIn = new ComboBox<>(); driverIn.getItems().add("Unassigned"); driverData.forEach(d -> driverIn.getItems().add(d.getFirstName() + " " + d.getLastName())); driverIn.setValue("Unassigned");
        TextField pickupIn = new TextField(); TextField dropIn = new TextField(); TextField timeIn = new TextField("10:00 AM");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Pending", "Confirmed", "Completed", "Cancelled")); statusIn.setValue("Pending");
        
        Button addBtn = new Button("Create Booking"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);
        
        dateIn.valueProperty().addListener((obs, old, newVal) -> refreshAvailableCars(newVal, carIn, null));
        carIn.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) { String regNo = newVal.split(" - ")[0]; Car selectedCar = carData.stream().filter(c -> c.getRegNo().equals(regNo)).findFirst().orElse(null);
                if (selectedCar != null) { typeIn.setText(selectedCar.getType()); fuelIn.setText(selectedCar.getFuel()); }
            } else { typeIn.clear(); fuelIn.clear(); }
        });
        refreshAvailableCars(dateIn.getValue(), carIn, null);

        form.addRow(0, new Label("Company:"), compIn, new Label("Employee:"), empIn, new Label("Date:"), dateIn);
        form.addRow(1, new Label("Service:"), serviceIn, new Label("Assign Car:"), carIn, new Label("Driver:"), driverIn);
        form.addRow(2, new Label("Car Type:"), typeIn, new Label("Fuel:"), fuelIn, new Label("Time:"), timeIn);
        form.addRow(3, new Label("Pickup:"), pickupIn, new Label("Drop:"), dropIn, new Label("Status:"), statusIn);
        form.addRow(4, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                compIn.setValue(newSel.getCompany()); empIn.setText(newSel.getEmployee()); serviceIn.setValue(newSel.getServiceType()); dateIn.setValue(LocalDate.parse(newSel.getDate())); refreshAvailableCars(dateIn.getValue(), carIn, newSel.getCarRegNo()); carIn.setValue(carData.stream().filter(c -> c.getRegNo().equals(newSel.getCarRegNo())).map(c -> c.getRegNo() + " - " + c.getMake()).findFirst().orElse("")); driverIn.setValue(newSel.getDriver()); pickupIn.setText(newSel.getPickup()); dropIn.setText(newSel.getDropLoc()); statusIn.setValue(newSel.getStatus()); timeIn.setText(newSel.getTime());
                updateBtn.setDisable(false); deleteBtn.setDisable(false); addBtn.setDisable(true); 
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Booking (company, employee, serviceType, carRegNo, carType, fuelType, driver, pickup, dropLoc, date, time, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                String regNo = carIn.getValue() != null ? carIn.getValue().split(" - ")[0] : "";
                pstmt.setString(1, compIn.getValue()); pstmt.setString(2, empIn.getText()); pstmt.setString(3, serviceIn.getValue()); pstmt.setString(4, regNo); pstmt.setString(5, typeIn.getText()); pstmt.setString(6, fuelIn.getText()); pstmt.setString(7, driverIn.getValue()); pstmt.setString(8, pickupIn.getText()); pstmt.setString(9, dropIn.getText()); pstmt.setString(10, dateIn.getValue().toString()); pstmt.setString(11, timeIn.getText()); pstmt.setString(12, statusIn.getValue());
                pstmt.executeUpdate(); ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) bookingData.add(new Booking(String.valueOf(rs.getInt(1)), compIn.getValue(), empIn.getText(), serviceIn.getValue(), regNo, typeIn.getText(), fuelIn.getText(), driverIn.getValue(), pickupIn.getText(), dropIn.getText(), dateIn.getValue().toString(), timeIn.getText(), statusIn.getValue()));
                syncAllCarStatuses(conn); refreshAvailableCars(dateIn.getValue(), carIn, null); updateDashboardStats();
                empIn.clear(); pickupIn.clear(); dropIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Booking SET company=?, employee=?, serviceType=?, carRegNo=?, carType=?, fuelType=?, driver=?, pickup=?, dropLoc=?, date=?, time=?, status=? WHERE id=?")) {
                    String regNo = carIn.getValue() != null ? carIn.getValue().split(" - ")[0] : "";
                    pstmt.setString(1, compIn.getValue()); pstmt.setString(2, empIn.getText()); pstmt.setString(3, serviceIn.getValue()); pstmt.setString(4, regNo); pstmt.setString(5, typeIn.getText()); pstmt.setString(6, fuelIn.getText()); pstmt.setString(7, driverIn.getValue()); pstmt.setString(8, pickupIn.getText()); pstmt.setString(9, dropIn.getText()); pstmt.setString(10, dateIn.getValue().toString()); pstmt.setString(11, timeIn.getText()); pstmt.setString(12, statusIn.getValue()); pstmt.setInt(13, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate();
                    sel.setCompany(compIn.getValue()); sel.setEmployee(empIn.getText()); sel.setServiceType(serviceIn.getValue()); sel.setCarRegNo(regNo); sel.setCarType(typeIn.getText()); sel.setFuelType(fuelIn.getText()); sel.setDriver(driverIn.getValue()); sel.setPickup(pickupIn.getText()); sel.setDropLoc(dropIn.getText()); sel.setDate(dateIn.getValue().toString()); sel.setTime(timeIn.getText()); sel.setStatus(statusIn.getValue());
                    table.refresh(); table.getSelectionModel().clearSelection();
                    syncAllCarStatuses(conn); refreshAvailableCars(dateIn.getValue(), carIn, null); updateDashboardStats();
                    empIn.clear(); pickupIn.clear(); dropIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Booking WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    bookingData.remove(sel); table.getSelectionModel().clearSelection();
                    syncAllCarStatuses(conn); refreshAvailableCars(dateIn.getValue(), carIn, null); updateDashboardStats();
                    empIn.clear(); pickupIn.clear(); dropIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Booking Management"), table, form); return layout;
    }

    private void refreshAvailableCars(LocalDate date, ComboBox<String> carIn, String currentBookedCar) {
        carIn.getItems().clear(); if (date == null) return; String selectedDate = date.toString();
        for (Car c : carData) {
            boolean isBooked = bookingData.stream().anyMatch(b -> b.getDate().equals(selectedDate) && b.getCarRegNo().equals(c.getRegNo()) && !b.getStatus().equals("Cancelled") && (currentBookedCar == null || !currentBookedCar.equals(c.getRegNo())));
            if (!isBooked && !c.getStatus().equals("Maintenance")) carIn.getItems().add(c.getRegNo() + " - " + c.getMake() + " " + c.getModel());
        }
    }
    
    private void syncAllCarStatuses(Connection conn) throws SQLException {
        for (Car car : carData) {
            if (!"Maintenance".equals(car.getStatus())) {
                boolean active = bookingData.stream().anyMatch(b -> b.getCarRegNo().equals(car.getRegNo()) && ("Pending".equals(b.getStatus()) || "Confirmed".equals(b.getStatus())));
                String newStatus = active ? "On Trip" : "Available";
                if(!car.getStatus().equals(newStatus)) {
                    car.setStatus(newStatus);
                    try(PreparedStatement ps = conn.prepareStatement("UPDATE Car SET status=? WHERE id=?")) { ps.setString(1, newStatus); ps.setInt(2, Integer.parseInt(car.getId())); ps.executeUpdate(); }
                }
            }
        }
    }

    // --- RATE CARD VIEW ---
    private VBox createRateCardView() {
        VBox layout = new VBox(15);
        TableView<RateCard> table = new TableView<>(rateCardData);
        table.getColumns().addAll(createCol("ID", "id", 60), createCol("Company", "company", 160), createCol("Service", "serviceType", 140), createCol("Car Type", "carType", 90), createCol("Fuel", "fuelType", 90), createCol("Base Fare", "baseFare", 90), createCol("Extra KM/₹", "extraKmRate", 90), createCol("Extra Hr/₹", "extraHrRate", 90));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        ComboBox<String> compIn = new ComboBox<>(); companyData.forEach(c -> compIn.getItems().add(c.getName()));
        ComboBox<String> serviceIn = new ComboBox<>(serviceTypeList); serviceIn.setEditable(true);
        ComboBox<String> typeIn = new ComboBox<>(FXCollections.observableArrayList("Any", "Sedan", "SUV", "Luxury", "Hatchback")); typeIn.setValue("Any");
        ComboBox<String> fuelIn = new ComboBox<>(FXCollections.observableArrayList("Any", "Petrol", "Diesel", "EV")); fuelIn.setValue("Any");
        TextField baseFareIn = new TextField(); TextField inclKmIn = new TextField(); TextField inclHrsIn = new TextField(); TextField perKmIn = new TextField(); TextField perHrIn = new TextField();
        
        Button addBtn = new Button("Add Rate Card"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("Company:"), compIn, new Label("Service:"), serviceIn, new Label("Car Type:"), typeIn);
        form.addRow(1, new Label("Fuel Type:"), fuelIn, new Label("Base Fare:"), baseFareIn, new Label("Incl KM:"), inclKmIn);
        form.addRow(2, new Label("Incl Hrs:"), inclHrsIn, new Label("Extra KM:"), perKmIn, new Label("Extra Hr:"), perHrIn);
        form.addRow(3, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                compIn.setValue(newSel.getCompany()); serviceIn.setValue(newSel.getServiceType()); typeIn.setValue(newSel.getCarType()); fuelIn.setValue(newSel.getFuelType()); baseFareIn.setText(newSel.getBaseFare()); inclKmIn.setText(newSel.getInclKm()); inclHrsIn.setText(newSel.getInclHrs()); perKmIn.setText(newSel.getExtraKmRate()); perHrIn.setText(newSel.getExtraHrRate()); 
                updateBtn.setDisable(false); deleteBtn.setDisable(false); addBtn.setDisable(true); 
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO RateCard (company, serviceType, carType, fuelType, baseFare, inclKm, inclHrs, extraKmRate, extraHrRate) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, compIn.getValue()); pstmt.setString(2, serviceIn.getValue()); pstmt.setString(3, typeIn.getValue()); pstmt.setString(4, fuelIn.getValue()); pstmt.setString(5, baseFareIn.getText()); pstmt.setString(6, inclKmIn.getText()); pstmt.setString(7, inclHrsIn.getText()); pstmt.setString(8, perKmIn.getText()); pstmt.setString(9, perHrIn.getText());
                pstmt.executeUpdate(); ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) rateCardData.add(new RateCard(String.valueOf(rs.getInt(1)), compIn.getValue(), serviceIn.getValue(), typeIn.getValue(), fuelIn.getValue(), baseFareIn.getText(), inclKmIn.getText(), inclHrsIn.getText(), perKmIn.getText(), perHrIn.getText()));
                baseFareIn.clear(); inclKmIn.clear(); inclHrsIn.clear(); perKmIn.clear(); perHrIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            RateCard sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE RateCard SET company=?, serviceType=?, carType=?, fuelType=?, baseFare=?, inclKm=?, inclHrs=?, extraKmRate=?, extraHrRate=? WHERE id=?")) {
                    pstmt.setString(1, compIn.getValue()); pstmt.setString(2, serviceIn.getValue()); pstmt.setString(3, typeIn.getValue()); pstmt.setString(4, fuelIn.getValue()); pstmt.setString(5, baseFareIn.getText()); pstmt.setString(6, inclKmIn.getText()); pstmt.setString(7, inclHrsIn.getText()); pstmt.setString(8, perKmIn.getText()); pstmt.setString(9, perHrIn.getText()); pstmt.setInt(10, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate(); 
                    sel.setCompany(compIn.getValue()); sel.setServiceType(serviceIn.getValue()); sel.setCarType(typeIn.getValue()); sel.setFuelType(fuelIn.getValue()); sel.setBaseFare(baseFareIn.getText()); sel.setInclKm(inclKmIn.getText()); sel.setInclHrs(inclHrsIn.getText()); sel.setExtraKmRate(perKmIn.getText()); sel.setExtraHrRate(perHrIn.getText());
                    table.refresh(); table.getSelectionModel().clearSelection();
                    baseFareIn.clear(); inclKmIn.clear(); inclHrsIn.clear(); perKmIn.clear(); perHrIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            RateCard sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM RateCard WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    rateCardData.remove(sel); table.getSelectionModel().clearSelection();
                    baseFareIn.clear(); inclKmIn.clear(); inclHrsIn.clear(); perKmIn.clear(); perHrIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Dynamic Rate Card Configuration"), table, form); return layout;
    }

    // --- BILLING / INVOICE VIEW ---
    private VBox createBillingView() {
        VBox layout = new VBox(15);
        TableView<Invoice> table = new TableView<>(invoiceData);
        table.getColumns().addAll(
            createCol("Inv ID", "id", 50), 
            createCol("BKG Ref", "bookingRef", 60), 
            createCol("Company", "company", 130), 
            createCol("KM", "distance", 50), 
            createCol("Hrs", "hours", 50), 
            createCol("Base", "baseFare", 60), 
            createCol("Extra KM", "distCharge", 70), 
            createCol("Extra Hr", "hrCharge", 70), 
            createCol("Tolls", "tolls", 50), 
            createCol("Tax", "tax", 50), 
            createCol("Total", "total", 80), 
            createCol("Status", "status", 70)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        ComboBox<String> bkgIn = new ComboBox<>(); bookingData.forEach(b -> bkgIn.getItems().add(b.getId() + " - " + b.getCompany()));
        TextField distanceIn = new TextField(); TextField hoursIn = new TextField(); TextField tollsIn = new TextField("0.00"); TextField taxPctIn = new TextField("5.00");
        TextField carRegIn = new TextField(); carRegIn.setEditable(false); TextField baseFareIn = new TextField(); baseFareIn.setEditable(false);
        TextField inclKmIn = new TextField(); inclKmIn.setEditable(false); TextField inclHrsIn = new TextField(); inclHrsIn.setEditable(false);
        TextField extraKmRateIn = new TextField(); extraKmRateIn.setEditable(false); TextField extraHrRateIn = new TextField(); extraHrRateIn.setEditable(false);
        ComboBox<String> payModeIn = new ComboBox<>(FXCollections.observableArrayList("Credit Card", "Bank Transfer", "Wallet", "Cash")); payModeIn.setValue("Wallet");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Unpaid", "Paid", "Void")); statusIn.setValue("Unpaid");
        
        Button calcBtn = new Button("Generate Invoice"); calcBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        Button updateBtn = new Button("Update Invoice"); updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        bkgIn.valueProperty().addListener((obs, old, newVal) -> {
            if(newVal != null) {
                String bkgId = newVal.split(" - ")[0]; Booking b = bookingData.stream().filter(bk -> bk.getId().equals(bkgId)).findFirst().orElse(null);
                if(b != null) {
                    carRegIn.setText(b.getCarRegNo() + " (" + b.getCarType() + ")");
                    boolean found = false;
                    for(RateCard rc : rateCardData) {
                        if(b.getCompany().equalsIgnoreCase(rc.getCompany()) && b.getServiceType().equalsIgnoreCase(rc.getServiceType()) && (rc.getCarType().equals("Any") || b.getCarType().equalsIgnoreCase(rc.getCarType()))) {
                            baseFareIn.setText(rc.getBaseFare()); inclKmIn.setText(rc.getInclKm()); inclHrsIn.setText(rc.getInclHrs()); extraKmRateIn.setText(rc.getExtraKmRate()); extraHrRateIn.setText(rc.getExtraHrRate()); found = true; break;
                        }
                    }
                    if(!found) { baseFareIn.setText("0"); inclKmIn.setText("0"); extraKmRateIn.setText("0"); extraHrRateIn.setText("0"); }
                }
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) {
                bkgIn.getItems().stream().filter(item -> item.startsWith(newSel.getBookingRef())).findFirst().ifPresent(bkgIn::setValue);
                distanceIn.setText(newSel.getDistance()); hoursIn.setText(newSel.getHours()); tollsIn.setText(newSel.getTolls());
                payModeIn.setValue(newSel.getPayMode()); statusIn.setValue(newSel.getStatus());
                updateBtn.setDisable(false); deleteBtn.setDisable(false); calcBtn.setDisable(true);
            } else { updateBtn.setDisable(true); deleteBtn.setDisable(true); calcBtn.setDisable(false); }
        });

        form.addRow(0, new Label("Select BKG:"), bkgIn, new Label("Vehicle:"), carRegIn, new Label("Status:"), statusIn);
        form.addRow(1, new Label("KM Run:"), distanceIn, new Label("Hours:"), hoursIn, new Label("Tolls:"), tollsIn);
        form.addRow(2, new Label("Base Fare:"), baseFareIn, new Label("Tax Pct:"), taxPctIn, new Label("Pay Mode:"), payModeIn);
        form.addRow(3, new Label("Extra KM Rate:"), extraKmRateIn, new Label("Extra Hr Rate:"), extraHrRateIn, new Label(""), new HBox(10, calcBtn, updateBtn, deleteBtn));

        calcBtn.setOnAction(e -> {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Invoice (bookingRef, company, carRegNo, distance, hours, baseFare, distCharge, hrCharge, tolls, tax, total, payMode, status, date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                String bRef = bkgIn.getValue().split(" - ")[0]; String comp = bkgIn.getValue().split(" - ")[1];
                double dist = parseDoubleSafe(distanceIn.getText()), hrs = parseDoubleSafe(hoursIn.getText()), base = parseDoubleSafe(baseFareIn.getText()), eKm = parseDoubleSafe(extraKmRateIn.getText()), eHr = parseDoubleSafe(extraHrRateIn.getText()), toll = parseDoubleSafe(tollsIn.getText()), tx = parseDoubleSafe(taxPctIn.getText());
                double dChg = Math.max(0, dist - parseDoubleSafe(inclKmIn.getText())) * eKm;
                double hChg = Math.max(0, hrs - parseDoubleSafe(inclHrsIn.getText())) * eHr;
                double sub = base + dChg + hChg + toll; double tAmt = sub * (tx/100); double total = sub + tAmt;
                
                pstmt.setString(1, bRef); pstmt.setString(2, comp); pstmt.setString(3, carRegIn.getText()); pstmt.setString(4, String.valueOf(dist)); pstmt.setString(5, String.valueOf(hrs)); pstmt.setString(6, String.format("%.2f", base)); pstmt.setString(7, String.format("%.2f", dChg)); pstmt.setString(8, String.format("%.2f", hChg)); pstmt.setString(9, String.format("%.2f", toll)); pstmt.setString(10, String.format("%.2f", tAmt)); pstmt.setString(11, String.format("%.2f", total)); pstmt.setString(12, payModeIn.getValue()); pstmt.setString(13, statusIn.getValue()); pstmt.setString(14, LocalDateTime.now().toString());
                pstmt.executeUpdate(); ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()) invoiceData.add(new Invoice(String.valueOf(rs.getInt(1)), bRef, comp, carRegIn.getText(), String.valueOf(dist), String.valueOf(hrs), String.format("%.2f", base), String.format("%.2f", dChg), String.format("%.2f", hChg), String.format("%.2f", toll), String.format("%.2f", tAmt), String.format("%.2f", total), payModeIn.getValue(), statusIn.getValue(), ""));
                updateDashboardStats(); distanceIn.clear(); hoursIn.clear(); tollsIn.setText("0.00");
                showAlert("Success", "Invoice Saved to DB! Total: ₹" + String.format("%.2f", total));
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Invoice sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Invoice SET distance=?, hours=?, distCharge=?, hrCharge=?, tolls=?, tax=?, total=?, payMode=?, status=? WHERE id=?")) {
                    double dist = parseDoubleSafe(distanceIn.getText()), hrs = parseDoubleSafe(hoursIn.getText()), base = parseDoubleSafe(baseFareIn.getText()), eKm = parseDoubleSafe(extraKmRateIn.getText()), eHr = parseDoubleSafe(extraHrRateIn.getText()), toll = parseDoubleSafe(tollsIn.getText()), tx = parseDoubleSafe(taxPctIn.getText());
                    double dChg = Math.max(0, dist - parseDoubleSafe(inclKmIn.getText())) * eKm; double hChg = Math.max(0, hrs - parseDoubleSafe(inclHrsIn.getText())) * eHr;
                    double sub = base + dChg + hChg + toll; double tAmt = sub * (tx/100); double total = sub + tAmt;
                    
                    pstmt.setString(1, String.valueOf(dist)); pstmt.setString(2, String.valueOf(hrs)); pstmt.setString(3, String.format("%.2f", dChg)); pstmt.setString(4, String.format("%.2f", hChg)); pstmt.setString(5, String.format("%.2f", toll)); pstmt.setString(6, String.format("%.2f", tAmt)); pstmt.setString(7, String.format("%.2f", total)); pstmt.setString(8, payModeIn.getValue()); pstmt.setString(9, statusIn.getValue()); pstmt.setInt(10, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate();
                    sel.setDistance(String.valueOf(dist)); sel.setHours(String.valueOf(hrs)); sel.setTolls(String.format("%.2f", toll)); sel.setDistCharge(String.format("%.2f", dChg)); sel.setHrCharge(String.format("%.2f", hChg)); sel.setTax(String.format("%.2f", tAmt)); sel.setTotal(String.format("%.2f", total)); sel.setPayMode(payModeIn.getValue()); sel.setStatus(statusIn.getValue());
                    table.refresh(); table.getSelectionModel().clearSelection(); updateDashboardStats();
                    distanceIn.clear(); hoursIn.clear(); tollsIn.setText("0.00");
                    showAlert("Success", "Invoice Updated in DB! New Total: ₹" + String.format("%.2f", total));
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Invoice sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Invoice WHERE id=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate();
                    invoiceData.remove(sel); table.getSelectionModel().clearSelection(); updateDashboardStats();
                    distanceIn.clear(); hoursIn.clear(); tollsIn.setText("0.00");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Billing Operations (JDBC Connected)"), table, form); return layout;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
    
    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this record?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion"); alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private double parseDoubleSafe(String val) { try { return (val==null || val.trim().isEmpty()) ? 0.0 : Double.parseDouble(val.trim()); } catch (Exception e) { return 0.0; } }

    private <T> TableColumn<T, String> createCol(String title, String property, double minWidth) {
        TableColumn<T, String> col = new TableColumn<>(title); col.setCellValueFactory(new PropertyValueFactory<>(property)); col.setMinWidth(minWidth); return col;
    }

    private String createInlineCSS() {
        return "data:text/css," +
               ".table-view { -fx-background-color: transparent; -fx-border-color: #d2d6de; } .table-view .column-header-background { -fx-background-color: #e9ecef; } " +
               ".action-button { -fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .action-button:hover { -fx-background-color: #3498db; } " +
               ".update-button { -fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .update-button:hover { -fx-background-color: #e67e22; } " +
               ".delete-button { -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .delete-button:hover { -fx-background-color: #c0392b; }";
    }

    // =========================================================================
    // DATA MODELS
    // =========================================================================
    
    public static class Company {
        private final SimpleStringProperty id, name, contact, email, phone, address;
        public Company(String id, String name, String contact, String email, String phone, String address) { this.id = new SimpleStringProperty(id); this.name = new SimpleStringProperty(name); this.contact = new SimpleStringProperty(contact); this.email = new SimpleStringProperty(email); this.phone = new SimpleStringProperty(phone); this.address = new SimpleStringProperty(address); }
        public String getId() { return id.get(); } public String getName() { return name.get(); } public String getContact() { return contact.get(); } public String getEmail() { return email.get(); } public String getPhone() { return phone.get(); } public String getAddress() { return address.get(); }
        public void setName(String val) { name.set(val); } public void setContact(String val) { contact.set(val); } public void setEmail(String val) { email.set(val); } public void setPhone(String val) { phone.set(val); } public void setAddress(String val) { address.set(val); }
    }

    public static class Car {
        private final SimpleStringProperty id, regNo, make, model, year, capacity, luggage, type, fuel, status;
        public Car(String id, String regNo, String make, String model, String year, String capacity, String luggage, String type, String fuel, String status) { this.id = new SimpleStringProperty(id); this.regNo = new SimpleStringProperty(regNo); this.make = new SimpleStringProperty(make); this.model = new SimpleStringProperty(model); this.year = new SimpleStringProperty(year); this.capacity = new SimpleStringProperty(capacity); this.luggage = new SimpleStringProperty(luggage); this.type = new SimpleStringProperty(type); this.fuel = new SimpleStringProperty(fuel); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getRegNo() { return regNo.get(); } public String getMake() { return make.get(); } public String getModel() { return model.get(); } public String getYear() { return year.get(); } public String getCapacity() { return capacity.get(); } public String getLuggage() { return luggage.get(); } public String getType() { return type.get(); } public String getFuel() { return fuel.get(); } public String getStatus() { return status.get(); }
        public void setRegNo(String v){regNo.set(v);} public void setMake(String v){make.set(v);} public void setModel(String v){model.set(v);} public void setYear(String v){year.set(v);} public void setCapacity(String v){capacity.set(v);} public void setLuggage(String v){luggage.set(v);} public void setType(String v){type.set(v);} public void setFuel(String v){fuel.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class Driver {
        private final SimpleStringProperty id, firstName, lastName, phone, license, shift, status;
        public Driver(String id, String fName, String lName, String phone, String license, String shift, String status) { this.id = new SimpleStringProperty(id); this.firstName = new SimpleStringProperty(fName); this.lastName = new SimpleStringProperty(lName); this.phone = new SimpleStringProperty(phone); this.license = new SimpleStringProperty(license); this.shift = new SimpleStringProperty(shift); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getFirstName() { return firstName.get(); } public String getLastName() { return lastName.get(); } public String getPhone() { return phone.get(); } public String getLicense() { return license.get(); } public String getShift() { return shift.get(); } public String getStatus() { return status.get(); }
        public void setFirstName(String v){firstName.set(v);} public void setLastName(String v){lastName.set(v);} public void setPhone(String v){phone.set(v);} public void setLicense(String v){license.set(v);} public void setShift(String v){shift.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class Booking {
        private final SimpleStringProperty id, company, employee, serviceType, carRegNo, carType, fuelType, driver, pickup, dropLoc, date, time, status;
        public Booking(String id, String company, String employee, String serviceType, String carRegNo, String carType, String fuelType, String driver, String pickup, String dropLoc, String date, String time, String status) { this.id = new SimpleStringProperty(id); this.company = new SimpleStringProperty(company); this.employee = new SimpleStringProperty(employee); this.serviceType = new SimpleStringProperty(serviceType); this.carRegNo = new SimpleStringProperty(carRegNo); this.carType = new SimpleStringProperty(carType); this.fuelType = new SimpleStringProperty(fuelType); this.driver = new SimpleStringProperty(driver); this.pickup = new SimpleStringProperty(pickup); this.dropLoc = new SimpleStringProperty(dropLoc); this.date = new SimpleStringProperty(date); this.time = new SimpleStringProperty(time); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getCompany() { return company.get(); } public String getEmployee() { return employee.get(); } public String getServiceType() { return serviceType.get(); } public String getCarRegNo() { return carRegNo.get(); } public String getCarType() { return carType.get(); } public String getFuelType() { return fuelType.get(); } public String getDriver() { return driver.get(); } public String getPickup() { return pickup.get(); } public String getDropLoc() { return dropLoc.get(); } public String getDate() { return date.get(); } public String getTime() { return time.get(); } public String getStatus() { return status.get(); }
        public void setCompany(String v){company.set(v);} public void setEmployee(String v){employee.set(v);} public void setServiceType(String v){serviceType.set(v);} public void setCarRegNo(String v){carRegNo.set(v);} public void setCarType(String v){carType.set(v);} public void setFuelType(String v){fuelType.set(v);} public void setDriver(String v){driver.set(v);} public void setPickup(String v){pickup.set(v);} public void setDropLoc(String v){dropLoc.set(v);} public void setDate(String v){date.set(v);} public void setTime(String v){time.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class RateCard {
        private final SimpleStringProperty id, company, serviceType, carType, fuelType, baseFare, inclKm, inclHrs, extraKmRate, extraHrRate;
        public RateCard(String id, String company, String serviceType, String carType, String fuelType, String baseFare, String inclKm, String inclHrs, String extraKmRate, String extraHrRate) { this.id = new SimpleStringProperty(id); this.company = new SimpleStringProperty(company); this.serviceType = new SimpleStringProperty(serviceType); this.carType = new SimpleStringProperty(carType); this.fuelType = new SimpleStringProperty(fuelType); this.baseFare = new SimpleStringProperty(baseFare); this.inclKm = new SimpleStringProperty(inclKm); this.inclHrs = new SimpleStringProperty(inclHrs); this.extraKmRate = new SimpleStringProperty(extraKmRate); this.extraHrRate = new SimpleStringProperty(extraHrRate); }
        public String getId() { return id.get(); } public String getCompany() { return company.get(); } public String getServiceType() { return serviceType.get(); } public String getCarType() { return carType.get(); } public String getFuelType() { return fuelType.get(); } public String getBaseFare() { return baseFare.get(); } public String getInclKm() { return inclKm.get(); } public String getInclHrs() { return inclHrs.get(); } public String getExtraKmRate() { return extraKmRate.get(); } public String getExtraHrRate() { return extraHrRate.get(); }
        public void setCompany(String v){company.set(v);} public void setServiceType(String v){serviceType.set(v);} public void setCarType(String v){carType.set(v);} public void setFuelType(String v){fuelType.set(v);} public void setBaseFare(String v){baseFare.set(v);} public void setInclKm(String v){inclKm.set(v);} public void setInclHrs(String v){inclHrs.set(v);} public void setExtraKmRate(String v){extraKmRate.set(v);} public void setExtraHrRate(String v){extraHrRate.set(v);}
    }

    public static class Invoice {
        private final SimpleStringProperty id, bookingRef, company, carRegNo, distance, hours, baseFare, distCharge, hrCharge, tolls, tax, total, payMode, status, date;
        public Invoice(String id, String bookingRef, String company, String carRegNo, String distance, String hours, String baseFare, String distCharge, String hrCharge, String tolls, String tax, String total, String payMode, String status, String date) { this.id = new SimpleStringProperty(id); this.bookingRef = new SimpleStringProperty(bookingRef); this.company = new SimpleStringProperty(company); this.carRegNo = new SimpleStringProperty(carRegNo); this.distance = new SimpleStringProperty(distance); this.hours = new SimpleStringProperty(hours); this.baseFare = new SimpleStringProperty(baseFare); this.distCharge = new SimpleStringProperty(distCharge); this.hrCharge = new SimpleStringProperty(hrCharge); this.tolls = new SimpleStringProperty(tolls); this.tax = new SimpleStringProperty(tax); this.total = new SimpleStringProperty(total); this.payMode = new SimpleStringProperty(payMode); this.status = new SimpleStringProperty(status); this.date = new SimpleStringProperty(date); }
        public String getId() { return id.get(); } public String getBookingRef() { return bookingRef.get(); } public String getCompany() { return company.get(); } public String getCarRegNo() { return carRegNo.get(); } public String getDistance() { return distance.get(); } public String getHours() { return hours.get(); } public String getBaseFare() { return baseFare.get(); } public String getDistCharge() { return distCharge.get(); } public String getHrCharge() { return hrCharge.get(); } public String getTolls() { return tolls.get(); } public String getTax() { return tax.get(); } public String getTotal() { return total.get(); } public String getPayMode() { return payMode.get(); } public String getStatus() { return status.get(); } public String getDate() { return date.get(); }
        public void setDistance(String v) { distance.set(v); } public void setHours(String v) { hours.set(v); } public void setTolls(String v) { tolls.set(v); } public void setTotal(String v) { total.set(v); } public void setStatus(String v) { status.set(v); }
        public void setDistCharge(String v) { distCharge.set(v); } public void setHrCharge(String v) { hrCharge.set(v); } public void setTax(String v) { tax.set(v); } public void setPayMode(String v) { payMode.set(v); }
    }
}
