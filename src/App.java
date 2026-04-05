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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CORPORATE CAR RENTAL SYSTEM - FULL JAVAFX UI
 * Includes Premium Dashboard, Date-based Car Availability, 
 * Auto-Sync Vehicle Statuses, explicit Car details in Billing, 
 * strict Global ID Counters, "Any" Wildcard Rate Card configs,
 * and Full Invoice Update support tied to Live Revenue.
 */
public class App extends Application {

    // --- MOCK DATABASE (Observable Data for UI binding) ---
    private final ObservableList<Company> companyData = FXCollections.observableArrayList();
    private final ObservableList<Car> carData = FXCollections.observableArrayList();
    private final ObservableList<Driver> driverData = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingData = FXCollections.observableArrayList();
    private final ObservableList<Invoice> invoiceData = FXCollections.observableArrayList();
    private final ObservableList<RateCard> rateCardData = FXCollections.observableArrayList();
    
    // Dynamic List for Service Types (updates everywhere)
    private final ObservableList<String> serviceTypeList = FXCollections.observableArrayList("8hrs - 80km", "12hrs - 120km", "Airport Transfer", "Local Fixed");

    // --- STRICT GLOBAL ID COUNTERS (Prevents Duplicate Overwrites) ---
    private int nextCompanyId = 3;
    private int nextVehicleId = 3;
    private int nextDriverId = 3;
    private int nextBookingId = 103;
    private int nextRateCardId = 4;
    private int nextInvoiceId = 1001;

    // --- UI COMPONENTS ---
    private StackPane contentArea;
    private Label headerTitle;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        seedMockData();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        HBox header = createHeader();
        root.setTop(header);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        switchView("Dashboard");

        Scene scene = new Scene(root, 1450, 850);
        scene.getStylesheets().add(createInlineCSS());

        primaryStage.setTitle("Corporate Car Rental ERP System");
        primaryStage.setScene(scene);
        primaryStage.show();
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
            
            btn.setOnAction(e -> switchView(item));
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
    // MODULE VIEWS
    // =========================================================================

    private VBox createDashboardView() {
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(10));

