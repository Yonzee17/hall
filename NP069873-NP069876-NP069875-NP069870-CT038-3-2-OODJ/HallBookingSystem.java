import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class HallBookingSystem
 {
    public static void main(String[] args) {
        new UserLoginFrame();
    }
}

class UserLoginFrame extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JButton loginButton, registerButton;

    public UserLoginFrame() {
        setTitle("Hall Booking System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("User Type:"));
        String[] userTypes = {"Super Account", "Scheduler", "Customer", "Administrator", "Manager"};
        userTypeCombo = new JComboBox<>(userTypes);
        add(userTypeCombo);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> new RoleSelectionForm());
        add(registerButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeCombo.getSelectedItem();

        if (username.equals("admin") && password.equals("admin") && userType.equals("Super Account")) {
            new SuperAccountDashboard();
            dispose();
        } else if (validateLogin(username, password, userType)) {
            JOptionPane.showMessageDialog(this, "Welcome " + userType + "!");
            switch (userType) {
                case "Scheduler": new SchedulerDashboard(); break;
                case "Customer": new CustomerDashboard(username); break;
                case "Administrator": new AdminDashboard(); break;
                case "Manager": new ManagerDashboard(); break;
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!");
        }
    }

    private boolean validateLogin(String username, String password, String userType) {
        try (Scanner scanner = new Scanner(new File("approved_users.txt"))) {
            while (scanner.hasNextLine()) {
                String[] userData = scanner.nextLine().split(",");
                if (userData.length == 3 && userData[0].equals(username) && userData[1].equals(password) && userData[2].equals(userType)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

class SuperAccountDashboard extends JFrame {
    public SuperAccountDashboard() {
        setTitle("Super Account Dashboard");
        setSize(400, 300);
        setLayout(new GridLayout(2, 1));

        JButton reviewRequestsButton = new JButton("Review Registration Requests");
        reviewRequestsButton.addActionListener(e -> new ReviewRegistrationRequests());
        add(reviewRequestsButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> { dispose(); new UserLoginFrame(); });
        add(logoutButton);

        setVisible(true);
    }
}

class RoleSelectionForm extends JFrame {
    public RoleSelectionForm() {
        setTitle("Select Role to Register");
        setSize(300, 200);
        setLayout(new GridLayout(3, 1));

        JButton customerButton = new JButton("Register as Customer");
        customerButton.addActionListener(e -> new RegistrationForm("Customer"));
        add(customerButton);

        JButton schedulerButton = new JButton("Register as Scheduler");
        schedulerButton.addActionListener(e -> new RegistrationForm("Scheduler"));
        add(schedulerButton);

        JButton managerButton = new JButton("Register as Manager");
        managerButton.addActionListener(e -> new RegistrationForm("Manager"));
        add(managerButton);
		
		JButton adminButton = new JButton("Register as Administrator");
        adminButton.addActionListener(e -> new RegistrationForm("Administrator"));
        add(adminButton);

        setVisible(true);
    }
}

class RegistrationForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private String role;

    public RegistrationForm(String role) {
        this.role = role;
        setTitle(role + " Registration");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> submitRequest());
        add(registerButton);

        setVisible(true);
    }

    private void submitRequest() {
        try (FileWriter writer = new FileWriter("registration_requests.txt", true)) {
            writer.write(usernameField.getText() + "," + new String(passwordField.getPassword()) + "," + role + "\n");
            JOptionPane.showMessageDialog(this, "Registration request submitted for approval.");
            dispose();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class HallInfo {
    public static final Map<String, Double> HALL_RATES = new HashMap<>();
    static {
        HALL_RATES.put("Auditorium", 300.0);
        HALL_RATES.put("Banquet Hall", 100.0);
        HALL_RATES.put("Meeting Room", 50.0);
    }

    public static boolean isValidTime(int hour) {
        return hour >= 8 && hour <= 18; 
    }

    public static String getHallRatesInfo() {
        StringBuilder info = new StringBuilder("Hall Booking Rates:\n");
        for (Map.Entry<String, Double> entry : HALL_RATES.entrySet()) {
            info.append(entry.getKey()).append(": RM ")
                .append(entry.getValue()).append(" per hour\n");
        }
        return info.toString();
    }
}


class ReviewRegistrationRequests extends JFrame {
    private JTextArea requestArea;

    public ReviewRegistrationRequests() {
        setTitle("Review Registration Requests");
        setSize(400, 300);
        setLayout(new BorderLayout());

        requestArea = new JTextArea();
        JButton approveButton = new JButton("Approve All");

        try (Scanner scanner = new Scanner(new File("registration_requests.txt"))) {
            while (scanner.hasNextLine()) {
                requestArea.append(scanner.nextLine() + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        add(new JScrollPane(requestArea), BorderLayout.CENTER);
        add(approveButton, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> approveRequest());
        setVisible(true);
    }

    private void approveRequest() {
        String[] requests = requestArea.getText().split("\n");
        try (FileWriter writer = new FileWriter("approved_users.txt", true);
             PrintWriter clear = new PrintWriter("registration_requests.txt")) {
            for (String request : requests) {
                writer.write(request + "\n");
            }
            clear.print("");
            requestArea.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}


// Customer Dashboard and Features
class CustomerDashboard extends JFrame {
    private String username;

    public CustomerDashboard(String username) {
        this.username = username;
        setTitle("Customer Dashboard");
        setSize(400, 400);
        setLayout(new GridLayout(7, 1));

        add(new JLabel("Logged in as: " + username));

        JButton updateProfileBtn = new JButton("Update Profile");
        updateProfileBtn.addActionListener(e -> new UpdateProfileForm(username));
        add(updateProfileBtn);

        JButton bookHallBtn = new JButton("Book a Hall");
        bookHallBtn.addActionListener(e -> new HallBookingForm(username));
        add(bookHallBtn);

        JButton viewBookingsBtn = new JButton("View My Bookings");
        viewBookingsBtn.addActionListener(e -> new ViewBookings(username));
        add(viewBookingsBtn);

        JButton cancelBookingBtn = new JButton("Cancel Booking");
        cancelBookingBtn.addActionListener(e -> new CancelBookingForm(username));
        add(cancelBookingBtn);

        JButton reportIssueBtn = new JButton("Report Issue");
        reportIssueBtn.addActionListener(e -> new IssueReportingForm(username));
        add(reportIssueBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new UserLoginFrame(); });
        add(logoutBtn);

        setVisible(true);
    }
}


class HallBookingForm extends JFrame {
    public HallBookingForm(String username) {
        setTitle("Book a Hall");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2));

        JComboBox<String> hallCombo = new JComboBox<>();
        try (Scanner scanner = new Scanner(new File("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt"))) {
            while (scanner.hasNextLine()) {
                hallCombo.addItem(scanner.nextLine());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JTextField yearField = new JTextField();
        JTextField monthField = new JTextField();
        JTextField dayField = new JTextField();

        add(new JLabel("Select Hall:"));
        add(hallCombo);
        add(new JLabel("Year (YYYY):"));
        add(yearField);
        add(new JLabel("Month (MM):"));
        add(monthField);
        add(new JLabel("Day (DD):"));
        add(dayField);

        JButton payButton = new JButton("Confirm Booking & Pay");
        add(new JLabel());
        add(payButton);

        payButton.addActionListener(e -> {
            String hall = (String) hallCombo.getSelectedItem();
            String year = yearField.getText().trim();
            String rawMonth = monthField.getText().trim();
			String month;

			Map<String, String> monthMap = new HashMap<>();
			monthMap.put("january", "01");
			monthMap.put("february", "02");
			monthMap.put("march", "03");
			monthMap.put("april", "04");
			monthMap.put("may", "05");
			monthMap.put("june", "06");
			monthMap.put("july", "07");
			monthMap.put("august", "08");
			monthMap.put("september", "09");
			monthMap.put("october", "10");
			monthMap.put("november", "11");
			monthMap.put("december", "12");

			
			try {
				int m = Integer.parseInt(rawMonth);
				if (m >= 1 && m <= 12) {
					month = String.format("%02d", m);
				} else {
					throw new NumberFormatException("Invalid month number");
				}
			} catch (NumberFormatException ex) {
				String lowered = rawMonth.toLowerCase();
				if (monthMap.containsKey(lowered)) {
					month = monthMap.get(lowered);
				} else {
					JOptionPane.showMessageDialog(this, "Invalid month. Please enter a valid number (1-12) or name (e.g., June).");
					return;
				}
			}

            String day = dayField.getText().trim();

            if (year.isEmpty() || month.isEmpty() || day.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter year, month, and day.");
                return;
            }

			String date = year + "-" + month + "-" + String.format("%02d", Integer.parseInt(day));

            // Check maintenance first
            boolean maintenanceFound = false;
            StringBuilder maintenanceRemarks = new StringBuilder();
            try (Scanner scanner = new Scanner(new File("hall_maintenance.txt"))) {
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    if (parts.length >= 4 && parts[0].equals(hall) && parts[1].equals(date)) {
                        maintenanceFound = true;
                        maintenanceRemarks.append("Maintenance from ").append(parts[2])
                                .append(" - Remarks: ").append(parts[3]).append("\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (maintenanceFound) {
                JOptionPane.showMessageDialog(this,
                        "Note: Maintenance is scheduled for this hall on " + date + ":\n" + maintenanceRemarks,
                        "Maintenance Alert", JOptionPane.INFORMATION_MESSAGE);
				return;
            }

            
            boolean isAvailable = true;
            try (Scanner scanner = new Scanner(new File("bookings.txt"))) {
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    if (parts.length == 3 && parts[1].equals(hall) && parts[2].equals(date)) {
                        isAvailable = false;
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (!isAvailable) {
                JOptionPane.showMessageDialog(this, "This hall is already booked on the selected date.");
                return;
            }

            
            try (FileWriter writer = new FileWriter("bookings.txt", true)) {
                writer.write(username + "," + hall + "," + date + "\n");
                JOptionPane.showMessageDialog(this, "Booking Confirmed. Receipt Generated.");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

class ViewBookings extends JFrame {
    public ViewBookings(String username) {
        setTitle("My Bookings");
        setSize(400, 300);
        JTextArea area = new JTextArea();

        try (Scanner scanner = new Scanner(new File("bookings.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(username + ",")) {
                    area.append(line + "\n");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        add(new JScrollPane(area));
        setVisible(true);
    }
}

class CancelBookingForm extends JFrame {
    public CancelBookingForm(String username) {
        setTitle("Cancel Booking");
        setSize(400, 300);
        JTextArea area = new JTextArea();

        List<String> updatedBookings = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("bookings.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username)) {
                    LocalDate bookingDate = LocalDate.parse(parts[2]);
                    if (bookingDate.isAfter(LocalDate.now().plusDays(3))) {
                        area.append(line + "\n");
                    } else {
                        updatedBookings.add(line);
                    }
                } else {
                    updatedBookings.add(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        add(new JScrollPane(area));

        JButton cancelBtn = new JButton("Cancel All Eligible");
        cancelBtn.addActionListener(e -> {
            try (FileWriter writer = new FileWriter("bookings.txt")) {
                for (String entry : updatedBookings) {
                    writer.write(entry + "\n");
                }
                JOptionPane.showMessageDialog(this, "Eligible bookings cancelled.");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        add(cancelBtn, BorderLayout.SOUTH);
        setVisible(true);
    }
}

class IssueReportingForm extends JFrame {
    public IssueReportingForm(String username) {
        setTitle("Report Issue");
        setSize(400, 300);
        setLayout(new BorderLayout());

        JTextArea issueArea = new JTextArea("Describe issue...");
        JButton submit = new JButton("Submit");

        submit.addActionListener(e -> {
            String issueDescription = issueArea.getText().trim();
            if (issueDescription.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Issue description cannot be empty.");
                return;
            }

            String issueId = "ISSUE" + System.currentTimeMillis(); // Unique ID using timestamp

            try (FileWriter writer = new FileWriter("issues.txt", true)) {
                writer.write(issueId + "," + username + "," + issueDescription + "\n");
                JOptionPane.showMessageDialog(this, "Issue reported with ID: " + issueId);
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        add(new JScrollPane(issueArea), BorderLayout.CENTER);
        add(submit, BorderLayout.SOUTH);
        setVisible(true);
    }
}


class UpdateProfileForm extends JFrame {
    public UpdateProfileForm(String currentUsername) {
        setTitle("Update Username & Password");
        setSize(350, 200);
        setLayout(new GridLayout(4, 2));

        JTextField usernameField = new JTextField(currentUsername);
        JPasswordField passwordField = new JPasswordField();
        JButton updateBtn = new JButton("Update");

        add(new JLabel("New Username:"));
        add(usernameField);
        add(new JLabel("New Password:"));
        add(passwordField);
        add(new JLabel()); // spacer
        add(updateBtn);

        updateBtn.addActionListener(e -> {
            String newUsername = usernameField.getText().trim();
            String newPassword = new String(passwordField.getPassword()).trim();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
                return;
            }

            List<String> updatedUsers = new ArrayList<>();
            boolean found = false;

            try (Scanner scanner = new Scanner(new File("approved_users.txt"))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length == 3 && parts[0].trim().equals(currentUsername.trim()) && parts[2].equals("Customer")) {
                        updatedUsers.add(newUsername + "," + newPassword + ",Customer");
                        found = true;
                    } else {
                        updatedUsers.add(line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "User not found in approved_users.txt.");
                return;
            }

            try (FileWriter writer = new FileWriter("approved_users.txt")) {
                for (String user : updatedUsers) {
                    writer.write(user + "\n");
                }
                JOptionPane.showMessageDialog(this, "Profile updated. Please login again.");
                dispose();
                new UserLoginFrame(); // back to login
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}



//Scheduler 
class SchedulerDashboard extends JFrame {
    public SchedulerDashboard() {
        setTitle("Scheduler Dashboard");
        setSize(400, 400);
        setLayout(new GridLayout(7, 1));

        JButton manageHallsBtn = new JButton("Manage Halls");
        manageHallsBtn.addActionListener(e -> new HallManagementForm());
        add(manageHallsBtn);

        JButton setAvailabilityBtn = new JButton("Set Hall Availability");
        setAvailabilityBtn.addActionListener(e -> new AvailabilityForm());
        add(setAvailabilityBtn);

        JButton setMaintenanceBtn = new JButton("Set Hall Maintenance");
        setMaintenanceBtn.addActionListener(e -> new MaintenanceForm());
        add(setMaintenanceBtn);
		
		JButton viewMaintenanceBtn = new JButton("View Maintenance Schedule");
		viewMaintenanceBtn.addActionListener(e -> new MaintenanceScheduleViewer());
		add(viewMaintenanceBtn);


        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new UserLoginFrame();
        });
        add(logoutBtn);

        setVisible(true);
    }
}

class HallManagementForm extends JFrame {
    public HallManagementForm() {
        setTitle("Hall Management");
        setSize(500, 400);
        setLayout(new GridLayout(5, 1));

        JButton addBtn = new JButton("Add Hall");
        JButton viewBtn = new JButton("View Halls");
        JButton editBtn = new JButton("Edit Hall");
        JButton deleteBtn = new JButton("Delete Hall");

        addBtn.addActionListener(e -> new AddHallForm());
        viewBtn.addActionListener(e -> new ViewHallsForm());
        editBtn.addActionListener(e -> new EditHallForm());
        deleteBtn.addActionListener(e -> new DeleteHallForm());

        add(addBtn);
        add(viewBtn);
        add(editBtn);
        add(deleteBtn);

        setVisible(true);
    }
}

class AddHallForm extends JFrame {
    public AddHallForm() {
        setTitle("Add New Hall");
        setSize(300, 250); // 
        setLayout(new GridLayout(4, 2)); 

        JTextField hallNameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField capacityField = new JTextField(); 
        JButton saveBtn = new JButton("Save");

        add(new JLabel("Hall Name:"));
        add(hallNameField);
        add(new JLabel("Price per hour (RM):"));
        add(priceField);
        add(new JLabel("Number of People:"));
        add(capacityField); 
        add(new JLabel()); 
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            String name = hallNameField.getText().trim();
            String price = priceField.getText().trim();
            String capacity = capacityField.getText().trim();

            if (name.isEmpty() || price.isEmpty() || capacity.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            try (FileWriter fw = new FileWriter("available_halls.txt", true)) {
                fw.write(name + " - RM" + price + "/hr - Capacity: " + capacity + " people\n");
                JOptionPane.showMessageDialog(this, "Hall added successfully.");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}



class ViewHallsForm extends JFrame {
    public ViewHallsForm() {
        setTitle("View All Halls");
        setSize(400, 300);
        JTextArea area = new JTextArea();

        try (Scanner scanner = new Scanner(new File("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt"))) {
            while (scanner.hasNextLine()) {
                area.append(scanner.nextLine() + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        add(new JScrollPane(area));
        setVisible(true);
    }
}

class EditHallForm extends JFrame {
    public EditHallForm() {
        setTitle("Edit Hall");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2));

        JComboBox<String> hallCombo = new JComboBox<>();
        List<String> hallLines = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File("available_halls.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                hallLines.add(line);
                hallCombo.addItem(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load halls.");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField capacityField = new JTextField();
        JButton updateBtn = new JButton("Update");

        hallCombo.addActionListener(e -> {
            String selected = (String) hallCombo.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split(" - RM|/hr - Capacity: | people");
                if (parts.length >= 3) {
                    nameField.setText(parts[0].trim());
                    priceField.setText(parts[1].trim());
                    capacityField.setText(parts[2].trim());
                }
            }
        });

        if (hallCombo.getItemCount() > 0) {
            hallCombo.setSelectedIndex(0);
        }

        add(new JLabel("Select Hall:"));
        add(hallCombo);
        add(new JLabel("New Name:"));
        add(nameField);
        add(new JLabel("New Price (RM):"));
        add(priceField);
        add(new JLabel("New Capacity:"));
        add(capacityField);
        add(updateBtn);

        updateBtn.addActionListener(e -> {
            int index = hallCombo.getSelectedIndex();
            if (index < 0) return;

            String name = nameField.getText().trim();
            String price = priceField.getText().trim();
            String capacity = capacityField.getText().trim();

            if (name.isEmpty() || price.isEmpty() || capacity.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            try {
                double priceVal = Double.parseDouble(price);
                int capacityVal = Integer.parseInt(capacity);
                if (priceVal <= 0 || capacityVal <= 0) throw new NumberFormatException();

                String newLine = name + " - RM" + priceVal + "/hr - Capacity: " + capacityVal + " people";
                hallLines.set(index, newLine);

                // Save updated list
                try (FileWriter fw = new FileWriter("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt")) {
                    for (String line : hallLines) {
                        fw.write(line + "\n");
                    }
                }

                JOptionPane.showMessageDialog(this, "Hall updated successfully.");
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and capacity.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}


class DeleteHallForm extends JFrame {
    public DeleteHallForm() {
        setTitle("Delete Hall");
        setSize(400, 200);
        setLayout(new BorderLayout());

        JComboBox<String> hallCombo = new JComboBox<>();
        List<String> halls = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                halls.add(line);
                hallCombo.addItem(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            String selected = (String) hallCombo.getSelectedItem();
            halls.remove(selected);
            try (FileWriter fw = new FileWriter("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt")) {
                for (String hall : halls) {
                    fw.write(hall + "\n");
                }
                JOptionPane.showMessageDialog(this, "Hall deleted.");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        add(hallCombo, BorderLayout.CENTER);
        add(deleteBtn, BorderLayout.SOUTH);
        setVisible(true);
    }
}

class AvailabilityForm extends JFrame {
    public AvailabilityForm() {
        setTitle("Hall Availability");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        JComboBox<String> hallCombo = new JComboBox<>();
        JTextField startDateTime = new JTextField();
        JTextField endDateTime = new JTextField();
        JTextField numberOfPeople = new JTextField();
        JTextField remarks = new JTextField();
        JButton save = new JButton("Save");

        try (Scanner fileScanner = new Scanner(new FileInputStream("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt"))) {
            while (fileScanner.hasNextLine()) {
                hallCombo.addItem(fileScanner.nextLine());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        add(new JLabel("Hall:"));
        add(hallCombo);
        add(new JLabel("Start DateTime (YYYY-MM-DDTHH:MM):"));
        add(startDateTime);
        add(new JLabel("End DateTime (YYYY-MM-DDTHH:MM):"));
        add(endDateTime);
        add(new JLabel("Remarks:"));
        add(remarks);
        add(save);

        save.addActionListener(e -> {
            try (FileWriter fw = new FileWriter("hall_availability.txt", true)) {
                fw.write(hallCombo.getSelectedItem() + "," + startDateTime.getText() + "," + endDateTime.getText() + ","  + remarks.getText() + "\n");
                JOptionPane.showMessageDialog(this, "Availability set.");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

class MaintenanceForm extends JFrame {
    public MaintenanceForm() {
        setTitle("Set Hall Maintenance");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2));

        JComboBox<String> hallCombo = new JComboBox<>();
        try (Scanner scanner = new Scanner(new File("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\available_halls.txt"))) {
            while (scanner.hasNextLine()) {
                hallCombo.addItem(scanner.nextLine());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JTextField startDateTime = new JTextField("2025-06-10T08:00");
        JTextField endDateTime = new JTextField("2025-06-10T12:00");
        JTextField numberOfPeople = new JTextField("0");
        JTextField remarks = new JTextField("Optional remarks");
        JButton save = new JButton("Save");

        add(new JLabel("Hall:"));
        add(hallCombo);
        add(new JLabel("Start DateTime (YYYY-MM-DDTHH:MM):"));
        add(startDateTime);
        add(new JLabel("End DateTime (YYYY-MM-DDTHH:MM):"));
        add(endDateTime);
        add(new JLabel("Number of People:"));
        add(numberOfPeople);
        add(new JLabel("Remarks:"));
        add(remarks);
        add(save);

        save.addActionListener(e -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime start = LocalDateTime.parse(startDateTime.getText().trim(), formatter);
                LocalDateTime end = LocalDateTime.parse(endDateTime.getText().trim(), formatter);

                if (!start.toLocalDate().equals(end.toLocalDate())) {
                    JOptionPane.showMessageDialog(this, "Start and End must be on the same date.");
                    return;
                }

                int startHour = start.getHour();
                int endHour = end.getHour();
                String date = start.toLocalDate().toString();

                String record = hallCombo.getSelectedItem() + "," + date + "," + startHour + "-" + endHour + "," + remarks.getText().trim();

                try (FileWriter fw = new FileWriter("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\hall_maintenance.txt", true)) {
                    fw.write(record + "\n");
                    JOptionPane.showMessageDialog(this, "Maintenance scheduled.");
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date-time format. Use YYYY-MM-DDTHH:MM");
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

class MaintenanceScheduleViewer extends JFrame {
    public MaintenanceScheduleViewer() {
        setTitle("Maintenance Schedule");
        setSize(500, 400);
        JTextArea scheduleArea = new JTextArea();
        scheduleArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(scheduleArea);
        add(scrollPane);

        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Riththe\\OneDrive - Lord Buddha Education Foundation\\Desktop\\NP069873-NP069876-NP069875-NP069870-CT038-3-2-OODJ\\hall_maintenance.txt"))) {
            String line;
            scheduleArea.append("Hall Name | Date | Time | Remarks\n");
            scheduleArea.append("------------------------------------------------------\n");
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4); // Expecting 4 parts now
                if (parts.length == 4) {
                    scheduleArea.append(parts[0] + " | " + parts[1] + " | " + parts[2] + " | " + parts[3] + "\n");
                }
            }
        } catch (IOException ex) {
            scheduleArea.setText("Unable to read maintenance schedule.");
        }

        setVisible(true);
    }
}



//Administrator
class AdminDashboard extends JFrame {
    public AdminDashboard() {
        setTitle("Administrator Dashboard");
        setSize(500, 400);
        setLayout(new GridLayout(5, 1));

        JButton manageSchedulersBtn = new JButton("Manage Scheduler Staff");
        manageSchedulersBtn.addActionListener(e -> new SchedulerStaffManagement());
        add(manageSchedulersBtn);

        JButton manageUsersBtn = new JButton("Manage Users");
        manageUsersBtn.addActionListener(e -> new UserManagement());
        add(manageUsersBtn);

        JButton bookingMgmtBtn = new JButton("Manage Bookings");
        bookingMgmtBtn.addActionListener(e -> new BookingManagement());
        add(bookingMgmtBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new UserLoginFrame();
        });
        add(logoutBtn);

        setVisible(true);
    }
}

class SchedulerStaffManagement extends JFrame {
    public SchedulerStaffManagement() {
        setTitle("Scheduler Staff Management");
        setSize(500, 400);
        JTextArea area = new JTextArea();

        JButton addBtn = new JButton("Add Scheduler");
        JButton viewBtn = new JButton("View Schedulers");
        JButton editBtn = new JButton("Edit Scheduler");
        JButton deleteBtn = new JButton("Delete Scheduler");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 4));
        panel.add(addBtn);
        panel.add(viewBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        addBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog("Enter new scheduler username:");
            String password = JOptionPane.showInputDialog("Enter password:");
            if (username != null && password != null) {
                try (FileWriter fw = new FileWriter("approved_users.txt", true)) {
                    fw.write(username + "," + password + ",Scheduler\n");
                    area.setText("Scheduler added.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        viewBtn.addActionListener(e -> {
            area.setText("");
            try (Scanner sc = new Scanner(new File("approved_users.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.endsWith("Scheduler")) {
                        area.append(line + "\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        editBtn.addActionListener(e -> {
            String oldUsername = JOptionPane.showInputDialog("Enter scheduler username to edit:");
            String newUsername = JOptionPane.showInputDialog("Enter new username:");
            String newPassword = JOptionPane.showInputDialog("Enter new password:");
            List<String> lines = new ArrayList<>();
            try (Scanner sc = new Scanner(new File("approved_users.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.startsWith(oldUsername + ",") && line.endsWith("Scheduler")) {
                        lines.add(newUsername + "," + newPassword + ",Scheduler");
                    } else {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try (FileWriter fw = new FileWriter("approved_users.txt")) {
                for (String l : lines) fw.write(l + "\n");
                area.setText("Scheduler updated.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog("Enter scheduler username to delete:");
            List<String> lines = new ArrayList<>();
            try (Scanner sc = new Scanner(new File("approved_users.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!(line.startsWith(username + ",") && line.endsWith("Scheduler"))) {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try (FileWriter fw = new FileWriter("approved_users.txt")) {
                for (String l : lines) fw.write(l + "\n");
                area.setText("Scheduler deleted.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

class UserManagement extends JFrame {
    public UserManagement() {
        setTitle("User Management");
        setSize(500, 400);
        JTextArea area = new JTextArea();

        JButton viewBtn = new JButton("View Users");
        JButton deleteBtn = new JButton("Delete User");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(viewBtn);
        panel.add(deleteBtn);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        viewBtn.addActionListener(e -> {
            area.setText("");
            try (Scanner sc = new Scanner(new File("approved_users.txt"))) {
                while (sc.hasNextLine()) {
                    area.append(sc.nextLine() + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog("Enter username to delete:");
            List<String> lines = new ArrayList<>();
            try (Scanner sc = new Scanner(new File("approved_users.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.startsWith(username + ",")) {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try (FileWriter fw = new FileWriter("approved_users.txt")) {
                for (String l : lines) fw.write(l + "\n");
                area.setText("User deleted.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

class BookingManagement extends JFrame {
    public BookingManagement() {
        setTitle("Booking Management");
        setSize(500, 400);
        JTextArea area = new JTextArea();
        JButton viewUpcomingBtn = new JButton("View Upcoming");
        JButton viewPastBtn = new JButton("View Past");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(viewUpcomingBtn);
        panel.add(viewPastBtn);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        viewUpcomingBtn.addActionListener(e -> {
            area.setText("");
            try (Scanner sc = new Scanner(new File("bookings.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        LocalDate date = LocalDate.parse(parts[2]);
                        if (!date.isBefore(LocalDate.now())) {
                            area.append(line + "\n");
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        viewPastBtn.addActionListener(e -> {
            area.setText("");
            try (Scanner sc = new Scanner(new File("bookings.txt"))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        LocalDate date = LocalDate.parse(parts[2]);
                        if (date.isBefore(LocalDate.now())) {
                            area.append(line + "\n");
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}

// Manager Dashboard
class ManagerDashboard extends JFrame {
    public ManagerDashboard() {
        setTitle("Manager Dashboard");
        setSize(400, 300);
        setLayout(new GridLayout(3, 1));

        JButton salesDashboardBtn = new JButton("Sales Dashboard");
        salesDashboardBtn.addActionListener(e -> new SalesDashboard());
        add(salesDashboardBtn);

        JButton maintenanceOpsBtn = new JButton("Maintenance Operations");
        maintenanceOpsBtn.addActionListener(e -> new MaintenanceOperations());
        add(maintenanceOpsBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new UserLoginFrame();
        });
        add(logoutBtn);

        setVisible(true);
    }
}

class SalesDashboard extends JFrame {
    private JTextArea salesArea;

    public SalesDashboard() {
        setTitle("Sales Dashboard");
        setSize(500, 400);
        setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel();
        JButton weeklyBtn = new JButton("Weekly Sales");
        JButton monthlyBtn = new JButton("Monthly Sales");
        JButton yearlyBtn = new JButton("Yearly Sales");

        filterPanel.add(weeklyBtn);
        filterPanel.add(monthlyBtn);
        filterPanel.add(yearlyBtn);

        add(filterPanel, BorderLayout.NORTH);

        salesArea = new JTextArea();
        add(new JScrollPane(salesArea), BorderLayout.CENTER);

        weeklyBtn.addActionListener(e -> displaySales("weekly"));
        monthlyBtn.addActionListener(e -> displaySales("monthly"));
        yearlyBtn.addActionListener(e -> displaySales("yearly"));

        setVisible(true);
    }

    private void displaySales(String period) {
        salesArea.setText("");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "weekly": startDate = today.minusWeeks(1); break;
            case "monthly": startDate = today.minusMonths(1); break;
            case "yearly": startDate = today.minusYears(1); break;
            default: startDate = today; break;
        }

        try (Scanner scanner = new Scanner(new File("bookings.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    LocalDate bookingDate = LocalDate.parse(parts[2], formatter);
                    if (!bookingDate.isBefore(startDate)) {
                        salesArea.append(line + "\n");
                    }
                }
            }
        } catch (IOException ex) {
            salesArea.setText("Error reading bookings.txt");
        }
    }
}
class MaintenanceOperations extends JFrame {
    private JTextArea issuesArea;
    private List<String> issuesList;

    public MaintenanceOperations() {
        setTitle("Maintenance Operations");
        setSize(600, 400);
        setLayout(new BorderLayout());

        issuesArea = new JTextArea();
        add(new JScrollPane(issuesArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton viewIssuesBtn = new JButton("View Issues");
        JButton assignStaffBtn = new JButton("Assign Scheduler");
        JButton updateStatusBtn = new JButton("Update Status");

        controlPanel.add(viewIssuesBtn);
        controlPanel.add(assignStaffBtn);
        controlPanel.add(updateStatusBtn);

        add(controlPanel, BorderLayout.SOUTH);

        viewIssuesBtn.addActionListener(e -> loadIssues());
        assignStaffBtn.addActionListener(e -> assignScheduler());
        updateStatusBtn.addActionListener(e -> updateIssueStatus());

        setVisible(true);
    }

    private void loadIssues() {
        issuesArea.setText("");
        issuesList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("issues.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                issuesList.add(line);
                issuesArea.append(line + "\n");
            }
        } catch (IOException ex) {
            issuesArea.setText("Error reading issues.txt");
        }
    }

    private void assignScheduler() {
        String issueId = JOptionPane.showInputDialog("Enter Issue ID to assign:");
        String schedulerName = JOptionPane.showInputDialog("Enter Scheduler Name:");
        updateIssue(issueId, "Assigned to: " + schedulerName);
    }

    private void updateIssueStatus() {
        String issueId = JOptionPane.showInputDialog("Enter Issue ID to update status:");
        String[] statuses = {"In Progress", "Done", "Closed", "Cancelled"};
        String status = (String) JOptionPane.showInputDialog(null, "Select Status:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        if (status != null) {
            updateIssue(issueId, "Status: " + status);
        }
    }

    private void updateIssue(String issueId, String updateInfo) {
        List<String> updatedIssues = new ArrayList<>();
        boolean found = false;
        for (String issue : issuesList) {
            if (issue.startsWith(issueId + ",")) {
                issue += ", " + updateInfo;
                found = true;
            }
            updatedIssues.add(issue);
        }
        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("issues.txt"))) {
                for (String updatedIssue : updatedIssues) {
                    writer.write(updatedIssue);
                    writer.newLine();
                }
                loadIssues();
            } catch (IOException ex) {
                issuesArea.setText("Error updating issues.txt");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Issue ID not found.");
        }
    }
}