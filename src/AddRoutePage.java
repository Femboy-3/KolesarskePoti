import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddRoutePage {

    private JFrame frame;
    private JTextField nameField, lengthField, difficultyField, durationField, descriptionField;
    private JComboBox<String> startCityComboBox, endCityComboBox;
    private JList<String> poiList;  // Correct class for the list
    private DefaultListModel<String> poiListModel;

    // Database connection information
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public AddRoutePage() {
        frame = new JFrame("Add Route");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        // Name field
        nameField = new JTextField(20);
        frame.add(new JLabel("Route Name:"));
        frame.add(nameField);

        // Length field
        lengthField = new JTextField(20);
        frame.add(new JLabel("Length:"));
        frame.add(lengthField);

        // Difficulty field
        difficultyField = new JTextField(20);
        frame.add(new JLabel("Difficulty:"));
        frame.add(difficultyField);

        // Duration field
        durationField = new JTextField(20);
        frame.add(new JLabel("Duration:"));
        frame.add(durationField);

        // Description field
        descriptionField = new JTextField(20);
        frame.add(new JLabel("Description:"));
        frame.add(descriptionField);

        // Start City dropdown
        startCityComboBox = new JComboBox<>();
        populateCityComboBox(startCityComboBox);
        frame.add(new JLabel("Start City:"));
        frame.add(startCityComboBox);

        // End City dropdown
        endCityComboBox = new JComboBox<>();
        populateCityComboBox(endCityComboBox);
        frame.add(new JLabel("End City:"));
        frame.add(endCityComboBox);

        // POI list
        poiListModel = new DefaultListModel<>();
        poiList = new JList<>(poiListModel);
        poiList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane poiScrollPane = new JScrollPane(poiList);
        frame.add(new JLabel("Select POIs:"));
        frame.add(poiScrollPane);

        JButton addRouteButton = new JButton("Add Route");
        addRouteButton.addActionListener(e -> addRoute());
        frame.add(addRouteButton);

        JButton backButton = new JButton("Back to Main Page");
        backButton.addActionListener(e -> goBackToMainPage());
        frame.add(backButton);

        frame.setVisible(true);
    }

    private void populateCityComboBox(JComboBox<String> comboBox) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT name FROM citys";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String cityName = rs.getString("name");
                    comboBox.addItem(cityName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addRoute() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT insert_route(?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, nameField.getText());
                stmt.setFloat(2, Float.parseFloat(lengthField.getText()));
                stmt.setInt(3, Integer.parseInt(difficultyField.getText()));
                stmt.setFloat(4, Float.parseFloat(durationField.getText()));
                stmt.setString(5, descriptionField.getText());
                stmt.setString(6, (String) startCityComboBox.getSelectedItem());
                stmt.setString(7, (String) endCityComboBox.getSelectedItem());

                List<String> selectedPOIs = poiList.getSelectedValuesList();
                Integer[] poiIds = selectedPOIs.stream().map(Integer::parseInt).toArray(Integer[]::new);
                Array poiArray = conn.createArrayOf("INTEGER", poiIds);
                stmt.setArray(8, poiArray);


                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int routeId = rs.getInt(1);
                    System.out.println("Inserted Route ID: " + routeId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void goBackToMainPage() {
        frame.dispose();
        new MainPage();
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(AddRoutePage::new);
    }
}