        // Welcome Section
        VBox welcomeBox = new VBox(5);
        Label welcomeTitle = new Label("Welcome back, System Administrator 👋");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeTitle.setTextFill(Color.web("#2c3e50"));
        Label welcomeSub = new Label("Here is what's happening with your fleet and bookings today.");
        welcomeSub.setFont(Font.font("System", FontWeight.NORMAL, 16));
        welcomeSub.setTextFill(Color.web("#7f8c8d"));
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSub);

        // Stats Section
        HBox statsBox = new HBox(25);
        
        // Dynamic Calculations
        long pendingBookings = bookingData.stream().filter(b -> b.getStatus().equals("Pending")).count();
        double totalRev = invoiceData.stream()
                .filter(inv -> "Paid".equals(inv.getStatus()))
                .mapToDouble(inv -> parseDoubleSafe(inv.getTotal()))
                .sum();

        statsBox.getChildren().addAll(
            createStatCard("🏢 Total Companies", String.valueOf(companyData.size()), "#2980b9", "#ebf5fb"),
            createStatCard("🚗 Active Fleet", String.valueOf(carData.size()), "#27ae60", "#eafaf1"),
            createStatCard("📅 Pending Bookings", String.valueOf(pendingBookings), "#e74c3c", "#fdedec"),
            createStatCard("💰 Total Revenue", "₹" + String.format("%.2f", totalRev), "#8e44ad", "#f5eef8")
        );

        // Recent Bookings Section
        VBox recentBox = new VBox(10);
        Label recentTitle = new Label("Recent Bookings Overview");
        recentTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        recentTitle.setTextFill(Color.web("#2c3e50"));
        
        TableView<Booking> recentTable = new TableView<>();
        recentTable.setItems(bookingData);
        recentTable.getColumns().addAll(
            createCol("BKG ID", "id", 100),
            createCol("Company", "company", 250),
            createCol("Service Type", "serviceType", 200),
            createCol("Date", "date", 150),
            createCol("Status", "status", 150)
        );
        recentTable.setPrefHeight(300);
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recentTable.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-background-radius: 5;");
        
        recentBox.getChildren().addAll(recentTitle, recentTable);

        layout.getChildren().addAll(welcomeBox, statsBox, recentBox);
        return layout;
    }

    private VBox createStatCard(String title, String value, String color, String bgColor) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, " + bgColor + "); " +
                      "-fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                      "-fx-border-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
        card.setPadding(new Insets(20));
        card.setPrefWidth(260);
        
        Label titleLabel = new Label(title); 
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        Label valueLabel = new Label(value); 
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 36)); 
        valueLabel.setTextFill(Color.web("#2c3e50"));
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    // --- COMPANY VIEW ---
    private VBox createCompanyView() {
        VBox layout = new VBox(15);
        TableView<Company> table = new TableView<>(companyData);
        table.getColumns().addAll(
            createCol("ID", "id", 50), createCol("Company Name", "name", 200), createCol("Contact Person", "contact", 150), 
            createCol("Email", "email", 200), createCol("Phone", "phone", 150), createCol("Billing Address", "address", 250)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField nameIn = new TextField(); nameIn.setPromptText("Company Name");
        TextField contactIn = new TextField(); contactIn.setPromptText("Contact Person");
        TextField emailIn = new TextField(); emailIn.setPromptText("Email Address");
        TextField phoneIn = new TextField(); phoneIn.setPromptText("Phone No.");
        TextField addressIn = new TextField(); addressIn.setPromptText("Full Billing Address"); addressIn.setPrefWidth(300);
        
        Button addBtn = new Button("Add Company"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        HBox btnBox = new HBox(10, addBtn, updateBtn);

        form.addRow(0, new Label("Company Name:"), nameIn, new Label("Contact Person:"), contactIn);
        form.addRow(1, new Label("Email Address:"), emailIn, new Label("Phone Number:"), phoneIn);
        form.addRow(2, new Label("Billing Address:"), addressIn, btnBox);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameIn.setText(newSel.getName()); contactIn.setText(newSel.getContact());
                emailIn.setText(newSel.getEmail()); phoneIn.setText(newSel.getPhone());
                addressIn.setText(newSel.getAddress());
                updateBtn.setDisable(false); addBtn.setDisable(true);
            } else { updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if(!nameIn.getText().isEmpty()) {
                companyData.add(new Company("C" + (nextCompanyId++), nameIn.getText(), contactIn.getText(), emailIn.getText(), phoneIn.getText(), addressIn.getText()));
                nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
            }
        });

        updateBtn.setOnAction(e -> {
            Company selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                selected.setName(nameIn.getText()); selected.setContact(contactIn.getText());
                selected.setEmail(emailIn.getText()); selected.setPhone(phoneIn.getText());
                selected.setAddress(addressIn.getText());
                table.refresh(); table.getSelectionModel().clearSelection();
                nameIn.clear(); contactIn.clear(); emailIn.clear(); phoneIn.clear(); addressIn.clear();
            }
        });

        layout.getChildren().addAll(new Label("Corporate Clients Registry"), table, form);
        return layout;
    }

    // --- VEHICLE VIEW ---
    private VBox createVehicleView() {
        VBox layout = new VBox(15);
        TableView<Car> table = new TableView<>(carData);
        table.getColumns().addAll(
            createCol("Car ID", "id", 60), createCol("Reg No", "regNo", 120), createCol("Make", "make", 120), 
            createCol("Model", "model", 120), createCol("Year", "year", 60), createCol("Seats", "capacity", 60),
            createCol("Luggage", "luggage", 80), createCol("Type", "type", 100), createCol("Fuel", "fuel", 80), createCol("Status", "status", 100)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField regIn = new TextField(); TextField makeIn = new TextField(); TextField modelIn = new TextField(); TextField yearIn = new TextField();
        TextField capIn = new TextField(); capIn.setPromptText("No. of Seats"); TextField lugIn = new TextField(); lugIn.setPromptText("Bags Count");
        ComboBox<String> typeIn = new ComboBox<>(FXCollections.observableArrayList("Sedan", "SUV", "Luxury", "Hatchback"));
        ComboBox<String> fuelIn = new ComboBox<>(FXCollections.observableArrayList("Petrol", "Diesel", "EV"));
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Available", "On Trip", "Maintenance")); statusIn.setValue("Available");
        
        Button addBtn = new Button("Add Vehicle"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        HBox btnBox = new HBox(10, addBtn, updateBtn);

        form.addRow(0, new Label("Reg No:"), regIn, new Label("Make:"), makeIn, new Label("Model:"), modelIn);
        form.addRow(1, new Label("Mfg Year:"), yearIn, new Label("Seats:"), capIn, new Label("Luggage:"), lugIn);
        form.addRow(2, new Label("Car Type:"), typeIn, new Label("Fuel Type:"), fuelIn, new Label("Status:"), statusIn);
        form.addRow(3, new Label(""), btnBox);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                regIn.setText(newSel.getRegNo()); makeIn.setText(newSel.getMake()); modelIn.setText(newSel.getModel());
                yearIn.setText(newSel.getYear()); capIn.setText(newSel.getCapacity()); lugIn.setText(newSel.getLuggage());
                typeIn.setValue(newSel.getType()); fuelIn.setValue(newSel.getFuel()); statusIn.setValue(newSel.getStatus());
                updateBtn.setDisable(false); addBtn.setDisable(true);
            } else { updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if(!regIn.getText().isEmpty()) {
                carData.add(new Car("V" + (nextVehicleId++), regIn.getText(), makeIn.getText(), modelIn.getText(), yearIn.getText(), capIn.getText(), lugIn.getText(), typeIn.getValue(), fuelIn.getValue(), statusIn.getValue()));
                regIn.clear(); makeIn.clear(); modelIn.clear(); yearIn.clear(); capIn.clear(); lugIn.clear();
            }
        });

        updateBtn.setOnAction(e -> {
            Car selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                selected.setRegNo(regIn.getText()); selected.setMake(makeIn.getText()); selected.setModel(modelIn.getText());
                selected.setYear(yearIn.getText()); selected.setCapacity(capIn.getText()); selected.setLuggage(lugIn.getText());
                selected.setType(typeIn.getValue()); selected.setFuel(fuelIn.getValue()); selected.setStatus(statusIn.getValue());
                table.refresh(); table.getSelectionModel().clearSelection();
                regIn.clear(); makeIn.clear(); modelIn.clear(); yearIn.clear(); capIn.clear(); lugIn.clear();
            }
        });

        layout.getChildren().addAll(new Label("Fleet & Vehicle Management"), table, form);
        return layout;
    }

    // --- DRIVER VIEW ---
    private VBox createDriverView() {
        VBox layout = new VBox(15);
        TableView<Driver> table = new TableView<>(driverData);
        table.getColumns().addAll(
            createCol("Driver ID", "id", 80), createCol("First Name", "firstName", 150), createCol("Last Name", "lastName", 150), 
            createCol("Phone", "phone", 120), createCol("License No", "license", 150), createCol("Shift", "shift", 100), createCol("Status", "status", 100)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        TextField fNameIn = new TextField(); TextField lNameIn = new TextField(); TextField phoneIn = new TextField(); TextField licIn = new TextField();
        ComboBox<String> shiftIn = new ComboBox<>(FXCollections.observableArrayList("Morning", "Night", "Split")); shiftIn.setValue("Morning");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Active", "On Leave", "Suspended")); statusIn.setValue("Active");
        
        Button addBtn = new Button("Add Driver"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Record"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        HBox btnBox = new HBox(10, addBtn, updateBtn);

        form.addRow(0, new Label("First Name:"), fNameIn, new Label("Last Name:"), lNameIn, new Label("Shift:"), shiftIn);
        form.addRow(1, new Label("Phone:"), phoneIn, new Label("License:"), licIn, new Label("Status:"), statusIn);
        form.addRow(2, new Label(""), btnBox);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fNameIn.setText(newSel.getFirstName()); lNameIn.setText(newSel.getLastName());
                phoneIn.setText(newSel.getPhone()); licIn.setText(newSel.getLicense());
                shiftIn.setValue(newSel.getShift()); statusIn.setValue(newSel.getStatus());
                updateBtn.setDisable(false); addBtn.setDisable(true);
            } else { updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if(!fNameIn.getText().isEmpty()) {
                driverData.add(new Driver("D" + (nextDriverId++), fNameIn.getText(), lNameIn.getText(), phoneIn.getText(), licIn.getText(), shiftIn.getValue(), statusIn.getValue()));
                fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
            }
        });

        updateBtn.setOnAction(e -> {
            Driver selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                selected.setFirstName(fNameIn.getText()); selected.setLastName(lNameIn.getText());
                selected.setPhone(phoneIn.getText()); selected.setLicense(licIn.getText());
                selected.setShift(shiftIn.getValue()); selected.setStatus(statusIn.getValue());
                table.refresh(); table.getSelectionModel().clearSelection();
                fNameIn.clear(); lNameIn.clear(); phoneIn.clear(); licIn.clear();
            }
        });

        layout.getChildren().addAll(new Label("Driver Roster & Shifts"), table, form);
        return layout;
    }

    // --- BOOKING VIEW (WITH DYNAMIC CAR AVAILABILITY) ---
    private VBox createBookingView() {
        VBox layout = new VBox(15);
        TableView<Booking> table = new TableView<>(bookingData);
        table.getColumns().addAll(
            createCol("BKG ID", "id", 80), createCol("Company", "company", 130), createCol("Employee", "employee", 100), 
            createCol("Service Type", "serviceType", 110), createCol("Assigned Car", "carRegNo", 130), 
            createCol("Car Type", "carType", 80), createCol("Fuel", "fuelType", 70), createCol("Driver", "driver", 110), 
            createCol("Date", "date", 90), createCol("Time", "time", 70), createCol("Status", "status", 90)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> compIn = new ComboBox<>(); companyData.forEach(c -> compIn.getItems().add(c.getName()));
        TextField empIn = new TextField(); empIn.setPromptText("Employee Name");
        ComboBox<String> serviceIn = new ComboBox<>(serviceTypeList); serviceIn.setEditable(true);
        DatePicker dateIn = new DatePicker(LocalDate.now()); 
        
        // Dynamic Car Selector
        ComboBox<String> carIn = new ComboBox<>(); carIn.setPromptText("Select Date First");
        TextField typeIn = new TextField(); typeIn.setEditable(false); typeIn.setStyle("-fx-background-color: #ecf0f1;");
        TextField fuelIn = new TextField(); fuelIn.setEditable(false); fuelIn.setStyle("-fx-background-color: #ecf0f1;");
        
        ComboBox<String> driverIn = new ComboBox<>(); driverIn.getItems().add("Unassigned"); 
        driverData.forEach(d -> driverIn.getItems().add(d.getFirstName() + " " + d.getLastName())); driverIn.setValue("Unassigned");
        
        TextField pickupIn = new TextField(); TextField dropIn = new TextField();
        TextField timeIn = new TextField("10:00 AM");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Pending", "Confirmed", "Completed", "Cancelled"));
        statusIn.setValue("Pending");
        
        Button addBtn = new Button("Create Booking"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Booking"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        HBox btnBox = new HBox(10, addBtn, updateBtn);

        // Date selection updates available cars
        dateIn.valueProperty().addListener((obs, oldVal, newVal) -> refreshAvailableCars(newVal, carIn, null));

        // Car selection auto-fills type and fuel
        carIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String regNo = newVal.split(" - ")[0];
                Car selectedCar = carData.stream().filter(c -> c.getRegNo().equals(regNo)).findFirst().orElse(null);
                if (selectedCar != null) {
                    typeIn.setText(selectedCar.getType());
                    fuelIn.setText(selectedCar.getFuel());
                }
            } else {
                typeIn.clear(); fuelIn.clear();
            }
        });

        // Trigger initial load
        refreshAvailableCars(dateIn.getValue(), carIn, null);

        form.addRow(0, new Label("Company:"), compIn, new Label("Employee:"), empIn, new Label("Date:"), dateIn);
        form.addRow(1, new Label("Service Type:"), serviceIn, new Label("Assign Car:"), carIn, new Label("Driver:"), driverIn);
        form.addRow(2, new Label("Car Type (Auto):"), typeIn, new Label("Fuel (Auto):"), fuelIn, new Label("Time:"), timeIn);
        form.addRow(3, new Label("Pickup Loc:"), pickupIn, new Label("Drop Loc:"), dropIn, new Label("Status:"), statusIn);
        form.addRow(4, new Label(""), btnBox);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                compIn.setValue(newSel.getCompany()); empIn.setText(newSel.getEmployee());
                serviceIn.setValue(newSel.getServiceType()); 
                dateIn.setValue(LocalDate.parse(newSel.getDate())); 
                
                // Refresh cars but allow the currently booked car to remain available
                refreshAvailableCars(dateIn.getValue(), carIn, newSel.getCarRegNo());
                
                // Select the actual car
                String carStr = carData.stream().filter(c -> c.getRegNo().equals(newSel.getCarRegNo())).map(c -> c.getRegNo() + " - " + c.getMake() + " " + c.getModel()).findFirst().orElse("");
                carIn.setValue(carStr);
                
                typeIn.setText(newSel.getCarType()); fuelIn.setText(newSel.getFuelType());
                driverIn.setValue(newSel.getDriver()); pickupIn.setText(newSel.getPickup()); 
                dropIn.setText(newSel.getDropLoc()); timeIn.setText(newSel.getTime()); 
                statusIn.setValue(newSel.getStatus());
                
                updateBtn.setDisable(false); addBtn.setDisable(true);
            } else { updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if(compIn.getValue() != null && carIn.getValue() != null && dateIn.getValue() != null) {
                if(serviceIn.getValue() != null && !serviceTypeList.contains(serviceIn.getValue())) {
                    serviceTypeList.add(serviceIn.getValue());
                }
                String regNo = carIn.getValue().split(" - ")[0];
                bookingData.add(new Booking("BKG-" + (nextBookingId++), compIn.getValue(), empIn.getText(), 
                        serviceIn.getValue(), regNo, typeIn.getText(), fuelIn.getText(), driverIn.getValue(), pickupIn.getText(), 
                        dropIn.getText(), dateIn.getValue().toString(), timeIn.getText(), statusIn.getValue()));
                
                empIn.clear(); pickupIn.clear(); dropIn.clear();
                
                // Auto Sync Car Status globally
                syncAllCarStatuses();
                
                // Refresh dropdown so car disappears for that date
                refreshAvailableCars(dateIn.getValue(), carIn, null);
            }
        });

        updateBtn.setOnAction(e -> {
            Booking selected = table.getSelectionModel().getSelectedItem();
            if(selected != null && carIn.getValue() != null) {
                if(serviceIn.getValue() != null && !serviceTypeList.contains(serviceIn.getValue())) {
                    serviceTypeList.add(serviceIn.getValue());
                }
                String regNo = carIn.getValue().split(" - ")[0];
                selected.setCompany(compIn.getValue()); selected.setEmployee(empIn.getText());
                selected.setServiceType(serviceIn.getValue()); selected.setCarRegNo(regNo);
                selected.setCarType(typeIn.getText()); selected.setFuelType(fuelIn.getText()); 
                selected.setDriver(driverIn.getValue()); selected.setPickup(pickupIn.getText()); 
                selected.setDropLoc(dropIn.getText()); selected.setDate(dateIn.getValue().toString()); 
                selected.setTime(timeIn.getText()); selected.setStatus(statusIn.getValue());
                
                table.refresh(); table.getSelectionModel().clearSelection();
                empIn.clear(); pickupIn.clear(); dropIn.clear();
                
                // Auto Sync Car Status globally (so if completed, it becomes available)
                syncAllCarStatuses();
                
                refreshAvailableCars(dateIn.getValue(), carIn, null);
            }
        });

        layout.getChildren().addAll(new Label("Booking Management (Dynamic Asset Allocation)"), table, form);
        return layout;
    }

    // Determines which cars are free on a given date
    private void refreshAvailableCars(LocalDate date, ComboBox<String> carIn, String currentBookedCar) {
        carIn.getItems().clear();
        if (date == null) return;
        String selectedDate = date.toString();

        for (Car c : carData) {
            boolean isBooked = false;
            for (Booking b : bookingData) {
                // If the car is booked on this date and not cancelled
                if (b.getDate().equals(selectedDate) && b.getCarRegNo().equals(c.getRegNo()) && !b.getStatus().equals("Cancelled")) {
                    // Unless it's the exact car we are currently editing
                    if (currentBookedCar != null && currentBookedCar.equals(c.getRegNo())) {
                        isBooked = false; 
                    } else {
                        isBooked = true;
                    }
                    break;
                }
            }
            if (!isBooked && !c.getStatus().equals("Maintenance")) {
                carIn.getItems().add(c.getRegNo() + " - " + c.getMake() + " " + c.getModel());
            }
        }
    }
    
    // Synchronizes the global status of all cars based on active bookings
    private void syncAllCarStatuses() {
        for (Car car : carData) {
            // Ignore cars that are manually set to Maintenance
            if (!"Maintenance".equals(car.getStatus())) {
                boolean hasActiveBooking = bookingData.stream()
                        .anyMatch(b -> b.getCarRegNo().equals(car.getRegNo()) && 
                                      ("Pending".equals(b.getStatus()) || "Confirmed".equals(b.getStatus())));
                car.setStatus(hasActiveBooking ? "On Trip" : "Available");
            }
        }
    }

    // --- RATE CARD VIEW ---
    private VBox createRateCardView() {
        VBox layout = new VBox(15);
        TableView<RateCard> table = new TableView<>(rateCardData);
        table.getColumns().addAll(
            createCol("ID", "id", 60), createCol("Company", "company", 160), createCol("Service Type", "serviceType", 140), 
            createCol("Car Type", "carType", 90), createCol("Fuel", "fuelType", 90), createCol("Base Fare", "baseFare", 90), 
            createCol("Incl KM", "inclKm", 70), createCol("Incl Hrs", "inclHrs", 70), 
            createCol("Extra KM/₹", "extraKmRate", 90), createCol("Extra Hr/₹", "extraHrRate", 90)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> compIn = new ComboBox<>(); companyData.forEach(c -> compIn.getItems().add(c.getName()));
        ComboBox<String> serviceIn = new ComboBox<>(serviceTypeList); serviceIn.setEditable(true);
        ComboBox<String> typeIn = new ComboBox<>(FXCollections.observableArrayList("Any", "Sedan", "SUV", "Luxury", "Hatchback"));
        typeIn.setValue("Any");
        ComboBox<String> fuelIn = new ComboBox<>(FXCollections.observableArrayList("Any", "Petrol", "Diesel", "EV"));
        fuelIn.setValue("Any");
        
        TextField baseFareIn = new TextField(); baseFareIn.setPromptText("e.g. 1500");
        TextField inclKmIn = new TextField(); inclKmIn.setPromptText("e.g. 80");
        TextField inclHrsIn = new TextField(); inclHrsIn.setPromptText("e.g. 8");
        TextField perKmIn = new TextField(); perKmIn.setPromptText("e.g. 15");
        TextField perHrIn = new TextField(); perHrIn.setPromptText("e.g. 150");
        
        Button addBtn = new Button("Add Rate Card"); addBtn.getStyleClass().add("action-button");
        Button updateBtn = new Button("Update Rate"); updateBtn.getStyleClass().add("update-button"); updateBtn.setDisable(true);
        HBox btnBox = new HBox(10, addBtn, updateBtn);

        form.addRow(0, new Label("Company:"), compIn, new Label("Service Type:"), serviceIn, new Label("Car Type:"), typeIn);
        form.addRow(1, new Label("Fuel Type:"), fuelIn, new Label("Base Fare (₹):"), baseFareIn, new Label("Included KM:"), inclKmIn);
        form.addRow(2, new Label("Included Hours:"), inclHrsIn, new Label("Extra per KM (₹):"), perKmIn, new Label("Extra per Hr (₹):"), perHrIn);
        form.addRow(3, new Label(""), btnBox);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                compIn.setValue(newSel.getCompany()); serviceIn.setValue(newSel.getServiceType()); 
                typeIn.setValue(newSel.getCarType()); fuelIn.setValue(newSel.getFuelType());
                baseFareIn.setText(newSel.getBaseFare()); inclKmIn.setText(newSel.getInclKm()); inclHrsIn.setText(newSel.getInclHrs());
                perKmIn.setText(newSel.getExtraKmRate()); perHrIn.setText(newSel.getExtraHrRate());
                updateBtn.setDisable(false); addBtn.setDisable(true);
            } else { updateBtn.setDisable(true); addBtn.setDisable(false); }
        });

        addBtn.setOnAction(e -> {
            if(compIn.getValue() != null && !perKmIn.getText().isEmpty()) {
                if(serviceIn.getValue() != null && !serviceTypeList.contains(serviceIn.getValue())) {
                    serviceTypeList.add(serviceIn.getValue());
                }
                rateCardData.add(new RateCard("RC-" + (nextRateCardId++), compIn.getValue(), serviceIn.getValue(), typeIn.getValue(), fuelIn.getValue(), baseFareIn.getText(), inclKmIn.getText(), inclHrsIn.getText(), perKmIn.getText(), perHrIn.getText()));
                baseFareIn.clear(); inclKmIn.clear(); inclHrsIn.clear(); perKmIn.clear(); perHrIn.clear();
            }
        });

        updateBtn.setOnAction(e -> {
            RateCard selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                if(serviceIn.getValue() != null && !serviceTypeList.contains(serviceIn.getValue())) {
                    serviceTypeList.add(serviceIn.getValue());
                }
                selected.setCompany(compIn.getValue()); selected.setServiceType(serviceIn.getValue()); 
                selected.setCarType(typeIn.getValue()); selected.setFuelType(fuelIn.getValue());
                selected.setBaseFare(baseFareIn.getText()); selected.setInclKm(inclKmIn.getText()); selected.setInclHrs(inclHrsIn.getText());
                selected.setExtraKmRate(perKmIn.getText()); selected.setExtraHrRate(perHrIn.getText());
                table.refresh(); table.getSelectionModel().clearSelection();
                baseFareIn.clear(); inclKmIn.clear(); inclHrsIn.clear(); perKmIn.clear(); perHrIn.clear();
            }
        });

        layout.getChildren().addAll(new Label("Dynamic Rate Card Configuration (Supports 'Any' Wildcards)"), table, form);
        return layout;
    }

    // --- BILLING / INVOICE VIEW ---
    private VBox createBillingView() {
        VBox layout = new VBox(15);
        TableView<Invoice> table = new TableView<>(invoiceData);
        table.getColumns().addAll(
            createCol("Inv ID", "id", 70), createCol("BKG Ref", "bookingRef", 80), createCol("Car Reg", "carRegNo", 110), 
            createCol("Company", "company", 130), createCol("KM", "distance", 50), createCol("Hours", "hours", 50), 
            createCol("Base Fare", "baseFare", 80), createCol("Extra KM", "distCharge", 80), createCol("Extra Hr", "hrCharge", 80), 
            createCol("Tolls", "tolls", 60), createCol("Tax", "tax", 60), createCol("Total", "total", 90), createCol("Status", "status", 80)
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        ComboBox<String> bkgIn = new ComboBox<>(); 
        bookingData.stream().filter(b -> !b.getStatus().equals("Cancelled")).forEach(b -> bkgIn.getItems().add(b.getId() + " - " + b.getCompany()));
        
        // Manual User Inputs
        TextField distanceIn = new TextField(); distanceIn.setPromptText("Enter Total KM");
        TextField hoursIn = new TextField(); hoursIn.setPromptText("Enter Total Hours");
        TextField tollsIn = new TextField("0.00"); TextField taxPctIn = new TextField("5.00");
        
        // Auto-fetched fields (Read-Only)
        TextField carRegIn = new TextField("Assigned Vehicle"); carRegIn.setEditable(false); carRegIn.setStyle("-fx-background-color: #ecf0f1; -fx-font-weight: bold;");
        TextField baseFareIn = new TextField("0.00"); baseFareIn.setEditable(false); baseFareIn.setStyle("-fx-background-color: #ecf0f1;");
        TextField inclKmIn = new TextField("0"); inclKmIn.setEditable(false); inclKmIn.setStyle("-fx-background-color: #ecf0f1;");
        TextField inclHrsIn = new TextField("0"); inclHrsIn.setEditable(false); inclHrsIn.setStyle("-fx-background-color: #ecf0f1;");
        TextField extraKmRateIn = new TextField("0.00"); extraKmRateIn.setEditable(false); extraKmRateIn.setStyle("-fx-background-color: #ecf0f1;");
        TextField extraHrRateIn = new TextField("0.00"); extraHrRateIn.setEditable(false); extraHrRateIn.setStyle("-fx-background-color: #ecf0f1;");
        
        ComboBox<String> payModeIn = new ComboBox<>(FXCollections.observableArrayList("Credit Card", "Bank Transfer", "Corporate Wallet", "Cash")); payModeIn.setValue("Corporate Wallet");
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("Unpaid", "Paid", "Void")); statusIn.setValue("Unpaid");
        
        Label rateWarningLabel = new Label("Awaiting Booking Selection...");
        rateWarningLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Button calcBtn = new Button("Generate Invoice"); 
        calcBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        Button updateBtn = new Button("Update Invoice");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        updateBtn.setDisable(true);
        
        HBox btnBox = new HBox(10, calcBtn, updateBtn);

        // SMART RATE CARD AUTO-FETCHER WITH WILDCARD MATCHING
        bkgIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String bkgId = newVal.split(" - ")[0];
                Booking selectedBkg = bookingData.stream().filter(b -> b.getId().equals(bkgId)).findFirst().orElse(null);
                
                if (selectedBkg != null) {
                    
                    // Robust string cleaning to prevent invisible space mismatch bugs
                    String reqComp = selectedBkg.getCompany() != null ? selectedBkg.getCompany().trim() : "";
                    String reqServ = selectedBkg.getServiceType() != null ? selectedBkg.getServiceType().trim() : "";
                    String reqCar = selectedBkg.getCarType() != null ? selectedBkg.getCarType().trim() : "";
                    String reqFuel = selectedBkg.getFuelType() != null ? selectedBkg.getFuelType().trim() : "";
                    
                    // Display explicit Car details so user knows what parameters the system is searching for
                    carRegIn.setText(selectedBkg.getCarRegNo() + " (" + reqCar + ", " + reqFuel + ")"); 
                    
                    boolean rateFound = false;
                    
                    for (RateCard rc : rateCardData) {
                        String rcComp = rc.getCompany() != null ? rc.getCompany().trim() : "";
                        String rcServ = rc.getServiceType() != null ? rc.getServiceType().trim() : "";
                        String rcCar = rc.getCarType() != null ? rc.getCarType().trim() : "";
                        String rcFuel = rc.getFuelType() != null ? rc.getFuelType().trim() : "";
                        
                        // Supports exact matches OR "Any" wildcard match
                        boolean compMatch = reqComp.equalsIgnoreCase(rcComp);
                        boolean servMatch = reqServ.equalsIgnoreCase(rcServ);
                        boolean carMatch = rcCar.equalsIgnoreCase("Any") || reqCar.equalsIgnoreCase(rcCar);
                        boolean fuelMatch = rcFuel.equalsIgnoreCase("Any") || reqFuel.equalsIgnoreCase(rcFuel);
                        
                        if (compMatch && servMatch && carMatch && fuelMatch) {
                            baseFareIn.setText(rc.getBaseFare()); inclKmIn.setText(rc.getInclKm()); inclHrsIn.setText(rc.getInclHrs());
                            extraKmRateIn.setText(rc.getExtraKmRate()); extraHrRateIn.setText(rc.getExtraHrRate());
                            rateFound = true;
                            break;
                        }
                    }
                    
                    if(!rateFound) {
                        baseFareIn.setText("0.00"); inclKmIn.setText("0"); inclHrsIn.setText("0");
                        extraKmRateIn.setText("0.00"); extraHrRateIn.setText("0.00");
                        rateWarningLabel.setText("⚠️ No Rate Card for: " + reqComp + " | " + reqServ + " | " + reqCar + " | " + reqFuel);
                        rateWarningLabel.setTextFill(Color.RED);
                    } else {
                        rateWarningLabel.setText("✓ Rate Card Applied Successfully.");
                        rateWarningLabel.setTextFill(Color.web("#27ae60"));
                    }
                }
            }
        });

        // POPULATE FORM WHEN INVOICE IS SELECTED FOR UPDATING
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                String bkgRef = newSel.getBookingRef();
                for (String item : bkgIn.getItems()) {
                    if (item.startsWith(bkgRef + " -")) {
                        bkgIn.setValue(item);
                        break;
                    }
                }
                distanceIn.setText(newSel.getDistance());
                hoursIn.setText(newSel.getHours());
                tollsIn.setText(newSel.getTolls());
                payModeIn.setValue(newSel.getPayMode());
                statusIn.setValue(newSel.getStatus());
                
                updateBtn.setDisable(false);
                calcBtn.setDisable(true);
            } else {
                updateBtn.setDisable(true);
                calcBtn.setDisable(false);
            }
        });

        form.addRow(0, new Label("Select Booking:"), bkgIn, new Label("Vehicle Assigned:"), carRegIn, new Label("Status:"), statusIn);
        form.addRow(1, new Label("Total Run (KM):"), distanceIn, new Label("Total Time (Hrs):"), hoursIn, new Label("Tolls (₹):"), tollsIn);
        form.addRow(2, new Label("Base Fare Auto:"), baseFareIn, new Label("Incl KM Limit:"), inclKmIn, new Label("Incl Hrs Limit:"), inclHrsIn);
        form.addRow(3, new Label("Extra KM Rate:"), extraKmRateIn, new Label("Extra Hr Rate:"), extraHrRateIn, new Label("Tax Pct (%):"), taxPctIn);
        form.addRow(4, new Label("Pay Mode:"), payModeIn, rateWarningLabel, btnBox);
        GridPane.setColumnSpan(btnBox, 2);

        calcBtn.setOnAction(e -> {
            if(bkgIn.getValue() != null) {
                String bkgRef = bkgIn.getValue().split(" - ")[0]; 
                String comp = bkgIn.getValue().split(" - ")[1];
                String carAssigned = carRegIn.getText().split(" \\(")[0];
                
                // Using Safe Parsing to prevent Silent Failures and Exception crashes
                double dist = parseDoubleSafe(distanceIn.getText());
                double hrs = parseDoubleSafe(hoursIn.getText());
                double base = parseDoubleSafe(baseFareIn.getText());
                double inclKm = parseDoubleSafe(inclKmIn.getText());
                double inclHrs = parseDoubleSafe(inclHrsIn.getText());
                double extraKmRate = parseDoubleSafe(extraKmRateIn.getText());
                double extraHrRate = parseDoubleSafe(extraHrRateIn.getText());
                double tollAmt = parseDoubleSafe(tollsIn.getText());
                double taxPct = parseDoubleSafe(taxPctIn.getText());
                
                // Only charge if trip exceeds included limits
                double billableKm = Math.max(0, dist - inclKm);
                double billableHrs = Math.max(0, hrs - inclHrs);
                
                double distCharge = billableKm * extraKmRate;
                double hrCharge = billableHrs * extraHrRate;
                
                double subtotal = base + distCharge + hrCharge + tollAmt;
                double taxAmount = subtotal * (taxPct / 100);
                double totalAmount = subtotal + taxAmount;
                
                String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
                invoiceData.add(new Invoice(
                    "INV-" + (nextInvoiceId++), bkgRef, comp, carAssigned,
                    String.valueOf(dist), String.valueOf(hrs), String.format("%.2f", base), String.format("%.2f", distCharge), String.format("%.2f", hrCharge),
                    String.format("%.2f", tollAmt), String.format("%.2f", taxAmount), String.format("%.2f", totalAmount), 
                    payModeIn.getValue(), statusIn.getValue(), dateStr
                ));
                
                // Provide visual feedback
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Invoice Generated Successfully");
                alert.setContentText("Total Billing Amount: ₹" + String.format("%.2f", totalAmount));
                alert.showAndWait();
                
                distanceIn.clear(); hoursIn.clear(); tollsIn.setText("0.00");
            }
        });

        updateBtn.setOnAction(e -> {
            Invoice selected = table.getSelectionModel().getSelectedItem();
            if(selected != null && bkgIn.getValue() != null) {
                double dist = parseDoubleSafe(distanceIn.getText());
                double hrs = parseDoubleSafe(hoursIn.getText());
                double base = parseDoubleSafe(baseFareIn.getText());
                double inclKm = parseDoubleSafe(inclKmIn.getText());
                double inclHrs = parseDoubleSafe(inclHrsIn.getText());
                double extraKmRate = parseDoubleSafe(extraKmRateIn.getText());
                double extraHrRate = parseDoubleSafe(extraHrRateIn.getText());
                double tollAmt = parseDoubleSafe(tollsIn.getText());
                double taxPct = parseDoubleSafe(taxPctIn.getText());
                
                // Only charge if trip exceeds included limits
                double billableKm = Math.max(0, dist - inclKm);
                double billableHrs = Math.max(0, hrs - inclHrs);
                
                double distCharge = billableKm * extraKmRate;
                double hrCharge = billableHrs * extraHrRate;
                
                double subtotal = base + distCharge + hrCharge + tollAmt;
                double taxAmount = subtotal * (taxPct / 100);
                double totalAmount = subtotal + taxAmount;
                
                selected.setDistance(String.valueOf(dist));
                selected.setHours(String.valueOf(hrs));
                selected.setTolls(String.format("%.2f", tollAmt));
                selected.setDistCharge(String.format("%.2f", distCharge));
                selected.setHrCharge(String.format("%.2f", hrCharge));
                selected.setTax(String.format("%.2f", taxAmount));
                selected.setTotal(String.format("%.2f", totalAmount));
                selected.setPayMode(payModeIn.getValue());
                selected.setStatus(statusIn.getValue());
                
                table.refresh();
                table.getSelectionModel().clearSelection();
                
                // Provide visual feedback
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Invoice Updated Successfully");
                alert.setContentText("Updated Billing Amount: ₹" + String.format("%.2f", totalAmount));
                alert.showAndWait();
                
                distanceIn.clear(); hoursIn.clear(); tollsIn.setText("0.00");
            }
        });

        layout.getChildren().addAll(new Label("Smart Execution & Billing Calculation"), table, form);
        return layout;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    // Safely parse numerical inputs, preventing the system from failing if a field is left empty
    private double parseDoubleSafe(String val) {
        try {
            if(val == null || val.trim().isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private <T> TableColumn<T, String> createCol(String title, String property, double minWidth) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(minWidth);
        return col;
    }

    private String createInlineCSS() {
        return "data:text/css," +
               ".table-view { -fx-background-color: transparent; -fx-border-color: #d2d6de; } " +
               ".table-view .column-header-background { -fx-background-color: #e9ecef; } " +
               ".action-button { -fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } " +
               ".action-button:hover { -fx-background-color: #3498db; } " +
               ".update-button { -fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; } " +
               ".update-button:hover { -fx-background-color: #e67e22; } " +
               ".text-field, .combo-box, .date-picker { -fx-font-size: 13px; -fx-padding: 5px; }";
    }

    private void seedMockData() {
        companyData.add(new Company("C1", "TechCorp Global", "John Doe", "john@techcorp.com", "123-456-7890", "12 Silicon Blvd, NY"));
        companyData.add(new Company("C2", "Acme Industries", "Jane Smith", "jane@acme.com", "987-654-3210", "44 Factory Ln, NJ"));
        
        carData.add(new Car("V1", "KA-01-AB-1234", "Toyota", "Innova Crysta", "2022", "7", "4", "SUV", "Diesel", "Available"));
        carData.add(new Car("V2", "MH-12-XY-9876", "Honda", "City", "2023", "5", "3", "Sedan", "Petrol", "Available"));

        driverData.add(new Driver("D1", "Ramesh", "Kumar", "9876543210", "DL-123456", "Morning", "Active"));
        driverData.add(new Driver("D2", "Suresh", "Singh", "9123456780", "DL-654321", "Night", "Active"));

        bookingData.add(new Booking("BKG-101", "TechCorp Global", "Alice M", "8hrs - 80km", "KA-01-AB-1234", "SUV", "Diesel", "Ramesh Kumar", "Airport T2", "TechCorp HQ", "2023-10-25", "10:00 AM", "Completed"));
        bookingData.add(new Booking("BKG-102", "Acme Industries", "Bob B", "Airport Transfer", "MH-12-XY-9876", "Sedan", "Petrol", "Unassigned", "City Center", "Acme Factory", "2023-10-28", "14:30", "Pending"));
        
        // Accurate Rate Cards mappings based on seeded booking data
        rateCardData.add(new RateCard("RC-1", "TechCorp Global", "8hrs - 80km", "SUV", "Diesel", "1800.00", "80", "8", "16.50", "150.00"));
        rateCardData.add(new RateCard("RC-2", "TechCorp Global", "Airport Transfer", "Sedan", "Petrol", "900.00", "40", "2", "11.50", "100.00"));
        // Fixed Rate Card for Acme Industries so that BKG-102 doesn't fail default to 0!
        rateCardData.add(new RateCard("RC-3", "Acme Industries", "Airport Transfer", "Sedan", "Petrol", "1200.00", "50", "3", "10.00", "120.00"));

        invoiceData.add(new Invoice("INV-1000", "BKG-101", "TechCorp Global", "KA-01-AB-1234", "95", "9", "1800.00", "247.50", "150.00", "50.00", "112.38", "2359.88", "Corporate Wallet", "Paid", "2023-10-25 18:00"));
        
        // Run initial status sync for pre-loaded data
        syncAllCarStatuses();
    }

    // =========================================================================
    // DATA MODELS WITH FULL SETTERS
    // =========================================================================

    public static class Company {
        private final SimpleStringProperty id, name, contact, email, phone, address;
        public Company(String id, String name, String contact, String email, String phone, String address) {
            this.id = new SimpleStringProperty(id); this.name = new SimpleStringProperty(name); this.contact = new SimpleStringProperty(contact);
            this.email = new SimpleStringProperty(email); this.phone = new SimpleStringProperty(phone); this.address = new SimpleStringProperty(address);
        }
        public String getId() { return id.get(); } public String getName() { return name.get(); } public String getContact() { return contact.get(); }
        public String getEmail() { return email.get(); } public String getPhone() { return phone.get(); } public String getAddress() { return address.get(); }
        
        public void setName(String val) { name.set(val); } public void setContact(String val) { contact.set(val); }
        public void setEmail(String val) { email.set(val); } public void setPhone(String val) { phone.set(val); }
        public void setAddress(String val) { address.set(val); }
    }

    public static class Car {
        private final SimpleStringProperty id, regNo, make, model, year, capacity, luggage, type, fuel, status;
        public Car(String id, String regNo, String make, String model, String year, String capacity, String luggage, String type, String fuel, String status) {
            this.id = new SimpleStringProperty(id); this.regNo = new SimpleStringProperty(regNo); this.make = new SimpleStringProperty(make);
            this.model = new SimpleStringProperty(model); this.year = new SimpleStringProperty(year); this.capacity = new SimpleStringProperty(capacity);
            this.luggage = new SimpleStringProperty(luggage); this.type = new SimpleStringProperty(type); this.fuel = new SimpleStringProperty(fuel); this.status = new SimpleStringProperty(status);
        }
        public String getId() { return id.get(); } public String getRegNo() { return regNo.get(); } public String getMake() { return make.get(); }
        public String getModel() { return model.get(); } public String getYear() { return year.get(); } public String getCapacity() { return capacity.get(); }
        public String getLuggage() { return luggage.get(); } public String getType() { return type.get(); } public String getFuel() { return fuel.get(); } public String getStatus() { return status.get(); }

        public void setRegNo(String v){regNo.set(v);} public void setMake(String v){make.set(v);} public void setModel(String v){model.set(v);}
        public void setYear(String v){year.set(v);} public void setCapacity(String v){capacity.set(v);} public void setLuggage(String v){luggage.set(v);}
        public void setType(String v){type.set(v);} public void setFuel(String v){fuel.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class Driver {
        private final SimpleStringProperty id, firstName, lastName, phone, license, shift, status;
        public Driver(String id, String fName, String lName, String phone, String license, String shift, String status) {
            this.id = new SimpleStringProperty(id); this.firstName = new SimpleStringProperty(fName); this.lastName = new SimpleStringProperty(lName);
            this.phone = new SimpleStringProperty(phone); this.license = new SimpleStringProperty(license); this.shift = new SimpleStringProperty(shift); this.status = new SimpleStringProperty(status);
        }
        public String getId() { return id.get(); } public String getFirstName() { return firstName.get(); } public String getLastName() { return lastName.get(); }
        public String getPhone() { return phone.get(); } public String getLicense() { return license.get(); } public String getShift() { return shift.get(); } public String getStatus() { return status.get(); }

        public void setFirstName(String v){firstName.set(v);} public void setLastName(String v){lastName.set(v);}
        public void setPhone(String v){phone.set(v);} public void setLicense(String v){license.set(v);}
        public void setShift(String v){shift.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class Booking {
        private final SimpleStringProperty id, company, employee, serviceType, carRegNo, carType, fuelType, driver, pickup, dropLoc, date, time, status;
        public Booking(String id, String company, String employee, String serviceType, String carRegNo, String carType, String fuelType, String driver, String pickup, String dropLoc, String date, String time, String status) {
            this.id = new SimpleStringProperty(id); this.company = new SimpleStringProperty(company); this.employee = new SimpleStringProperty(employee);
            this.serviceType = new SimpleStringProperty(serviceType); this.carRegNo = new SimpleStringProperty(carRegNo);
            this.carType = new SimpleStringProperty(carType); this.fuelType = new SimpleStringProperty(fuelType); 
            this.driver = new SimpleStringProperty(driver); this.pickup = new SimpleStringProperty(pickup);
            this.dropLoc = new SimpleStringProperty(dropLoc); this.date = new SimpleStringProperty(date); this.time = new SimpleStringProperty(time); this.status = new SimpleStringProperty(status);
        }
        public String getId() { return id.get(); } public String getCompany() { return company.get(); } public String getEmployee() { return employee.get(); }
        public String getServiceType() { return serviceType.get(); } public String getCarRegNo() { return carRegNo.get(); } 
        public String getCarType() { return carType.get(); } public String getFuelType() { return fuelType.get(); }
        public String getDriver() { return driver.get(); } public String getPickup() { return pickup.get(); }
        public String getDropLoc() { return dropLoc.get(); } public String getDate() { return date.get(); } public String getTime() { return time.get(); } public String getStatus() { return status.get(); }

        public void setCompany(String v){company.set(v);} public void setEmployee(String v){employee.set(v);}
        public void setServiceType(String v){serviceType.set(v);} public void setCarRegNo(String v){carRegNo.set(v);}
        public void setCarType(String v){carType.set(v);} public void setFuelType(String v){fuelType.set(v);}
        public void setDriver(String v){driver.set(v);} public void setPickup(String v){pickup.set(v);}
        public void setDropLoc(String v){dropLoc.set(v);} public void setDate(String v){date.set(v);}
        public void setTime(String v){time.set(v);} public void setStatus(String v){status.set(v);}
    }

    public static class RateCard {
        private final SimpleStringProperty id, company, serviceType, carType, fuelType, baseFare, inclKm, inclHrs, extraKmRate, extraHrRate;
        public RateCard(String id, String company, String serviceType, String carType, String fuelType, String baseFare, String inclKm, String inclHrs, String extraKmRate, String extraHrRate) {
            this.id = new SimpleStringProperty(id); this.company = new SimpleStringProperty(company); this.serviceType = new SimpleStringProperty(serviceType);
            this.carType = new SimpleStringProperty(carType); this.fuelType = new SimpleStringProperty(fuelType);
            this.baseFare = new SimpleStringProperty(baseFare); this.inclKm = new SimpleStringProperty(inclKm); this.inclHrs = new SimpleStringProperty(inclHrs);
            this.extraKmRate = new SimpleStringProperty(extraKmRate); this.extraHrRate = new SimpleStringProperty(extraHrRate);
        }
        public String getId() { return id.get(); } public String getCompany() { return company.get(); } public String getServiceType() { return serviceType.get(); }
        public String getCarType() { return carType.get(); } public String getFuelType() { return fuelType.get(); }
        public String getBaseFare() { return baseFare.get(); } public String getInclKm() { return inclKm.get(); } public String getInclHrs() { return inclHrs.get(); }
        public String getExtraKmRate() { return extraKmRate.get(); } public String getExtraHrRate() { return extraHrRate.get(); }

        public void setCompany(String v){company.set(v);} public void setServiceType(String v){serviceType.set(v);} public void setCarType(String v){carType.set(v);}
        public void setFuelType(String v){fuelType.set(v);} public void setBaseFare(String v){baseFare.set(v);} public void setInclKm(String v){inclKm.set(v);}
        public void setInclHrs(String v){inclHrs.set(v);} public void setExtraKmRate(String v){extraKmRate.set(v);} public void setExtraHrRate(String v){extraHrRate.set(v);}
    }

    public static class Invoice {
        private final SimpleStringProperty id, bookingRef, company, carRegNo, distance, hours, baseFare, distCharge, hrCharge, tolls, tax, total, payMode, status, date;
        public Invoice(String id, String bookingRef, String company, String carRegNo, String distance, String hours, String baseFare, String distCharge, String hrCharge, String tolls, String tax, String total, String payMode, String status, String date) {
            this.id = new SimpleStringProperty(id); this.bookingRef = new SimpleStringProperty(bookingRef); this.company = new SimpleStringProperty(company);
            this.carRegNo = new SimpleStringProperty(carRegNo);
            this.distance = new SimpleStringProperty(distance); this.hours = new SimpleStringProperty(hours); this.baseFare = new SimpleStringProperty(baseFare); 
            this.distCharge = new SimpleStringProperty(distCharge); this.hrCharge = new SimpleStringProperty(hrCharge);
            this.tolls = new SimpleStringProperty(tolls); this.tax = new SimpleStringProperty(tax); this.total = new SimpleStringProperty(total);
            this.payMode = new SimpleStringProperty(payMode); this.status = new SimpleStringProperty(status); this.date = new SimpleStringProperty(date);
        }
        public String getId() { return id.get(); } public String getBookingRef() { return bookingRef.get(); } public String getCompany() { return company.get(); }
        public String getCarRegNo() { return carRegNo.get(); }
        public String getDistance() { return distance.get(); } public String getHours() { return hours.get(); } public String getBaseFare() { return baseFare.get(); } 
        public String getDistCharge() { return distCharge.get(); } public String getHrCharge() { return hrCharge.get(); }
        public String getTolls() { return tolls.get(); } public String getTax() { return tax.get(); } public String getTotal() { return total.get(); }
        public String getPayMode() { return payMode.get(); } public String getStatus() { return status.get(); } public String getDate() { return date.get(); }

        public void setDistance(String v) { distance.set(v); }
        public void setHours(String v) { hours.set(v); }
        public void setBaseFare(String v) { baseFare.set(v); }
        public void setDistCharge(String v) { distCharge.set(v); }
        public void setHrCharge(String v) { hrCharge.set(v); }
        public void setTolls(String v) { tolls.set(v); }
        public void setTax(String v) { tax.set(v); }
        public void setTotal(String v) { total.set(v); }
        public void setPayMode(String v) { payMode.set(v); }
        public void setStatus(String v) { status.set(v); }
        public void setDate(String v) { date.set(v); }
    }
}