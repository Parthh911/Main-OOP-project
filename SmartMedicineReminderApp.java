import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class SmartMedicineReminderApp {
    
        static class Medicine implements Serializable {
            private static final long serialVersionUID = 1L;

            private String name;
            private String dosage;
            private String strength;
            private String timing;
            private boolean taken;
            private String status;
            private String date;

        public Medicine(String name, String dosage, String strength, String timing, String date) {
            this.name = name;
            this.dosage = dosage;
            this.strength = strength;
            this.timing = timing;
            this.date = date;
            this.taken = false;
            this.status = "Pending";
        }

        public String getName() {
            return name;
        }

        public String getDosage() {
            return dosage;
        }

        public String getStrength() {
            return strength;
        }

        public String getTiming() {
            return timing;
        }

        public boolean isTaken() {
            return taken;
        }

        public void setTaken(boolean taken) {
            this.taken = taken;
            this.status = taken ? "Taken" : "Pending";
        }

        public String getStatus() {
            return status;
        }

        public void setMissed() {
            this.taken = false;
            this.status = taken ? "Pending" : "Missed";
        }

        public String getDate() {
            return date;
        }

        public String getDetails() {
            return (name != null ? name : "No Name") + " - "
                    + (dosage != null ? dosage : "No Dosage") + " - "
                    + (strength != null ? strength : "No Strength") + " at "
                    + (timing != null ? timing : "No Timing") + " on "
                    + (date != null ? date : "No Date") + " (" + status + ")";
        }

        
        public String toFileString() {
            return name + ";" + dosage + ";" + strength + ";" + timing + ";" + date + ";" + taken + ";" + status;
        }

        
        public static Medicine fromFileString(String fileString) {
            String[] parts = fileString.split(";", -1);
            if (parts.length < 7) return null;
            Medicine medicine = new Medicine(
                    parts[0].isEmpty() ? null : parts[0],
                    parts[1].isEmpty() ? null : parts[1],
                    parts[2].isEmpty() ? null : parts[2],
                    parts[3].isEmpty() ? null : parts[3],
                    parts[4].isEmpty() ? null : parts[4]
            );
            medicine.taken = Boolean.parseBoolean(parts[5]);
            medicine.status = parts[6];
            return medicine;
        }
    }

    
    static class HealthRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        private String date;
        private String bloodPressure;
        private String sugarLevel;
        private String oxygenLevel;

        public HealthRecord(String date, String bloodPressure, String sugarLevel, String oxygenLevel) {
            this.date = date;
            this.bloodPressure = bloodPressure;
            this.sugarLevel = sugarLevel;
            this.oxygenLevel = oxygenLevel;
        }

        public String getDate() {
            return date;
        }

        public String getDetails() {
            return "Date: " + (date != null ? date : "No Date") +
                    ", Blood Pressure: " + (bloodPressure != null ? bloodPressure : "No Data") +
                    ", Sugar Level: " + (sugarLevel != null ? sugarLevel : "No Data") +
                    ", Oxygen Level: " + (oxygenLevel != null ? oxygenLevel : "No Data");
        }

        
        public String toFileString() {
            return date + ";" + bloodPressure + ";" + sugarLevel + ";" + oxygenLevel;
        }

        
        public static HealthRecord fromFileString(String fileString) {
            String[] parts = fileString.split(";", -1);
            if (parts.length < 4) return null;
            return new HealthRecord(
                    parts[0].isEmpty() ? null : parts[0],
                    parts[1].isEmpty() ? null : parts[1],
                    parts[2].isEmpty() ? null : parts[2],
                    parts[3].isEmpty() ? null : parts[3]
            );
        }
    }

    
    static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;
        private List<Medicine> medicines;
        private List<HealthRecord> healthRecords;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
            this.medicines = new ArrayList<>();
            this.healthRecords = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void addMedicine(Medicine medicine) {
            medicines.add(medicine);
        }

        public void removeMedicine(Medicine medicine) {
            medicines.remove(medicine);
        }

        public List<Medicine> getMedicines() {
            return medicines;
        }

        public void addHealthRecord(HealthRecord record) {
            healthRecords.add(record);
        }

        public List<HealthRecord> getHealthRecords() {
            return healthRecords;
        }

        
        public String toFileString() {
            StringBuilder sb = new StringBuilder();
            sb.append("User:").append(name).append(";").append(age).append("\n");
            sb.append("Medicines:\n");
            for (Medicine med : medicines) {
                sb.append(med.toFileString()).append("\n");
            }
                sb.append("HealthRecords:\n");
                for (HealthRecord record : healthRecords) {
                    sb.append(record.toFileString()).append("\n");
            }
            sb.append("EndUser\n");
            return sb.toString();
        }

        
        public static User fromFileString(BufferedReader reader) throws IOException {
            String line;
            String[] userInfo = null;

            
            line = reader.readLine();
            if (line == null || line.isEmpty()) return null;

            if (line.startsWith("User:")) {
                userInfo = line.substring(5).split(";", -1);
            } else {
                return null;
            }

            if (userInfo.length < 2) return null;
            User user = new User(userInfo[0], Integer.parseInt(userInfo[1]));

            while ((line = reader.readLine()) != null) {
                if (line.equals("Medicines:")) {
                    while ((line = reader.readLine()) != null && !line.equals("HealthRecords:") && !line.equals("EndUser")) {
                        Medicine med = Medicine.fromFileString(line);
                        if (med != null) user.addMedicine(med);
                    }
                    if (line == null || line.equals("EndUser")) break;
                }
                if (line.equals("HealthRecords:")) {
                    while ((line = reader.readLine()) != null && !line.equals("EndUser")) {
                        HealthRecord record = HealthRecord.fromFileString(line);
                        if (record != null) user.addHealthRecord(record);
                    }
                    if (line == null || line.equals("EndUser")) break;
                }
                if (line.equals("EndUser")) break;
            }
            return user;
        }
    }

    
    private JFrame frame;
    private List<User> users;
    private User currentUser;
    private boolean isDarkTheme;
    private Timer reminderTimer;
    private JLabel clockLabel;
    private JLabel userLabel; 

    
    private static final String DATA_FILE = "users_data.txt";

    public SmartMedicineReminderApp() {
        users = new ArrayList<>();
        loadUserData();
        isDarkTheme = false;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Smart Medicine Reminder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        
        JTabbedPane tabbedPane = new JTabbedPane();

        
        clockLabel = new JLabel();
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Arial", Font.BOLD, 16));
        updateClock();
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        
        userLabel = new JLabel("No User Selected");
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));

        
        JButton themeToggleButton = new JButton("Toggle Theme");
        themeToggleButton.addActionListener(e -> toggleTheme());

        
        JPanel userPanel = new JPanel(new BorderLayout());
        JPanel userControlPanel = new JPanel();
        JButton addUserButton = new JButton("Add User");
        JButton selectUserButton = new JButton("Select User");

        addUserButton.addActionListener(e -> addUser());
        selectUserButton.addActionListener(e -> selectUser());

        userControlPanel.add(addUserButton);
        userControlPanel.add(selectUserButton);
        userPanel.add(userControlPanel, BorderLayout.NORTH);

        
        JPanel medicinePanel = new JPanel(new BorderLayout());
        JPanel medicineControlPanel = new JPanel();
        JButton addMedicineButton = new JButton("Add Medicine");
        JButton viewMedicinesButton = new JButton("View Medicines");
        JButton removeMedicineButton = new JButton("Remove Medicine");
        JButton printMedicinesButton = new JButton("Print Medicines");

        addMedicineButton.addActionListener(e -> addMedicine());
        viewMedicinesButton.addActionListener(e -> viewMedicines());
        removeMedicineButton.addActionListener(e -> removeMedicine());
        printMedicinesButton.addActionListener(e -> printMedicines()); 

        medicineControlPanel.add(addMedicineButton);
        medicineControlPanel.add(viewMedicinesButton);
        medicineControlPanel.add(removeMedicineButton);
        medicineControlPanel.add(printMedicinesButton); 
        medicinePanel.add(medicineControlPanel, BorderLayout.NORTH);

        
        JPanel healthPanel = new JPanel(new BorderLayout());
        JPanel healthControlPanel = new JPanel();
        JButton addHealthRecordButton = new JButton("Add Health Record");
        JButton viewHealthRecordsButton = new JButton("View Health Records");
        JButton printHealthRecordsButton = new JButton("Print Health Records"); 

        addHealthRecordButton.addActionListener(e -> addHealthRecord());
        viewHealthRecordsButton.addActionListener(e -> viewHealthRecords());
        printHealthRecordsButton.addActionListener(e -> printHealthRecords()); 

        healthControlPanel.add(addHealthRecordButton);
        healthControlPanel.add(viewHealthRecordsButton);
        healthControlPanel.add(printHealthRecordsButton); 
        healthPanel.add(healthControlPanel, BorderLayout.NORTH);

        
        JPanel calendarPanel = new JPanel(new BorderLayout());
        JButton viewCalendarButton = new JButton("View Calendar");

        viewCalendarButton.addActionListener(e -> viewCalendar());

        calendarPanel.add(viewCalendarButton, BorderLayout.NORTH);

        
        tabbedPane.addTab("Users", userPanel);
        tabbedPane.addTab("Medicines", medicinePanel);
        tabbedPane.addTab("Health Records", healthPanel);
        tabbedPane.addTab("Calendar", calendarPanel);

        
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search Medicines");

        searchButton.addActionListener(e -> searchMedicines(searchField.getText()));

        searchPanel.add(new JLabel("Search Medicines: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.add(userLabel); 
        topRightPanel.add(themeToggleButton);
        topRightPanel.add(clockLabel);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        
        startReminderTimer();

        frame.setVisible(true);
    }

    private void updateClock() {
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        clockLabel.setText("  " + currentTime + "  ");
    }

    private void startReminderTimer() {
        reminderTimer = new Timer(20000, e -> checkReminders());
        reminderTimer.start();
    }

    private void checkReminders() {
        if (currentUser == null) return;

        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        for (Medicine medicine : currentUser.getMedicines()) {
            if (medicine.getDate() != null && medicine.getTiming() != null) {
                if (medicine.getDate().equals(currentDate) && medicine.getTiming().equals(currentTime)) {
                    if (!medicine.isTaken()) {
                        JOptionPane.showMessageDialog(frame, "Time to take your medicine: " + medicine.getName());
                    }
                }
                
                if (medicine.getDate().equals(currentDate) && medicine.getTiming().compareTo(currentTime) < 0 && !medicine.isTaken()) {
                    medicine.setMissed();
                }
            }
        }
    }

    private void addUser() {
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        String[] options = {"Add User", "Cancel"};

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Name: "));
        panel.add(nameField);
        panel.add(new JLabel("Age: "));
        panel.add(ageField);

        int result = JOptionPane.showOptionDialog(frame, panel, "Add User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            int age = 0;
            try {
                if (!ageField.getText().trim().isEmpty()) {
                    age = Integer.parseInt(ageField.getText().trim());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid age!");
                return;
            }
            User user = new User(name, age);
            users.add(user);
            currentUser = user;
            userLabel.setText("User: " + currentUser.getName());
            JOptionPane.showMessageDialog(frame, "User added and selected!");
            saveUserData(); 
        }
    }

    private void selectUser() {
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No users available! Please add a user first.");
            return;
        }

        String[] userNames = users.stream().map(User::getName).toArray(String[]::new);
        String selectedName = (String) JOptionPane.showInputDialog(
                frame,
                "Select a user:",
                "User Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                userNames,
                userNames[0]
        );

        if (selectedName != null) {
            currentUser = users.stream()
                    .filter(u -> u.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);
            userLabel.setText("User: " + currentUser.getName());
            JOptionPane.showMessageDialog(frame, "User " + currentUser.getName() + " selected!");
        }
    }

    private void addMedicine() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField dosageField = new JTextField();
        JTextField strengthField = new JTextField();
        JTextField timingField = new JTextField();
        JTextField dateField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        String[] options = {"Add Medicine", "Cancel"};

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Medicine Name: "));
        panel.add(nameField);
        panel.add(new JLabel("Dosage: "));
        panel.add(dosageField);
        panel.add(new JLabel("Strength (e.g., 5ml, 50mg): "));
        panel.add(strengthField);
        panel.add(new JLabel("Timing (HH:mm): "));
        panel.add(timingField);
        panel.add(new JLabel("Date (dd/MM/yyyy): "));
        panel.add(dateField);

        int result = JOptionPane.showOptionDialog(frame, panel, "Add Medicine",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == JOptionPane.OK_OPTION) {
            String medName = nameField.getText().trim();
            String dosage = dosageField.getText().trim();
            String strength = strengthField.getText().trim();
            String timing = timingField.getText().trim();
            String date = dateField.getText().trim();

            
            Medicine medicine = new Medicine(
                    medName.isEmpty() ? null : medName,
                    dosage.isEmpty() ? null : dosage,
                    strength.isEmpty() ? null : strength,
                    timing.isEmpty() ? null : timing,
                    date.isEmpty() ? null : date
            );
            currentUser.addMedicine(medicine);
            JOptionPane.showMessageDialog(frame, "Medicine added for " + currentUser.getName() + "!");
            saveUserData(); 
        }
    }

    private void viewMedicines() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<Medicine> medicines = currentUser.getMedicines();
        if (medicines.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No medicines found for " + currentUser.getName());
            return;
        }

        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Medicine med : medicines) {
            listModel.addElement(med.getDetails());
        }

        JList<String> medicineList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(medicineList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton markTakenButton = new JButton("Mark as Taken");
        markTakenButton.addActionListener(e -> {
            int index = medicineList.getSelectedIndex();
            if (index != -1) {
                medicines.get(index).setTaken(true);
                listModel.setElementAt(medicines.get(index).getDetails(), index);
                saveUserData(); 
            }
        });

        panel.add(markTakenButton, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(frame, panel, "Medicines for " + currentUser.getName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void removeMedicine() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<Medicine> medicines = currentUser.getMedicines();
        if (medicines.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No medicines to remove for " + currentUser.getName());
            return;
        }

        String[] medicineNames = medicines.stream()
                .map(Medicine::getDetails)
                .toArray(String[]::new);

        String selectedMedicine = (String) JOptionPane.showInputDialog(
                frame,
                "Select a medicine to remove:",
                "Remove Medicine",
                JOptionPane.PLAIN_MESSAGE,
                null,
                medicineNames,
                medicineNames[0]
        );

        if (selectedMedicine != null) {
            Medicine medicineToRemove = medicines.stream()
                    .filter(med -> med.getDetails().equals(selectedMedicine))
                    .findFirst()
                    .orElse(null);

            if (medicineToRemove != null) {
                currentUser.removeMedicine(medicineToRemove);
                JOptionPane.showMessageDialog(frame, "Medicine removed.");
                saveUserData(); 
            }
        }
    }

    private void viewCalendar() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        Map<String, List<Medicine>> dateMedicineMap = new HashMap<>();
        for (Medicine medicine : currentUser.getMedicines()) {
            String date = medicine.getDate() != null ? medicine.getDate() : "No Date";
            dateMedicineMap.computeIfAbsent(date, k -> new ArrayList<>()).add(medicine);
        }

        String[] dates = dateMedicineMap.keySet().toArray(new String[0]);
        Arrays.sort(dates);

        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String date : dates) {
            listModel.addElement("Date: " + date);
            for (Medicine med : dateMedicineMap.get(date)) {
                listModel.addElement("    " + med.getDetails());
            }
        }

        JList<String> calendarList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(calendarList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(frame, panel, "Medicine Calendar for " + currentUser.getName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void addHealthRecord() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        JTextField dateField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        JTextField bpField = new JTextField();
        JTextField sugarField = new JTextField();
        JTextField oxygenField = new JTextField();
        String[] options = {"Add Record", "Cancel"};

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Date (dd/MM/yyyy): "));
        panel.add(dateField);
        panel.add(new JLabel("Blood Pressure: "));
        panel.add(bpField);
        panel.add(new JLabel("Sugar Level: "));
        panel.add(sugarField);
        panel.add(new JLabel("Oxygen Level: "));
        panel.add(oxygenField);

        int result = JOptionPane.showOptionDialog(frame, panel, "Add Health Record",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == JOptionPane.OK_OPTION) {
            String date = dateField.getText().trim();
            String bp = bpField.getText().trim();
            String sugar = sugarField.getText().trim();
            String oxygen = oxygenField.getText().trim();

            
            HealthRecord record = new HealthRecord(
                    date.isEmpty() ? null : date,
                    bp.isEmpty() ? null : bp,
                    sugar.isEmpty() ? null : sugar,
                    oxygen.isEmpty() ? null : oxygen
            );
            currentUser.addHealthRecord(record);
            JOptionPane.showMessageDialog(frame, "Health record added for " + currentUser.getName() + "!");
            saveUserData(); 
        }
    }

    private void viewHealthRecords() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<HealthRecord> records = currentUser.getHealthRecords();
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No health records found for " + currentUser.getName());
            return;
        }

        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (HealthRecord record : records) {
            listModel.addElement(record.getDetails());
        }

        JList<String> recordList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(recordList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(frame, panel, "Health Records for " + currentUser.getName(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void printMedicines() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<Medicine> medicines = currentUser.getMedicines();
        if (medicines.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No medicines to print for " + currentUser.getName());
            return;
        }

        try (PrintWriter writer = new PrintWriter(currentUser.getName() + "_medicines.txt")) {
            for (Medicine med : medicines) {
                writer.println(med.getDetails());
            }
            JOptionPane.showMessageDialog(frame, "Medicines printed to " + currentUser.getName() + "_medicines.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error printing medicines: " + e.getMessage());
        }
    }

    private void printHealthRecords() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<HealthRecord> records = currentUser.getHealthRecords();
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No health records to print for " + currentUser.getName());
            return;
        }

        try (PrintWriter writer = new PrintWriter(currentUser.getName() + "_health_records.txt")) {
            for (HealthRecord record : records) {
                writer.println(record.getDetails());
            }
            JOptionPane.showMessageDialog(frame, "Health records printed to " + currentUser.getName() + "_health_records.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error printing health records: " + e.getMessage());
        }
    }

    private void searchMedicines(String query) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user selected! Please select a user first.");
            return;
        }

        List<Medicine> medicines = currentUser.getMedicines();
        if (medicines.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No medicines found for " + currentUser.getName());
            return;
        }

        StringBuilder results = new StringBuilder("Search Results:\n");
        for (Medicine medicine : medicines) {
            if (medicine.getName() != null && medicine.getName().toLowerCase().contains(query.toLowerCase())) {
                results.append(medicine.getDetails()).append("\n");
            }
        }

        if (results.toString().equals("Search Results:\n")) {
            results.append("No matches found.");
        }

        JOptionPane.showMessageDialog(frame, results.toString());
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        try {
            if (isDarkTheme) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                UIManager.put("control", new Color(50, 50, 50));
                UIManager.put("info", new Color(50, 50, 50));
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusGreen", new Color(176, 179, 50));
                UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                UIManager.put("nimbusLightBackground", new Color(50, 50, 50));
                UIManager.put("nimbusOrange", new Color(191, 98, 4));
                UIManager.put("nimbusRed", new Color(169, 46, 34));
                UIManager.put("nimbusSelectedText", Color.WHITE);
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("text", Color.WHITE);
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                UIManager.put("control", null);
                UIManager.put("info", null);
                UIManager.put("nimbusBase", null);
                UIManager.put("nimbusAlertYellow", null);
                UIManager.put("nimbusDisabledText", null);
                UIManager.put("nimbusFocus", null);
                UIManager.put("nimbusGreen", null);
                UIManager.put("nimbusInfoBlue", null);
                UIManager.put("nimbusLightBackground", null);
                UIManager.put("nimbusOrange", null);
                UIManager.put("nimbusRed", null);
                UIManager.put("nimbusSelectedText", null);
                UIManager.put("nimbusSelectionBackground", null);
                UIManager.put("text", null);
            }
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error changing theme: " + e.getMessage());
        }
    }

    private void saveUserData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (User user : users) {
                writer.print(user.toFileString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving user data: " + e.getMessage());
        }
    }

    private void loadUserData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && line.startsWith("User:")) {     
                    
                    
                    List<String> userDataLines = new ArrayList<>();
                    userDataLines.add(line); 
                    while ((line = reader.readLine()) != null && !line.equals("EndUser")) {
                        userDataLines.add(line);
                    }
                    userDataLines.add("EndUser"); 
                    
                    String userData = String.join("\n", userDataLines);
                    BufferedReader userReader = new BufferedReader(new StringReader(userData));
                    User user = User.fromFileString(userReader);
                    if (user != null) users.add(user);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading user data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartMedicineReminderApp::new);
    }
}
