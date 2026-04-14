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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CORPORATE CAR RENTAL SYSTEM - FULL JDBC DATABASE UI (TRUE RELATIONAL)
 * ----------------------------------------------------
 * Features 100% strict Foreign Key relationships across 31 tables (including ServiceType).
 * Fully Dropdown-Driven UI: Cars, Drivers, Taxes, Services, and Employees auto-populate 
 * based on live database queries to enforce true relational data integrity.
 * ALL MODULES feature Add, Update, and Delete functionality.
 */
public class App extends Application {

    // =========================================================================
    // ⚙️ DATABASE CONFIGURATION
    // =========================================================================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/corporate_rental?createDatabaseIfNotExist=true";
    private static final String DB_USER = "root";       
    private static final String DB_PASS = "M@@L@XMIt@1962@*#";

    // --- OBSERVABLE LISTS (UI Binding via JOINs) ---
    private final ObservableList<Company> companyData = FXCollections.observableArrayList();
    private final ObservableList<Car> carData = FXCollections.observableArrayList();
    private final ObservableList<Driver> driverData = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();
    private final ObservableList<RateCard> rateCardData = FXCollections.observableArrayList();
    private final ObservableList<Invoice> invoiceData = FXCollections.observableArrayList();

    // Dynamic Smart Engine State Variables
    private final List<String> currentDynamicColumns = new ArrayList<>();
    private final List<Control> currentDynamicFields = new ArrayList<>();
    private final List<FKConfig> currentDynamicFKs = new ArrayList<>();

    // --- UI COMPONENTS ---
    private StackPane contentArea;
    private Label headerTitle, revLabel, pendingLabel;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        setupDatabaseTables();
        
        try {
            loadAllDataFromDB();
        } catch (Exception e) {
            System.err.println("Fatal Error loading DB data: " + e.getMessage());
            e.printStackTrace();
        }

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

        primaryStage.setTitle("Corporate ERP: Deep Relational Architecture");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // =========================================================================
    // 🗄️ TRUE RELATIONAL DATABASE SETUP
    // =========================================================================

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void setupDatabaseTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            boolean requiresRebuild = false;
            try { stmt.executeQuery("SELECT ServiceTypeID FROM Booking LIMIT 1"); } 
            catch (SQLException e) { requiresRebuild = true; }

            if (requiresRebuild) {
                System.out.println("🔄 Old Schema Detected! Nudging schema upgrade to include Service Types...");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                String[] dropTables = {
                    "InvoiceDetails", "Payment", "Invoice", "DriverRating", "Trip", 
                    "BookingHistory", "Booking", "BookingStatus", "DistanceMatrix", 
                    "Route", "Location", "DriverAttendance", "DriverDocuments", 
                    "DriverShift", "Driver", "CarDocuments", "CarInsurance", "CarFeatures", 
                    "FuelLog", "Maintenance", "BreakdownLog", "Car", "FuelType", "RateCard", 
                    "ServiceType", "CarType", "UserAccount", "Employee", "Department", "Company", "Tax"
                };
                for (String t : dropTables) stmt.execute("DROP TABLE IF EXISTS " + t);
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                System.out.println("✅ Outdated tables dropped successfully.");
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            String[] tables = {
                "CREATE TABLE IF NOT EXISTS Company (CompanyID INT AUTO_INCREMENT PRIMARY KEY, CompanyName VARCHAR(255) NOT NULL, ContactPerson VARCHAR(255), ContactEmail VARCHAR(255) UNIQUE, ContactPhone VARCHAR(50), BillingAddress TEXT)",
                "CREATE TABLE IF NOT EXISTS Department (DepartmentID INT AUTO_INCREMENT PRIMARY KEY, CompanyID INT NOT NULL, DepartmentName VARCHAR(255) NOT NULL, FOREIGN KEY (CompanyID) REFERENCES Company(CompanyID) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS Employee (EmployeeID INT AUTO_INCREMENT PRIMARY KEY, CompanyID INT NOT NULL, DepartmentID INT NOT NULL, FirstName VARCHAR(100) NOT NULL, LastName VARCHAR(100) NOT NULL, Email VARCHAR(255) UNIQUE, FOREIGN KEY (CompanyID) REFERENCES Company(CompanyID), FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID))",
                "CREATE TABLE IF NOT EXISTS UserAccount (UserID INT AUTO_INCREMENT PRIMARY KEY, EmployeeID INT, Username VARCHAR(100) UNIQUE NOT NULL, PasswordHash VARCHAR(255) NOT NULL, Role VARCHAR(50) NOT NULL, FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE SET NULL)",
                
                "CREATE TABLE IF NOT EXISTS ServiceType (ServiceTypeID INT AUTO_INCREMENT PRIMARY KEY, ServiceName VARCHAR(100) UNIQUE NOT NULL)",
                "CREATE TABLE IF NOT EXISTS CarType (CarTypeID INT AUTO_INCREMENT PRIMARY KEY, TypeName VARCHAR(100) NOT NULL, Capacity INT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS FuelType (FuelTypeID INT AUTO_INCREMENT PRIMARY KEY, FuelName VARCHAR(50) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS Car (CarID INT AUTO_INCREMENT PRIMARY KEY, CarTypeID INT NOT NULL, FuelTypeID INT NOT NULL, RegistrationNumber VARCHAR(100) UNIQUE NOT NULL, Make VARCHAR(100), Model VARCHAR(100), ManufacturingYear INT, CurrentStatus VARCHAR(50) DEFAULT 'Available', FOREIGN KEY (CarTypeID) REFERENCES CarType(CarTypeID), FOREIGN KEY (FuelTypeID) REFERENCES FuelType(FuelTypeID))",
                "CREATE TABLE IF NOT EXISTS CarFeatures (FeatureID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, FeatureName VARCHAR(255) NOT NULL, FOREIGN KEY (CarID) REFERENCES Car(CarID) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS CarInsurance (InsuranceID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, PolicyNumber VARCHAR(100) UNIQUE NOT NULL, ExpiryDate DATE NOT NULL, FOREIGN KEY (CarID) REFERENCES Car(CarID) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS CarDocuments (DocumentID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, DocumentType VARCHAR(100) NOT NULL, ValidityDate DATE NOT NULL, FOREIGN KEY (CarID) REFERENCES Car(CarID) ON DELETE CASCADE)",
                
                "CREATE TABLE IF NOT EXISTS Driver (DriverID INT AUTO_INCREMENT PRIMARY KEY, FirstName VARCHAR(100) NOT NULL, LastName VARCHAR(100) NOT NULL, Phone VARCHAR(50) UNIQUE NOT NULL, LicenseNumber VARCHAR(100) UNIQUE NOT NULL, Status VARCHAR(50) DEFAULT 'Active')",
                "CREATE TABLE IF NOT EXISTS DriverDocuments (DocID INT AUTO_INCREMENT PRIMARY KEY, DriverID INT NOT NULL, DocType VARCHAR(100) NOT NULL, ExpiryDate DATE, FOREIGN KEY (DriverID) REFERENCES Driver(DriverID) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS DriverShift (ShiftID INT AUTO_INCREMENT PRIMARY KEY, ShiftName VARCHAR(50) NOT NULL, StartTime TIME NOT NULL, EndTime TIME NOT NULL)",
                "CREATE TABLE IF NOT EXISTS DriverAttendance (AttendanceID INT AUTO_INCREMENT PRIMARY KEY, DriverID INT NOT NULL, ShiftID INT NOT NULL, WorkDate DATE NOT NULL, Status VARCHAR(50) DEFAULT 'Present', FOREIGN KEY (DriverID) REFERENCES Driver(DriverID), FOREIGN KEY (ShiftID) REFERENCES DriverShift(ShiftID))",
                
                "CREATE TABLE IF NOT EXISTS Location (LocationID INT AUTO_INCREMENT PRIMARY KEY, LocationName VARCHAR(255) NOT NULL, Address TEXT)",
                "CREATE TABLE IF NOT EXISTS Route (RouteID INT AUTO_INCREMENT PRIMARY KEY, StartLocationID INT NOT NULL, EndLocationID INT NOT NULL, FOREIGN KEY (StartLocationID) REFERENCES Location(LocationID), FOREIGN KEY (EndLocationID) REFERENCES Location(LocationID))",
                "CREATE TABLE IF NOT EXISTS DistanceMatrix (MatrixID INT AUTO_INCREMENT PRIMARY KEY, FromLocationID INT NOT NULL, ToLocationID INT NOT NULL, DistanceKM DECIMAL(10,2) NOT NULL, FOREIGN KEY (FromLocationID) REFERENCES Location(LocationID), FOREIGN KEY (ToLocationID) REFERENCES Location(LocationID))",
                
                "CREATE TABLE IF NOT EXISTS BookingStatus (StatusID INT AUTO_INCREMENT PRIMARY KEY, StatusName VARCHAR(50) UNIQUE NOT NULL)",
                "CREATE TABLE IF NOT EXISTS Booking (BookingID INT AUTO_INCREMENT PRIMARY KEY, CompanyID INT NOT NULL, EmployeeID INT NOT NULL, CarTypeID INT NOT NULL, ServiceTypeID INT NOT NULL, PickupLocationID INT NOT NULL, DropLocationID INT NOT NULL, PickupTime DATETIME NOT NULL, StatusID INT NOT NULL, CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (CompanyID) REFERENCES Company(CompanyID), FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID), FOREIGN KEY (CarTypeID) REFERENCES CarType(CarTypeID), FOREIGN KEY (ServiceTypeID) REFERENCES ServiceType(ServiceTypeID), FOREIGN KEY (PickupLocationID) REFERENCES Location(LocationID), FOREIGN KEY (DropLocationID) REFERENCES Location(LocationID), FOREIGN KEY (StatusID) REFERENCES BookingStatus(StatusID))",
                "CREATE TABLE IF NOT EXISTS BookingHistory (HistoryID INT AUTO_INCREMENT PRIMARY KEY, BookingID INT NOT NULL, StatusID INT NOT NULL, ChangeTimestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (BookingID) REFERENCES Booking(BookingID) ON DELETE CASCADE, FOREIGN KEY (StatusID) REFERENCES BookingStatus(StatusID))",
                
                "CREATE TABLE IF NOT EXISTS Trip (TripID INT AUTO_INCREMENT PRIMARY KEY, BookingID INT NOT NULL UNIQUE, CarID INT NOT NULL, DriverID INT NOT NULL, StartMeter INT NOT NULL, EndMeter INT, StartTime DATETIME NOT NULL, EndTime DATETIME, FOREIGN KEY (BookingID) REFERENCES Booking(BookingID), FOREIGN KEY (CarID) REFERENCES Car(CarID), FOREIGN KEY (DriverID) REFERENCES Driver(DriverID))",
                "CREATE TABLE IF NOT EXISTS DriverRating (RatingID INT AUTO_INCREMENT PRIMARY KEY, DriverID INT NOT NULL, TripID INT NOT NULL, Rating INT, Comments TEXT, FOREIGN KEY (DriverID) REFERENCES Driver(DriverID), FOREIGN KEY (TripID) REFERENCES Trip(TripID))",
                
                "CREATE TABLE IF NOT EXISTS RateCard (RateID INT AUTO_INCREMENT PRIMARY KEY, CompanyID INT NOT NULL, CarTypeID INT NOT NULL, ServiceTypeID INT NOT NULL, BaseFare DECIMAL(10,2) NOT NULL, PerKmRate DECIMAL(10,2) NOT NULL, PerHrRate DECIMAL(10,2) NOT NULL, FOREIGN KEY (CompanyID) REFERENCES Company(CompanyID), FOREIGN KEY (CarTypeID) REFERENCES CarType(CarTypeID), FOREIGN KEY (ServiceTypeID) REFERENCES ServiceType(ServiceTypeID))",
                "CREATE TABLE IF NOT EXISTS Tax (TaxID INT AUTO_INCREMENT PRIMARY KEY, TaxName VARCHAR(50) NOT NULL, Percentage DECIMAL(5,2) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS Invoice (InvoiceID INT AUTO_INCREMENT PRIMARY KEY, TripID INT NOT NULL UNIQUE, CompanyID INT NOT NULL, TotalAmount DECIMAL(12,2) NOT NULL, InvoiceDate DATETIME DEFAULT CURRENT_TIMESTAMP, Status VARCHAR(50) DEFAULT 'Unpaid', FOREIGN KEY (TripID) REFERENCES Trip(TripID), FOREIGN KEY (CompanyID) REFERENCES Company(CompanyID))",
                "CREATE TABLE IF NOT EXISTS InvoiceDetails (DetailID INT AUTO_INCREMENT PRIMARY KEY, InvoiceID INT NOT NULL, Description VARCHAR(255) NOT NULL, Amount DECIMAL(10,2) NOT NULL, FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS Payment (PaymentID INT AUTO_INCREMENT PRIMARY KEY, InvoiceID INT NOT NULL, AmountPaid DECIMAL(12,2) NOT NULL, PaymentMode VARCHAR(50) NOT NULL, PaymentDate DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID) ON DELETE CASCADE)",
                
                "CREATE TABLE IF NOT EXISTS FuelLog (LogID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, DriverID INT NOT NULL, Liters DECIMAL(8,2) NOT NULL, TotalCost DECIMAL(10,2) NOT NULL, Date DATETIME, FOREIGN KEY (CarID) REFERENCES Car(CarID), FOREIGN KEY (DriverID) REFERENCES Driver(DriverID))",
                "CREATE TABLE IF NOT EXISTS Maintenance (MaintenanceID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, ServiceDate DATE NOT NULL, TotalCost DECIMAL(10,2) NOT NULL, Description TEXT, FOREIGN KEY (CarID) REFERENCES Car(CarID))",
                "CREATE TABLE IF NOT EXISTS BreakdownLog (BreakdownID INT AUTO_INCREMENT PRIMARY KEY, CarID INT NOT NULL, TripID INT, Description TEXT NOT NULL, BreakdownDate DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (CarID) REFERENCES Car(CarID), FOREIGN KEY (TripID) REFERENCES Trip(TripID))"
            };

            for (String sql : tables) { stmt.execute(sql); }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            autoSeedDatabase();
        } catch (SQLException e) { showAlert("DB Init Error", e.getMessage()); }
    }
    
    private void autoSeedDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Company");
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("Empty database detected. Auto-seeding initial relational data...");
                
                stmt.execute("INSERT INTO BookingStatus (StatusName) VALUES ('Pending'), ('Confirmed'), ('Dispatched'), ('Completed'), ('Cancelled')");
                stmt.execute("INSERT INTO CarType (TypeName, Capacity) VALUES ('Sedan', 4), ('SUV', 6), ('Luxury', 4)");
                stmt.execute("INSERT INTO FuelType (FuelName) VALUES ('Petrol'), ('Diesel'), ('EV')");
                stmt.execute("INSERT INTO ServiceType (ServiceName) VALUES ('8hrs - 80km'), ('12hrs - 120km'), ('Airport Transfer'), ('Local Fixed')");
                stmt.execute("INSERT INTO Location (LocationName, Address) VALUES ('Airport T1', 'Domestic Terminal'), ('HQ Tech Park', 'Sector 5'), ('City Center', 'Downtown')");
                stmt.execute("INSERT INTO Tax (TaxName, Percentage) VALUES ('GST', 5.0), ('VAT', 12.0)");
                stmt.execute("INSERT INTO DriverShift (ShiftName, StartTime, EndTime) VALUES ('Morning', '08:00:00', '16:00:00'), ('Night', '20:00:00', '04:00:00')");
                
                stmt.execute("INSERT INTO Company (CompanyName, ContactPerson, ContactEmail, ContactPhone, BillingAddress) VALUES ('TechCorp', 'John', 'john@tech.com', '1234567890', 'NY'), ('Acme Corp', 'Jane', 'jane@acme.com', '0987654321', 'NJ')");
                stmt.execute("INSERT INTO Department (CompanyID, DepartmentName) VALUES (1, 'IT'), (2, 'Operations')");
                stmt.execute("INSERT INTO Employee (CompanyID, DepartmentID, FirstName, LastName, Email) VALUES (1, 1, 'Alice', 'Smith', 'alice@tech.com'), (2, 2, 'Bob', 'Jones', 'bob@acme.com')");
                stmt.execute("INSERT INTO Car (CarTypeID, FuelTypeID, RegistrationNumber, Make, Model, CurrentStatus) VALUES (2, 2, 'KA-01-AB-1234', 'Toyota', 'Innova', 'Available'), (1, 1, 'MH-12-XY-9876', 'Honda', 'City', 'Available')");
                stmt.execute("INSERT INTO Driver (FirstName, LastName, Phone, LicenseNumber, Status) VALUES ('Ramesh', 'Kumar', '9876543210', 'DL-123', 'Active'), ('Suresh', 'Singh', '9123456780', 'DL-456', 'Active')");
                stmt.execute("INSERT INTO RateCard (CompanyID, CarTypeID, ServiceTypeID, BaseFare, PerKmRate, PerHrRate) VALUES (1, 2, 1, 1800, 15, 100), (2, 1, 3, 1200, 12, 80)");
            }
        } catch (Exception e) { System.out.println("Auto-seed skipped: " + e.getMessage()); }
    }

    // =========================================================================
    // RELATIONAL DATA LOADERS (JOIN QUERIES)
    // =========================================================================

    private void loadAllDataFromDB() {
        companyData.clear(); carData.clear(); driverData.clear();
        bookingData.clear(); rateCardData.clear(); invoiceData.clear();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM Company");
            while(rs.next()) companyData.add(new Company(rs.getString("CompanyID"), rs.getString("CompanyName"), rs.getString("ContactPerson"), rs.getString("ContactEmail"), rs.getString("ContactPhone"), rs.getString("BillingAddress")));

            rs = stmt.executeQuery("SELECT c.CarID, c.RegistrationNumber, c.Make, c.Model, ct.TypeName, ft.FuelName, c.CurrentStatus FROM Car c JOIN CarType ct ON c.CarTypeID = ct.CarTypeID JOIN FuelType ft ON c.FuelTypeID = ft.FuelTypeID");
            while(rs.next()) carData.add(new Car(rs.getString("CarID"), rs.getString("RegistrationNumber"), rs.getString("Make"), rs.getString("Model"), rs.getString("TypeName"), rs.getString("FuelName"), rs.getString("CurrentStatus")));

            rs = stmt.executeQuery("SELECT * FROM Driver");
            while(rs.next()) driverData.add(new Driver(rs.getString("DriverID"), rs.getString("FirstName") + " " + rs.getString("LastName"), rs.getString("LicenseNumber"), rs.getString("Status")));

            String bkgSql = "SELECT b.BookingID, c.CompanyName, e.FirstName, ct.TypeName, st.ServiceName, l1.LocationName as Pickup, l2.LocationName as DropLoc, b.PickupTime, s.StatusName " +
                            "FROM Booking b JOIN Company c ON b.CompanyID = c.CompanyID JOIN Employee e ON b.EmployeeID = e.EmployeeID " +
                            "JOIN CarType ct ON b.CarTypeID = ct.CarTypeID JOIN ServiceType st ON b.ServiceTypeID = st.ServiceTypeID JOIN Location l1 ON b.PickupLocationID = l1.LocationID " +
                            "JOIN Location l2 ON b.DropLocationID = l2.LocationID JOIN BookingStatus s ON b.StatusID = s.StatusID";
            rs = stmt.executeQuery(bkgSql);
            while(rs.next()) bookingData.add(new Booking(rs.getString("BookingID"), rs.getString("CompanyName"), rs.getString("FirstName"), rs.getString("TypeName"), rs.getString("ServiceName"), rs.getString("Pickup"), rs.getString("DropLoc"), rs.getString("PickupTime"), rs.getString("StatusName")));

            String rcSql = "SELECT r.RateID, c.CompanyName, ct.TypeName, st.ServiceName, r.BaseFare, r.PerKmRate, r.PerHrRate " +
                           "FROM RateCard r JOIN Company c ON r.CompanyID = c.CompanyID JOIN CarType ct ON r.CarTypeID = ct.CarTypeID JOIN ServiceType st ON r.ServiceTypeID = st.ServiceTypeID";
            rs = stmt.executeQuery(rcSql);
            while(rs.next()) rateCardData.add(new RateCard(rs.getString("RateID"), rs.getString("CompanyName"), rs.getString("TypeName"), rs.getString("ServiceName"), rs.getString("BaseFare"), rs.getString("PerKmRate"), rs.getString("PerHrRate")));

            String invSql = "SELECT i.InvoiceID, i.TripID, c.CompanyName, i.TotalAmount, i.Status FROM Invoice i JOIN Company c ON i.CompanyID = c.CompanyID";
            rs = stmt.executeQuery(invSql);
            while(rs.next()) invoiceData.add(new Invoice(rs.getString("InvoiceID"), rs.getString("TripID"), rs.getString("CompanyName"), rs.getString("TotalAmount"), rs.getString("Status")));

        } catch (SQLException e) { System.err.println("DB Sync Error: " + e.getMessage()); }
    }
    
    // =========================================================================
    // SMART RELATIONAL LOOKUP HELPERS
    // =========================================================================
    
    static class FKConfig {
        String refTable, refIdCol, refDisplayCol;
        public FKConfig(String t, String i, String d) { refTable=t; refIdCol=i; refDisplayCol=d; }
    }

    private FKConfig getFKConfig(String columnName) {
        switch(columnName) {
            case "CompanyID": return new FKConfig("Company", "CompanyID", "CompanyName");
            case "DepartmentID": return new FKConfig("Department", "DepartmentID", "DepartmentName");
            case "EmployeeID": return new FKConfig("Employee", "EmployeeID", "FirstName");
            case "CarTypeID": return new FKConfig("CarType", "CarTypeID", "TypeName");
            case "FuelTypeID": return new FKConfig("FuelType", "FuelTypeID", "FuelName");
            case "ServiceTypeID": return new FKConfig("ServiceType", "ServiceTypeID", "ServiceName");
            case "CarID": return new FKConfig("Car", "CarID", "RegistrationNumber");
            case "DriverID": return new FKConfig("Driver", "DriverID", "FirstName");
            case "ShiftID": return new FKConfig("DriverShift", "ShiftID", "ShiftName");
            case "LocationID": case "StartLocationID": case "EndLocationID": case "PickupLocationID": case "DropLocationID": case "FromLocationID": case "ToLocationID": return new FKConfig("Location", "LocationID", "LocationName");
            case "StatusID": return new FKConfig("BookingStatus", "StatusID", "StatusName");
            case "BookingID": return new FKConfig("Booking", "BookingID", "BookingID");
            case "TripID": return new FKConfig("Trip", "TripID", "TripID");
            case "TaxID": return new FKConfig("Tax", "TaxID", "TaxName");
            case "InvoiceID": return new FKConfig("Invoice", "InvoiceID", "InvoiceID");
            default: return null;
        }
    }

    private int fetchId(String table, String idCol, String nameCol, String nameVal) {
        if (nameVal == null || nameVal.trim().isEmpty() || nameVal.equals("-1")) return -1;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT " + idCol + " FROM " + table + " WHERE " + nameCol + " = ? LIMIT 1")) {
            ps.setString(1, nameVal);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
    
    private String fetchName(String table, String displayCol, String idCol, String idVal) {
        if (idVal == null || idVal.isEmpty() || idVal.equals("-1")) return null;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT " + displayCol + " FROM " + table + " WHERE " + idCol + " = ? LIMIT 1")) {
            ps.setString(1, idVal);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return idVal;
    }
    
    private ObservableList<String> fetchList(String query) {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { System.err.println("Dropdown Query Failed: " + query); }
        return list;
    }

    private void safeResetAutoIncrement(String tableName) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1");
        } catch (SQLException ex) { System.out.println("Auto-Increment Reset error: " + ex.getMessage()); }
        loadAllDataFromDB(); updateDashboardStats();
    }
    
    private void hardResetDatabase() {
        String[] tables = {"InvoiceDetails", "Payment", "Invoice", "DriverRating", "Trip", "BookingHistory", "Booking", "BookingStatus", "DistanceMatrix", "Route", "Location", "DriverAttendance", "DriverDocuments", "DriverShift", "Driver", "CarDocuments", "CarInsurance", "CarFeatures", "FuelLog", "Maintenance", "BreakdownLog", "Car", "FuelType", "RateCard", "ServiceType", "CarType", "UserAccount", "Employee", "Department", "Company", "Tax"};
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String t : tables) { stmt.execute("DROP TABLE IF EXISTS " + t); }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            setupDatabaseTables(); 
            loadAllDataFromDB(); 
            updateDashboardStats();
            showAlert("Reset Successful", "All 31 tables completely dropped, rebuilt, and re-seeded with strict relational logic.");
        } catch (SQLException ex) { showAlert("Reset Error", "Could not drop tables: " + ex.getMessage()); }
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

        Label logo = new Label("ERP PRO");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 22));
        logo.setPadding(new Insets(0, 0, 30, 10));
        sidebar.getChildren().add(logo);

        String[] menuItems = {"Dashboard", "Companies", "Vehicles", "Drivers", "Bookings & Trips", "Rate Cards", "Trip Billing", "System Tables"};
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
        header.getChildren().addAll(headerTitle, spacer, new Label("System Admin"));
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
            case "Bookings & Trips": contentArea.getChildren().add(createBookingView()); break;
            case "Rate Cards": contentArea.getChildren().add(createRateCardView()); break;
            case "Trip Billing": contentArea.getChildren().add(createBillingView()); break;
            case "System Tables": contentArea.getChildren().add(createSystemTablesView()); break;
        }
    }

    // =========================================================================
    // MODULE VIEWS (SMART DROPDOWN CRUD WITH UPDATES)
    // =========================================================================

    private VBox createDashboardView() {
        VBox layout = new VBox(25); layout.setPadding(new Insets(10));
        HBox welcomeBoxWrapper = new HBox();
        VBox welcomeBox = new VBox(5);
        Label welcomeTitle = new Label("Welcome back, System Administrator 👋"); welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        Label welcomeSub = new Label("Live 31-Table Relational Database Overview."); welcomeSub.setFont(Font.font("System", FontWeight.NORMAL, 16));
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSub);
        
        Region welcomeSpacer = new Region(); HBox.setHgrow(welcomeSpacer, Priority.ALWAYS);
        Button resetDbBtn = new Button("⚠ Reset Database (Testing)");
        resetDbBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        resetDbBtn.setOnAction(e -> {
            if(confirmDelete()) hardResetDatabase(); 
        });
        welcomeBoxWrapper.getChildren().addAll(welcomeBox, welcomeSpacer, resetDbBtn);

        HBox statsBox = new HBox(25);
        Label compLab = createLabel(String.valueOf(companyData.size()), 36, "#2c3e50");
        Label carLab = createLabel(String.valueOf(carData.size()), 36, "#2c3e50");
        pendingLabel = createLabel("0", 36, "#2c3e50"); revLabel = createLabel("₹0.00", 36, "#2c3e50");
        statsBox.getChildren().addAll(createStatCard("🏢 Companies", compLab, "#2980b9", "#ebf5fb"), createStatCard("🚗 Active Fleet", carLab, "#27ae60", "#eafaf1"), createStatCard("📅 Pending Bookings", pendingLabel, "#e74c3c", "#fdedec"), createStatCard("💰 Total Revenue", revLabel, "#8e44ad", "#f5eef8"));
        updateDashboardStats();

        VBox recentBox = new VBox(10);
        TableView<Booking> recentTable = new TableView<>(bookingData);
        recentTable.getColumns().addAll(createCol("BKG ID", "id", 100), createCol("Company", "company", 200), createCol("Service", "serviceType", 150), createCol("Pickup Loc", "pickup", 150), createCol("Status", "status", 150));
        recentTable.setPrefHeight(300); recentBox.getChildren().addAll(new Label("Recent Bookings Overview"), recentTable);
        layout.getChildren().addAll(welcomeBoxWrapper, statsBox, recentBox); return layout;
    }

    private Label createLabel(String txt, int size, String color) { Label l = new Label(txt); l.setFont(Font.font("System", FontWeight.BOLD, size)); l.setTextFill(Color.web(color)); return l; }
    private VBox createStatCard(String title, Label valLabel, String color, String bgColor) {
        VBox card = new VBox(10); card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, " + bgColor + "); -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-border-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
        card.setPadding(new Insets(20)); card.setPrefWidth(260);
        Label titleLabel = new Label(title); titleLabel.setTextFill(Color.web("#7f8c8d")); card.getChildren().addAll(titleLabel, valLabel); return card;
    }

    // --- COMPANY VIEW ---
    private VBox createCompanyView() {
        VBox layout = new VBox(15);
        TableView<Company> table = new TableView<>(companyData);
        table.getColumns().addAll(createCol("ID", "id", 50), createCol("Company Name", "name", 200), createCol("Contact Person", "contact", 150), createCol("Email", "email", 200), createCol("Phone", "phone", 150), createCol("Address", "address", 200));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField nameIn = new TextField(); TextField contactIn = new TextField(); TextField emailIn = new TextField(); TextField phoneIn = new TextField(); TextField addressIn = new TextField(); addressIn.setPrefWidth(300);
        
        Button addBtn = new Button("Add Company"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Company"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Company"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);
        
        form.addRow(0, new Label("Name:"), nameIn, new Label("Contact:"), contactIn);
        form.addRow(1, new Label("Email:"), emailIn, new Label("Phone:"), phoneIn);
        form.addRow(2, new Label("Address:"), addressIn, new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                nameIn.setText(newSel.getName()); contactIn.setText(newSel.getContact()); emailIn.setText(newSel.getEmail()); phoneIn.setText(newSel.getPhone()); addressIn.setText(newSel.getAddress()); 
                deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
            } 
            else { deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Company (CompanyName, ContactPerson, ContactEmail, ContactPhone, BillingAddress) VALUES (?,?,?,?,?)")) {
                pstmt.setString(1, nameIn.getText()); pstmt.setString(2, contactIn.getText()); pstmt.setString(3, emailIn.getText()); pstmt.setString(4, phoneIn.getText()); pstmt.setString(5, addressIn.getText());
                pstmt.executeUpdate(); safeResetAutoIncrement("Company"); nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });
        
        updateBtn.setOnAction(e -> {
            Company sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Company SET CompanyName=?, ContactPerson=?, ContactEmail=?, ContactPhone=?, BillingAddress=? WHERE CompanyID=?")) {
                    pstmt.setString(1, nameIn.getText()); pstmt.setString(2, contactIn.getText()); pstmt.setString(3, emailIn.getText()); pstmt.setString(4, phoneIn.getText()); pstmt.setString(5, addressIn.getText());
                    pstmt.setInt(6, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); 
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                    nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        deleteBtn.setOnAction(e -> {
            Company sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Company WHERE CompanyID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); safeResetAutoIncrement("Company");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        layout.getChildren().addAll(new Label("Corporate Clients Registry"), table, form); return layout;
    }

    // --- VEHICLE VIEW ---
    private VBox createVehicleView() {
        VBox layout = new VBox(15);
        TableView<Car> table = new TableView<>(carData);
        table.getColumns().addAll(createCol("Car ID", "id", 60), createCol("Reg No", "regNo", 120), createCol("Make", "make", 120), createCol("Model", "model", 120), createCol("Type", "type", 100), createCol("Fuel", "fuel", 80), createCol("Status", "status", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField regIn = new TextField(); TextField makeIn = new TextField(); TextField modelIn = new TextField();
        
        ComboBox<String> typeIn = new ComboBox<>(fetchList("SELECT TypeName FROM CarType"));
        ComboBox<String> fuelIn = new ComboBox<>(fetchList("SELECT FuelName FROM FuelType"));
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Available", "On Trip", "Maintenance")); statusIn.setValue("Available");
        
        Button addBtn = new Button("Add Vehicle"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Vehicle"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Vehicle"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("Reg No:"), regIn, new Label("Make:"), makeIn, new Label("Model:"), modelIn);
        form.addRow(1, new Label("Car Type:"), typeIn, new Label("Fuel:"), fuelIn, new Label("Status:"), statusIn);
        form.addRow(2, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                regIn.setText(newSel.getRegNo()); makeIn.setText(newSel.getMake()); modelIn.setText(newSel.getModel());
                typeIn.setValue(newSel.getType()); fuelIn.setValue(newSel.getFuel()); statusIn.setValue(newSel.getStatus()); 
                deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
            } 
            else { deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
            int fuelId = fetchId("FuelType", "FuelTypeID", "FuelName", fuelIn.getValue());
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Car (CarTypeID, FuelTypeID, RegistrationNumber, Make, Model, CurrentStatus) VALUES (?,?,?,?,?,?)")) {
                pstmt.setInt(1, typeId); pstmt.setInt(2, fuelId); pstmt.setString(3, regIn.getText().toUpperCase()); pstmt.setString(4, makeIn.getText()); pstmt.setString(5, modelIn.getText()); pstmt.setString(6, statusIn.getValue());
                pstmt.executeUpdate(); safeResetAutoIncrement("Car"); regIn.clear(); makeIn.clear(); modelIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });
        
        updateBtn.setOnAction(e -> {
            Car sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
                int fuelId = fetchId("FuelType", "FuelTypeID", "FuelName", fuelIn.getValue());
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Car SET CarTypeID=?, FuelTypeID=?, RegistrationNumber=?, Make=?, Model=?, CurrentStatus=? WHERE CarID=?")) {
                    pstmt.setInt(1, typeId); pstmt.setInt(2, fuelId); pstmt.setString(3, regIn.getText().toUpperCase()); pstmt.setString(4, makeIn.getText()); pstmt.setString(5, modelIn.getText()); pstmt.setString(6, statusIn.getValue());
                    pstmt.setInt(7, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); 
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                    regIn.clear(); makeIn.clear(); modelIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        deleteBtn.setOnAction(e -> {
            Car sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Car WHERE CarID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); safeResetAutoIncrement("Car");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        layout.getChildren().addAll(new Label("Fleet Management (Auto-Linked via Database Lookups)"), table, form); return layout;
    }

    // --- DRIVER VIEW ---
    private VBox createDriverView() {
        VBox layout = new VBox(15);
        TableView<Driver> table = new TableView<>(driverData);
        table.getColumns().addAll(createCol("ID", "id", 50), createCol("Name", "firstName", 150), createCol("License", "license", 150), createCol("Status", "status", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField fNameIn = new TextField(); TextField lNameIn = new TextField(); TextField phoneIn = new TextField(); TextField licIn = new TextField();
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Active", "On Leave", "Suspended")); statusIn.setValue("Active");
        
        Button addBtn = new Button("Add Driver"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Driver"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Driver"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("First Name:"), fNameIn, new Label("Last Name:"), lNameIn, new Label("Phone:"), phoneIn);
        form.addRow(1, new Label("License:"), licIn, new Label("Status:"), statusIn, new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                String[] names = newSel.getFirstName().split(" ", 2);
                fNameIn.setText(names.length > 0 ? names[0] : "");
                lNameIn.setText(names.length > 1 ? names[1] : "");
                licIn.setText(newSel.getLicense());
                statusIn.setValue(newSel.getStatus());
                try(Connection c = getConnection(); PreparedStatement p = c.prepareStatement("SELECT Phone FROM Driver WHERE DriverID=?")){
                    p.setInt(1, Integer.parseInt(newSel.getId())); ResultSet r = p.executeQuery(); if(r.next()) phoneIn.setText(r.getString(1));
                } catch(Exception ignored){}

                deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
            } 
            else { deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Driver (FirstName, LastName, Phone, LicenseNumber, Status) VALUES (?,?,?,?,?)")) {
                pstmt.setString(1, fNameIn.getText()); pstmt.setString(2, lNameIn.getText()); pstmt.setString(3, phoneIn.getText()); pstmt.setString(4, licIn.getText()); pstmt.setString(5, statusIn.getValue());
                pstmt.executeUpdate(); safeResetAutoIncrement("Driver"); fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });
        
        updateBtn.setOnAction(e -> {
            Driver sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Driver SET FirstName=?, LastName=?, Phone=?, LicenseNumber=?, Status=? WHERE DriverID=?")) {
                    pstmt.setString(1, fNameIn.getText()); pstmt.setString(2, lNameIn.getText()); pstmt.setString(3, phoneIn.getText()); pstmt.setString(4, licIn.getText()); pstmt.setString(5, statusIn.getValue());
                    pstmt.setInt(6, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); 
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                    fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        deleteBtn.setOnAction(e -> {
            Driver sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Driver WHERE DriverID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); safeResetAutoIncrement("Driver");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        layout.getChildren().addAll(new Label("Driver Management"), table, form); return layout;
    }

    // --- BOOKINGS & TRIPS (TRUE LIFECYCLE) ---
    private VBox createBookingView() {
        VBox layout = new VBox(15);
        TableView<Booking> table = new TableView<>(bookingData);
        table.getColumns().addAll(
            createCol("BKG ID", "id", 60), createCol("Company", "company", 130), 
            createCol("Employee", "employee", 100), createCol("Service", "serviceType", 120),
            createCol("Pickup Loc", "pickup", 120), createCol("Drop Loc", "dropLoc", 120), createCol("Status", "status", 90)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> compIn = new ComboBox<>(fetchList("SELECT CompanyName FROM Company"));
        ComboBox<String> empIn = new ComboBox<>(); empIn.setPromptText("Select Company First");
        compIn.valueProperty().addListener((obs, old, newVal) -> {
            if(newVal != null) {
                int cid = fetchId("Company", "CompanyID", "CompanyName", newVal);
                empIn.setItems(fetchList("SELECT CONCAT(FirstName, ' ', LastName, ' [', Email, ']') FROM Employee WHERE CompanyID=" + cid));
            }
        });

        ComboBox<String> typeIn = new ComboBox<>(fetchList("SELECT TypeName FROM CarType"));
        ComboBox<String> serviceIn = new ComboBox<>(fetchList("SELECT ServiceName FROM ServiceType"));
        ComboBox<String> pickupIn = new ComboBox<>(fetchList("SELECT LocationName FROM Location"));
        ComboBox<String> dropIn = new ComboBox<>(fetchList("SELECT LocationName FROM Location"));
        TextField timeIn = new TextField(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ComboBox<String> statusIn = new ComboBox<>(fetchList("SELECT StatusName FROM BookingStatus")); statusIn.setValue("Pending");
        
        Button addBtn = new Button("Create Booking"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Booking"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Booking"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);
        
        Label dispatchLbl = new Label("Dispatch (Assigns Car & Driver -> Creates Trip log):"); dispatchLbl.setTextFill(Color.DARKRED);
        ComboBox<String> carAssignIn = new ComboBox<>(fetchList("SELECT CONCAT(RegistrationNumber, ' - ', Make, ' ', Model) FROM Car WHERE CurrentStatus='Available'"));
        ComboBox<String> driverAssignIn = new ComboBox<>(fetchList("SELECT CONCAT(FirstName, ' ', LastName, ' [', LicenseNumber, ']') FROM Driver WHERE Status='Active'"));
        Button dispatchBtn = new Button("Dispatch Trip"); dispatchBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;"); dispatchBtn.setDisable(true);

        form.addRow(0, new Label("1. Client:"), compIn, new Label("Employee (Auto-Sync):"), empIn, new Label("Service Type:"), serviceIn);
        form.addRow(1, new Label("Car Type Req:"), typeIn, new Label("Pickup Loc:"), pickupIn, new Label("Drop Loc:"), dropIn);
        form.addRow(2, new Label("Time:"), timeIn, new Label("Status:"), statusIn, new HBox(10, addBtn, updateBtn, deleteBtn));
        form.addRow(3, new Separator(), new Separator(), new Separator(), new Separator(), new Separator(), new Separator());
        form.addRow(4, dispatchLbl);
        form.addRow(5, new Label("Assign Car:"), carAssignIn, new Label("Assign Driver:"), driverAssignIn, dispatchBtn);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                compIn.setValue(newSel.getCompany()); 
                empIn.getItems().stream().filter(item -> item.contains(newSel.getEmployee())).findFirst().ifPresent(empIn::setValue);
                typeIn.setValue(newSel.getCarType());
                serviceIn.setValue(newSel.getServiceType());
                pickupIn.setValue(newSel.getPickup());
                dropIn.setValue(newSel.getDropLoc());
                statusIn.setValue(newSel.getStatus());
                
                if("Pending".equals(newSel.getStatus()) || "Confirmed".equals(newSel.getStatus())) { dispatchBtn.setDisable(false); } else { dispatchBtn.setDisable(true); }
                deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
            } else { deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); dispatchBtn.setDisable(true); }
        });

        addBtn.setOnAction(e -> {
            int compId = fetchId("Company", "CompanyID", "CompanyName", compIn.getValue());
            String empStr = empIn.getValue();
            String empEmail = empStr != null && empStr.contains("[") ? empStr.substring(empStr.indexOf("[") + 1, empStr.indexOf("]")) : "";
            int empId = fetchId("Employee", "EmployeeID", "Email", empEmail);
            int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
            int servId = fetchId("ServiceType", "ServiceTypeID", "ServiceName", serviceIn.getValue());
            int pickId = fetchId("Location", "LocationID", "LocationName", pickupIn.getValue());
            int dropId = fetchId("Location", "LocationID", "LocationName", dropIn.getValue());
            int statId = fetchId("BookingStatus", "StatusID", "StatusName", statusIn.getValue());
            
            if (compId == -1 || empId == -1 || typeId == -1 || servId == -1 || pickId == -1 || dropId == -1 || statId == -1) {
                showAlert("Validation Error", "Please completely select all dropdowns to create a strict relational booking.");
                return;
            }
            
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Booking (CompanyID, EmployeeID, CarTypeID, ServiceTypeID, PickupLocationID, DropLocationID, PickupTime, StatusID) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, compId); pstmt.setInt(2, empId); pstmt.setInt(3, typeId); pstmt.setInt(4, servId); pstmt.setInt(5, pickId); pstmt.setInt(6, dropId); pstmt.setString(7, timeIn.getText()); pstmt.setInt(8, statId);
                pstmt.executeUpdate(); 
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next()){
                    try(PreparedStatement psHist = conn.prepareStatement("INSERT INTO BookingHistory (BookingID, StatusID) VALUES (?,?)")){
                        psHist.setInt(1, rs.getInt(1)); psHist.setInt(2, statId); psHist.executeUpdate();
                    }
                }
                safeResetAutoIncrement("Booking");
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                int compId = fetchId("Company", "CompanyID", "CompanyName", compIn.getValue());
                String empStr = empIn.getValue();
                String empEmail = empStr != null && empStr.contains("[") ? empStr.substring(empStr.indexOf("[") + 1, empStr.indexOf("]")) : "";
                int empId = fetchId("Employee", "EmployeeID", "Email", empEmail);
                int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
                int servId = fetchId("ServiceType", "ServiceTypeID", "ServiceName", serviceIn.getValue());
                int pickId = fetchId("Location", "LocationID", "LocationName", pickupIn.getValue());
                int dropId = fetchId("Location", "LocationID", "LocationName", dropIn.getValue());
                int statId = fetchId("BookingStatus", "StatusID", "StatusName", statusIn.getValue());

                if (compId == -1 || empId == -1 || typeId == -1 || servId == -1 || pickId == -1 || dropId == -1 || statId == -1) {
                    showAlert("Validation Error", "Please completely select all dropdowns to update."); return;
                }

                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Booking SET CompanyID=?, EmployeeID=?, CarTypeID=?, ServiceTypeID=?, PickupLocationID=?, DropLocationID=?, PickupTime=?, StatusID=? WHERE BookingID=?")) {
                    pstmt.setInt(1, compId); pstmt.setInt(2, empId); pstmt.setInt(3, typeId); pstmt.setInt(4, servId); pstmt.setInt(5, pickId); pstmt.setInt(6, dropId); pstmt.setString(7, timeIn.getText()); pstmt.setInt(8, statId);
                    pstmt.setInt(9, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); 
                    
                    if(!sel.getStatus().equals(statusIn.getValue())) {
                        try(PreparedStatement psHist = conn.prepareStatement("INSERT INTO BookingHistory (BookingID, StatusID) VALUES (?,?)")){
                            psHist.setInt(1, Integer.parseInt(sel.getId())); psHist.setInt(2, statId); psHist.executeUpdate();
                        }
                    }
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        dispatchBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && carAssignIn.getValue() != null && driverAssignIn.getValue() != null) {
                int bkgId = Integer.parseInt(sel.getId());
                
                String carStr = carAssignIn.getValue();
                String regNo = carStr.contains(" - ") ? carStr.split(" - ")[0] : carStr;
                int carId = fetchId("Car", "CarID", "RegistrationNumber", regNo);
                
                String drvStr = driverAssignIn.getValue();
                String drvLic = drvStr.contains("[") ? drvStr.substring(drvStr.indexOf("[") + 1, drvStr.indexOf("]")) : "";
                int driverId = fetchId("Driver", "DriverID", "LicenseNumber", drvLic);
                int statId = fetchId("BookingStatus", "StatusID", "StatusName", "Dispatched");
                
                try (Connection conn = getConnection()) {
                    PreparedStatement p1 = conn.prepareStatement("UPDATE Booking SET StatusID=? WHERE BookingID=?");
                    p1.setInt(1, statId); p1.setInt(2, bkgId); p1.executeUpdate();
                    
                    PreparedStatement p2 = conn.prepareStatement("INSERT INTO BookingHistory (BookingID, StatusID) VALUES (?,?)");
                    p2.setInt(1, bkgId); p2.setInt(2, statId); p2.executeUpdate();
                    
                    PreparedStatement p3 = conn.prepareStatement("INSERT INTO Trip (BookingID, CarID, DriverID, StartMeter, StartTime) VALUES (?,?,?,?,?)");
                    p3.setInt(1, bkgId); p3.setInt(2, carId); p3.setInt(3, driverId); p3.setInt(4, 0); p3.setString(5, LocalDateTime.now().toString()); p3.executeUpdate();
                    
                    PreparedStatement p4 = conn.prepareStatement("UPDATE Car SET CurrentStatus='On Trip' WHERE CarID=?");
                    p4.setInt(1, carId); p4.executeUpdate();
                    
                    safeResetAutoIncrement("Trip");
                    showAlert("Dispatch Success", "Trip created successfully! Vehicle and Driver allocated.");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Booking WHERE BookingID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); safeResetAutoIncrement("Booking"); table.getSelectionModel().clearSelection();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Booking Lifecycle (Select a booking to Dispatch a Trip)"), table, form); return layout;
    }

    // --- RATE CARD VIEW ---
    private VBox createRateCardView() {
        VBox layout = new VBox(15);
        TableView<RateCard> table = new TableView<>(rateCardData);
        table.getColumns().addAll(
            createCol("ID", "id", 60), createCol("Company", "company", 160), 
            createCol("Car Type", "carType", 120), createCol("Service", "serviceType", 140), 
            createCol("Base Fare", "baseFare", 90), createCol("Per KM", "perKmRate", 90), createCol("Per Hr", "perHrRate", 90)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> compIn = new ComboBox<>(fetchList("SELECT CompanyName FROM Company"));
        ComboBox<String> typeIn = new ComboBox<>(fetchList("SELECT TypeName FROM CarType"));
        ComboBox<String> serviceIn = new ComboBox<>(fetchList("SELECT ServiceName FROM ServiceType"));
        TextField baseFareIn = new TextField(); TextField perKmIn = new TextField(); TextField perHrIn = new TextField();
        
        Button addBtn = new Button("Add Rate Card"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Rate Card"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Rate Card"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("Company:"), compIn, new Label("Car Type:"), typeIn, new Label("Service Type:"), serviceIn);
        form.addRow(1, new Label("Base Fare:"), baseFareIn, new Label("Per KM Rate:"), perKmIn, new Label("Per Hr Rate:"), perHrIn);
        form.addRow(2, new Label(""), new HBox(10, addBtn, updateBtn, deleteBtn));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) { 
                compIn.setValue(newSel.getCompany()); typeIn.setValue(newSel.getCarType()); serviceIn.setValue(newSel.getServiceType());
                baseFareIn.setText(newSel.getBaseFare()); perKmIn.setText(newSel.getPerKmRate()); perHrIn.setText(newSel.getPerHrRate()); 
                deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
            } else { deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            int compId = fetchId("Company", "CompanyID", "CompanyName", compIn.getValue());
            int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
            int servId = fetchId("ServiceType", "ServiceTypeID", "ServiceName", serviceIn.getValue());
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO RateCard (CompanyID, CarTypeID, ServiceTypeID, BaseFare, PerKmRate, PerHrRate) VALUES (?,?,?,?,?,?)")) {
                pstmt.setInt(1, compId); pstmt.setInt(2, typeId); pstmt.setInt(3, servId); pstmt.setString(4, baseFareIn.getText()); pstmt.setString(5, perKmIn.getText()); pstmt.setString(6, perHrIn.getText());
                pstmt.executeUpdate(); safeResetAutoIncrement("RateCard");
                baseFareIn.clear(); perKmIn.clear(); perHrIn.clear();
            } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            RateCard sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                int compId = fetchId("Company", "CompanyID", "CompanyName", compIn.getValue());
                int typeId = fetchId("CarType", "CarTypeID", "TypeName", typeIn.getValue());
                int servId = fetchId("ServiceType", "ServiceTypeID", "ServiceName", serviceIn.getValue());
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE RateCard SET CompanyID=?, CarTypeID=?, ServiceTypeID=?, BaseFare=?, PerKmRate=?, PerHrRate=? WHERE RateID=?")) {
                    pstmt.setInt(1, compId); pstmt.setInt(2, typeId); pstmt.setInt(3, servId); pstmt.setString(4, baseFareIn.getText()); pstmt.setString(5, perKmIn.getText()); pstmt.setString(6, perHrIn.getText());
                    pstmt.setInt(7, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); 
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                    baseFareIn.clear(); perKmIn.clear(); perHrIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            RateCard sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM RateCard WHERE RateID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId())); pstmt.executeUpdate(); safeResetAutoIncrement("RateCard"); table.getSelectionModel().clearSelection();
                    baseFareIn.clear(); perKmIn.clear(); perHrIn.clear();
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Dynamic Rate Card Configuration (Relational)"), table, form); return layout;
    }

    // --- BILLING / INVOICE VIEW ---
    private VBox createBillingView() {
        VBox layout = new VBox(15);
        TableView<Invoice> table = new TableView<>(invoiceData);
        table.getColumns().addAll(
            createCol("Inv ID", "id", 50), createCol("Trip Ref", "bookingRef", 60), createCol("Company", "company", 130), 
            createCol("Total", "total", 80), createCol("Status", "status", 70)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> tripIn = new ComboBox<>(fetchList("SELECT CONCAT('Trip #', TripID, ' (BKG-', BookingID, ')') FROM Trip WHERE TripID NOT IN (SELECT TripID FROM Invoice)"));
        ComboBox<String> taxIn = new ComboBox<>(fetchList("SELECT CONCAT(TaxName, ' (', Percentage, '%)') FROM Tax"));
        taxIn.getSelectionModel().selectFirst(); 
        
        TextField distanceIn = new TextField(); TextField baseFareIn = new TextField(); 
        TextField extraKmRateIn = new TextField(); extraKmRateIn.setEditable(false); 
        TextField extraHrRateIn = new TextField(); extraHrRateIn.setEditable(false);
        TextField hoursIn = new TextField(); hoursIn.setPromptText("Billed Hrs");
        TextField tollsIn = new TextField("0.00");
        
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Unpaid", "Paid", "Void")); statusIn.setValue("Paid");
        ComboBox<String> payModeIn = new ComboBox<>(FXCollections.observableArrayList("Credit Card", "Bank Transfer", "Wallet", "Cash")); payModeIn.setValue("Wallet");
        
        Button calcBtn = new Button("Bill Trip & Generate Invoice"); calcBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        Button updateBtn = new Button("Update Status"); updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Invoice"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);

        form.addRow(0, new Label("Select Trip ID:"), tripIn, new Label("Distance (KM):"), distanceIn);
        form.addRow(1, new Label("Base Fare (₹):"), baseFareIn, new Label("Hours:"), hoursIn);
        form.addRow(2, new Label("Per KM Rate:"), extraKmRateIn, new Label("Per Hr Rate:"), extraHrRateIn);
        form.addRow(3, new Label("Tolls (₹):"), tollsIn, new Label("Tax Bracket:"), taxIn);
        form.addRow(4, new Label("Status:"), statusIn, new Label("Payment Mode:"), payModeIn);
        form.addRow(5, new Label(""), new HBox(10, calcBtn, updateBtn, deleteBtn));

        tripIn.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.contains("#")) {
                int tripId = Integer.parseInt(newVal.replace("Trip #", "").split(" ")[0]);
                try (Connection conn = getConnection()) {
                    String q = "SELECT c.CompanyName, ct.TypeName, st.ServiceName FROM Trip t JOIN Booking b ON t.BookingID = b.BookingID JOIN Company c ON b.CompanyID = c.CompanyID JOIN CarType ct ON b.CarTypeID = ct.CarTypeID JOIN ServiceType st ON b.ServiceTypeID = st.ServiceTypeID WHERE t.TripID = ?";
                    PreparedStatement ps = conn.prepareStatement(q);
                    ps.setInt(1, tripId);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        String tripCompany = rs.getString("CompanyName");
                        String tripCarType = rs.getString("TypeName");
                        String tripService = rs.getString("ServiceName");
                        boolean found = false;
                        for(RateCard rc : rateCardData) {
                            if(tripCompany.equalsIgnoreCase(rc.getCompany()) && tripCarType.equalsIgnoreCase(rc.getCarType()) && tripService.equalsIgnoreCase(rc.getServiceType())) {
                                baseFareIn.setText(rc.getBaseFare()); extraKmRateIn.setText(rc.getPerKmRate()); extraHrRateIn.setText(rc.getPerHrRate()); found = true; break;
                            }
                        }
                        if(!found) { baseFareIn.setText("0"); extraKmRateIn.setText("0"); extraHrRateIn.setText("0"); }
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if(newSel != null) {
                if(!tripIn.getItems().contains("Trip #" + newSel.getBookingRef() + " (Historical)")) {
                    tripIn.getItems().add("Trip #" + newSel.getBookingRef() + " (Historical)");
                }
                tripIn.setValue("Trip #" + newSel.getBookingRef() + " (Historical)");
                statusIn.setValue(newSel.getStatus());
                calcBtn.setDisable(true); updateBtn.setDisable(false); deleteBtn.setDisable(false);
            } else { calcBtn.setDisable(false); updateBtn.setDisable(true); deleteBtn.setDisable(true); }
        });

        calcBtn.setOnAction(e -> {
            if (tripIn.getValue() != null && tripIn.getValue().contains("#")) {
                int tripId = Integer.parseInt(tripIn.getValue().replace("Trip #", "").split(" ")[0]);
                
                String tStr = taxIn.getValue();
                double taxPercentage = tStr != null && tStr.contains("(") ? Double.parseDouble(tStr.substring(tStr.indexOf("(") + 1, tStr.indexOf("%"))) : 0;
                
                double dist = parseDoubleSafe(distanceIn.getText()), hrs = parseDoubleSafe(hoursIn.getText()), base = parseDoubleSafe(baseFareIn.getText());
                double eKm = parseDoubleSafe(extraKmRateIn.getText()), eHr = parseDoubleSafe(extraHrRateIn.getText()), toll = parseDoubleSafe(tollsIn.getText());
                double dChg = dist * eKm; double hChg = hrs * eHr; double sub = base + dChg + hChg + toll; 
                double taxAmt = sub * (taxPercentage / 100); double total = sub + taxAmt;
                
                try (Connection conn = getConnection()) {
                    int compId = -1;
                    try(PreparedStatement ps = conn.prepareStatement("SELECT b.CompanyID FROM Trip t JOIN Booking b ON t.BookingID = b.BookingID WHERE t.TripID = ?")) {
                        ps.setInt(1, tripId); ResultSet rs = ps.executeQuery(); if(rs.next()) compId = rs.getInt(1);
                    }
                    
                    PreparedStatement p1 = conn.prepareStatement("INSERT INTO Invoice (TripID, CompanyID, TotalAmount, Status) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    p1.setInt(1, tripId); p1.setInt(2, compId); p1.setString(3, String.valueOf(total)); p1.setString(4, statusIn.getValue()); p1.executeUpdate();
                    
                    ResultSet rs = p1.getGeneratedKeys();
                    if(rs.next()) {
                        int invId = rs.getInt(1);
                        PreparedStatement p2 = conn.prepareStatement("INSERT INTO InvoiceDetails (InvoiceID, Description, Amount) VALUES (?,?,?)");
                        p2.setInt(1, invId); p2.setString(2, "Base Fare"); p2.setString(3, String.valueOf(base)); p2.executeUpdate();
                        p2.setInt(1, invId); p2.setString(2, "Distance Charge"); p2.setString(3, String.valueOf(dChg)); p2.executeUpdate();
                        p2.setInt(1, invId); p2.setString(2, "Hours Charge"); p2.setString(3, String.valueOf(hChg)); p2.executeUpdate();
                        p2.setInt(1, invId); p2.setString(2, "Tolls"); p2.setString(3, String.valueOf(toll)); p2.executeUpdate();
                        p2.setInt(1, invId); p2.setString(2, "Tax Amount"); p2.setString(3, String.valueOf(taxAmt)); p2.executeUpdate();
                        
                        if("Paid".equalsIgnoreCase(statusIn.getValue())) {
                            PreparedStatement p3 = conn.prepareStatement("INSERT INTO Payment (InvoiceID, AmountPaid, PaymentMode) VALUES (?,?,?)");
                            p3.setInt(1, invId); p3.setString(2, String.valueOf(total)); p3.setString(3, payModeIn.getValue()); p3.executeUpdate();
                        }
                    }
                    
                    safeResetAutoIncrement("Invoice");
                    showAlert("Success", "Cascaded Billing Complete! Invoice Details and Payment tables updated. Total: ₹" + String.format("%.2f", total));
                    tripIn.setItems(fetchList("SELECT CONCAT('Trip #', TripID, ' (BKG-', BookingID, ')') FROM Trip WHERE TripID NOT IN (SELECT TripID FROM Invoice)"));
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        updateBtn.setOnAction(e -> {
            Invoice sel = table.getSelectionModel().getSelectedItem();
            if(sel != null) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("UPDATE Invoice SET Status=? WHERE InvoiceID=?")) {
                    pstmt.setString(1, statusIn.getValue());
                    pstmt.setInt(2, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate();
                    loadAllDataFromDB(); table.getSelectionModel().clearSelection();
                    showAlert("Success", "Invoice Status Updated successfully!");
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        deleteBtn.setOnAction(e -> {
            Invoice sel = table.getSelectionModel().getSelectedItem();
            if(sel != null && confirmDelete()) {
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Invoice WHERE InvoiceID=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.getId()));
                    pstmt.executeUpdate();
                    safeResetAutoIncrement("Invoice"); table.getSelectionModel().clearSelection();
                    tripIn.setItems(fetchList("SELECT CONCAT('Trip #', TripID, ' (BKG-', BookingID, ')') FROM Trip WHERE TripID NOT IN (SELECT TripID FROM Invoice)"));
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        layout.getChildren().addAll(new Label("Trip Billing Operations (Cascades to InvoiceDetails & Payment)"), table, form); return layout;
    }

    // --- SMART DYNAMIC SYSTEM TABLES ENGINE (COVERS ALL 31 TABLES + AUTO FOREIGN KEYS) ---
    private VBox createSystemTablesView() {
        VBox layout = new VBox(15); layout.setPadding(new Insets(10));
        HBox topBox = new HBox(10); topBox.setAlignment(Pos.CENTER_LEFT);
        Label selLabel = new Label("Select Database Table:"); selLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ComboBox<String> tableSelector = new ComboBox<>(FXCollections.observableArrayList(
            "Company", "Department", "Employee", "UserAccount", "CarType", "FuelType", "Car", 
            "CarFeatures", "CarInsurance", "CarDocuments", "Driver", "DriverDocuments", 
            "DriverAttendance", "DriverShift", "DriverRating", "Location", "Route", 
            "DistanceMatrix", "BookingStatus", "ServiceType", "Booking", "BookingHistory", "Trip", 
            "RateCard", "Tax", "Invoice", "InvoiceDetails", "Payment", "FuelLog", 
            "Maintenance", "BreakdownLog"
        ));
        tableSelector.setValue("Employee"); 
        topBox.getChildren().addAll(selLabel, tableSelector);

        TableView<ObservableList<String>> dynamicTable = new TableView<>(); VBox.setVgrow(dynamicTable, Priority.ALWAYS);
        FlowPane dynamicForm = new FlowPane(15, 15); dynamicForm.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        Button addBtn = new Button("Add Record"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        Button deleteBtn = new Button("Delete Record"); deleteBtn.getStyleClass().add("delete-button"); deleteBtn.setDisable(true);
        
        tableSelector.valueProperty().addListener((obs, oldVal, newVal) -> loadDynamicTable(newVal, dynamicTable, dynamicForm, deleteBtn, updateBtn, addBtn));

        addBtn.setOnAction(e -> {
            String tName = tableSelector.getValue();
            if (currentDynamicColumns.isEmpty()) return;
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tName).append(" (");
            StringBuilder placeholders = new StringBuilder("VALUES (");
            
            List<String> insertCols = new ArrayList<>();
            List<String> insertVals = new ArrayList<>();
            
            for (int i=0; i<currentDynamicColumns.size(); i++) {
                Control c = currentDynamicFields.get(i);
                if (c == null) continue; // Skip Primary Key
                
                insertCols.add(currentDynamicColumns.get(i));
                FKConfig fk = currentDynamicFKs.get(i);
                String val = "";
                
                if (c instanceof ComboBox) {
                    String displayValue = (String) ((ComboBox<?>)c).getValue();
                    if (displayValue != null && fk != null) {
                        int fkId = fetchId(fk.refTable, fk.refIdCol, fk.refDisplayCol, displayValue);
                        val = fkId != -1 ? String.valueOf(fkId) : null;
                    }
                } else {
                    val = ((TextField)c).getText();
                }
                insertVals.add(val);
            }
            
            for (int i=0; i<insertCols.size(); i++) {
                sql.append(insertCols.get(i)).append(i == insertCols.size()-1 ? ") " : ", ");
                placeholders.append("?").append(i == insertCols.size()-1 ? ")" : ", ");
            }
            sql.append(placeholders.toString());
            
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i=0; i<insertVals.size(); i++) {
                    if (insertVals.get(i) == null || insertVals.get(i).trim().isEmpty()) {
                        pstmt.setNull(i+1, Types.INTEGER); // Handling optional FK mappings elegantly
                    } else {
                        pstmt.setString(i+1, insertVals.get(i));
                    }
                }
                pstmt.executeUpdate(); 
                safeResetAutoIncrement(tName);
                loadDynamicTable(tName, dynamicTable, dynamicForm, deleteBtn, updateBtn, addBtn);
            } catch(SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        updateBtn.setOnAction(e -> {
            ObservableList<String> sel = dynamicTable.getSelectionModel().getSelectedItem();
            if (sel == null || currentDynamicColumns.isEmpty()) return;
            String tName = tableSelector.getValue();
            String pkCol = currentDynamicColumns.get(0);
            
            StringBuilder sql = new StringBuilder("UPDATE ").append(tName).append(" SET ");
            List<String> updateCols = new ArrayList<>();
            List<String> updateVals = new ArrayList<>();
            
            for (int i=1; i<currentDynamicColumns.size(); i++) {
                Control c = currentDynamicFields.get(i);
                updateCols.add(currentDynamicColumns.get(i));
                FKConfig fk = currentDynamicFKs.get(i);
                String val = "";
                
                if (c instanceof ComboBox) {
                    String displayValue = (String) ((ComboBox<?>)c).getValue();
                    if (displayValue != null && fk != null) {
                        int fkId = fetchId(fk.refTable, fk.refIdCol, fk.refDisplayCol, displayValue);
                        val = fkId != -1 ? String.valueOf(fkId) : null;
                    }
                } else {
                    val = ((TextField)c).getText();
                }
                updateVals.add(val);
            }
            
            for (int i=0; i<updateCols.size(); i++) {
                sql.append(updateCols.get(i)).append("=?");
                if (i < updateCols.size() - 1) sql.append(", ");
            }
            sql.append(" WHERE ").append(pkCol).append("=?");
            
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i=0; i<updateVals.size(); i++) {
                    if (updateVals.get(i) == null || updateVals.get(i).trim().isEmpty()) {
                        pstmt.setNull(i+1, Types.INTEGER);
                    } else {
                        pstmt.setString(i+1, updateVals.get(i));
                    }
                }
                pstmt.setInt(updateVals.size() + 1, Integer.parseInt(sel.get(0)));
                pstmt.executeUpdate(); 
                loadAllDataFromDB();
                loadDynamicTable(tName, dynamicTable, dynamicForm, deleteBtn, updateBtn, addBtn);
            } catch(SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        });

        deleteBtn.setOnAction(e -> {
            ObservableList<String> sel = dynamicTable.getSelectionModel().getSelectedItem();
            if (sel != null && confirmDelete()) {
                String tName = tableSelector.getValue();
                String pkCol = currentDynamicColumns.get(0);
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM " + tName + " WHERE " + pkCol + "=?")) {
                    pstmt.setInt(1, Integer.parseInt(sel.get(0))); pstmt.executeUpdate(); 
                    safeResetAutoIncrement(tName);
                    loadDynamicTable(tName, dynamicTable, dynamicForm, deleteBtn, updateBtn, addBtn);
                } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
            }
        });

        loadDynamicTable("Employee", dynamicTable, dynamicForm, deleteBtn, updateBtn, addBtn);
        layout.getChildren().addAll(new Label("Smart Metadata Explorer (Auto-Binds Foreign Keys)"), topBox, dynamicTable, dynamicForm, new HBox(10, addBtn, updateBtn, deleteBtn)); return layout;
    }

    private void loadDynamicTable(String tableName, TableView<ObservableList<String>> table, FlowPane form, Button deleteBtn, Button updateBtn, Button addBtn) {
        table.getColumns().clear(); table.getItems().clear(); form.getChildren().clear(); 
        currentDynamicColumns.clear(); currentDynamicFields.clear(); currentDynamicFKs.clear();
        deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false);
        
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            
            for (int i = 1; i <= colCount; i++) {
                final int j = i - 1; 
                String colName = meta.getColumnName(i); 
                currentDynamicColumns.add(colName);
                
                FKConfig fk = getFKConfig(colName);
                currentDynamicFKs.add(fk);
                
                // Pre-fetch FK mapping cache for TableView Display
                Map<String, String> displayCache = null;
                if (fk != null) {
                    displayCache = new HashMap<>();
                    try(Statement lStmt = conn.createStatement(); ResultSet lRs = lStmt.executeQuery("SELECT " + fk.refIdCol + ", " + fk.refDisplayCol + " FROM " + fk.refTable)) {
                        while(lRs.next()) displayCache.put(lRs.getString(1), lRs.getString(2));
                    }
                }
                final Map<String, String> finalCache = displayCache;

                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(param -> {
                    String rawVal = param.getValue().get(j);
                    if (finalCache != null && rawVal != null && finalCache.containsKey(rawVal)) {
                        return new SimpleStringProperty(finalCache.get(rawVal)); // Show human name
                    }
                    return new SimpleStringProperty(rawVal);
                });
                table.getColumns().add(col);
                
                // Form Builder
                if (i == 1 || colName.equalsIgnoreCase(tableName + "ID") || colName.equalsIgnoreCase("id")) {
                    currentDynamicFields.add(null); // PK placeholder
                } else {
                    VBox fieldBox = new VBox(2); 
                    Control input;
                    if (fk != null) {
                        ComboBox<String> cb = new ComboBox<>(fetchList("SELECT " + fk.refDisplayCol + " FROM " + fk.refTable));
                        cb.setPromptText("Select " + fk.refTable);
                        input = cb;
                    } else {
                        input = new TextField();
                    }
                    currentDynamicFields.add(input); 
                    fieldBox.getChildren().addAll(new Label(colName + ":"), input); 
                    form.getChildren().add(fieldBox); 
                }
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) row.add(rs.getString(i) == null ? "" : rs.getString(i));
                data.add(row);
            }
            table.setItems(data);
            
            table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
                if (newSel != null) { 
                    for(int i = 0; i < currentDynamicFields.size(); i++) {
                        Control c = currentDynamicFields.get(i);
                        if (c == null) continue;
                        String rawVal = newSel.get(i);
                        if (c instanceof ComboBox) {
                            FKConfig fk = currentDynamicFKs.get(i);
                            String humanName = fetchName(fk.refTable, fk.refDisplayCol, fk.refIdCol, rawVal);
                            ((ComboBox<String>)c).setValue(humanName);
                        } else {
                            ((TextField)c).setText(rawVal);
                        }
                    }
                    deleteBtn.setDisable(false); updateBtn.setDisable(false); addBtn.setDisable(true); 
                } else { 
                    for(Control c : currentDynamicFields) { if (c instanceof TextField) ((TextField)c).clear(); else if (c != null) ((ComboBox)c).getSelectionModel().clearSelection(); }
                    deleteBtn.setDisable(true); updateBtn.setDisable(true); addBtn.setDisable(false); 
                }
            });
        } catch (SQLException e) { System.out.println("Error loading table: " + tableName + " - " + e.getMessage()); }
    }

    // =========================================================================
    // HELPER METHODS & UI CSS
    // =========================================================================

    private void showAlert(String title, String content) { Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
    private boolean confirmDelete() { Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO); return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES; }
    private double parseDoubleSafe(String val) { try { return (val==null || val.trim().isEmpty()) ? 0.0 : Double.parseDouble(val.trim()); } catch (Exception e) { return 0.0; } }
    private <T> TableColumn<T, String> createCol(String title, String property, double minWidth) { TableColumn<T, String> col = new TableColumn<>(title); col.setCellValueFactory(new PropertyValueFactory<>(property)); col.setMinWidth(minWidth); return col; }
    private String createInlineCSS() { return "data:text/css," + ".table-view { -fx-background-color: transparent; -fx-border-color: #d2d6de; } .table-view .column-header-background { -fx-background-color: #e9ecef; } " + ".action-button { -fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .action-button:hover { -fx-background-color: #3498db; } " + ".update-button { -fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .update-button:hover { -fx-background-color: #e67e22; } " + ".delete-button { -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } .delete-button:hover { -fx-background-color: #c0392b; }"; }

    // =========================================================================
    // UI DATA MODELS
    // =========================================================================
    
    public static class Company {
        private final SimpleStringProperty id, name, contact, email, phone, address;
        public Company(String id, String name, String contact, String email, String phone, String address) { this.id = new SimpleStringProperty(id); this.name = new SimpleStringProperty(name); this.contact = new SimpleStringProperty(contact); this.email = new SimpleStringProperty(email); this.phone = new SimpleStringProperty(phone); this.address = new SimpleStringProperty(address); }
        public String getId() { return id.get(); } public String getName() { return name.get(); } public String getContact() { return contact.get(); } public String getEmail() { return email.get(); } public String getPhone() { return phone.get(); } public String getAddress() { return address.get(); }
    }
    public static class Car {
        private final SimpleStringProperty id, regNo, make, model, type, fuel, status;
        public Car(String id, String regNo, String make, String model, String type, String fuel, String status) { this.id = new SimpleStringProperty(id); this.regNo = new SimpleStringProperty(regNo); this.make = new SimpleStringProperty(make); this.model = new SimpleStringProperty(model); this.type = new SimpleStringProperty(type); this.fuel = new SimpleStringProperty(fuel); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getRegNo() { return regNo.get(); } public String getMake() { return make.get(); } public String getModel() { return model.get(); } public String getType() { return type.get(); } public String getFuel() { return fuel.get(); } public String getStatus() { return status.get(); }
    }
    public static class Driver {
        private final SimpleStringProperty id, firstName, license, status;
        public Driver(String id, String fName, String license, String status) { this.id = new SimpleStringProperty(id); this.firstName = new SimpleStringProperty(fName); this.license = new SimpleStringProperty(license); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getFirstName() { return firstName.get(); } public String getLicense() { return license.get(); } public String getStatus() { return status.get(); }
    }
    public static class Booking {
        private final SimpleStringProperty id, company, employee, carType, serviceType, pickup, dropLoc, time, status;
        public Booking(String id, String company, String employee, String carType, String serviceType, String pickup, String dropLoc, String time, String status) { this.id = new SimpleStringProperty(id); this.company = new SimpleStringProperty(company); this.employee = new SimpleStringProperty(employee); this.carType = new SimpleStringProperty(carType); this.serviceType = new SimpleStringProperty(serviceType); this.pickup = new SimpleStringProperty(pickup); this.dropLoc = new SimpleStringProperty(dropLoc); this.time = new SimpleStringProperty(time); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getCompany() { return company.get(); } public String getEmployee() { return employee.get(); } public String getCarType() { return carType.get(); } public String getServiceType() { return serviceType.get(); } public String getPickup() { return pickup.get(); } public String getDropLoc() { return dropLoc.get(); } public String getTime() { return time.get(); } public String getStatus() { return status.get(); }
    }
    public static class RateCard {
        private final SimpleStringProperty id, company, carType, serviceType, baseFare, perKmRate, perHrRate;
        public RateCard(String id, String company, String carType, String serviceType, String baseFare, String perKmRate, String perHrRate) { 
            this.id = new SimpleStringProperty(id); 
            this.company = new SimpleStringProperty(company); 
            this.carType = new SimpleStringProperty(carType); 
            this.serviceType = new SimpleStringProperty(serviceType);
            this.baseFare = new SimpleStringProperty(baseFare); 
            this.perKmRate = new SimpleStringProperty(perKmRate); 
            this.perHrRate = new SimpleStringProperty(perHrRate); 
        }
        public String getId() { return id.get(); }
        public String getCompany() { return company.get(); }
        public String getCarType() { return carType.get(); }
        public String getServiceType() { return serviceType.get(); }
        public String getBaseFare() { return baseFare.get(); }
        public String getPerKmRate() { return perKmRate.get(); }
        public String getPerHrRate() { return perHrRate.get(); }
    }
    public static class Invoice {
        private final SimpleStringProperty id, bookingRef, company, total, status;
        public Invoice(String id, String bookingRef, String company, String total, String status) { this.id = new SimpleStringProperty(id); this.bookingRef = new SimpleStringProperty(bookingRef); this.company = new SimpleStringProperty(company); this.total = new SimpleStringProperty(total); this.status = new SimpleStringProperty(status); }
        public String getId() { return id.get(); } public String getBookingRef() { return bookingRef.get(); } public String getCompany() { return company.get(); } public String getTotal() { return total.get(); } public String getStatus() { return status.get(); }
    }
}
