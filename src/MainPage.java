import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainPage {

    private JFrame frame;
    private JTable routesTable;
    private DefaultTableModel model;

    // Database connection information
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public MainPage() {
        frame = new JFrame("Routes Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        String[] columnNames = {"ID", "Name", "Length", "Difficulty", "Duration", "Description", "Number of POIs", "Start Location ID", "End Location ID", "Date Created"};

        model = new DefaultTableModel(columnNames, 0);

        // Initialize JTable with the model
        routesTable = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(routesTable);
        frame.add(scrollPane);
        JPanel bottomPanel = new JPanel();
        JButton addRouteButton = new JButton("Add Route");

        addRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddRoutePage();
            }
        });

        bottomPanel.add(addRouteButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Add mouse listener for row clicks
        routesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    int selectedRow = routesTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int routeId = (int) model.getValueAt(selectedRow, 0);
                        new RouteDetailsPage(routeId);
                    }
                }
            }
        });

        loadRoutesData();
    }

    public void loadRoutesData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT * FROM get_all_routes()";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();

                model.setRowCount(0);

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    float length = rs.getFloat("length");
                    int difficulty = rs.getInt("difficulty");
                    float duration = rs.getFloat("duration");
                    String description = rs.getString("description");
                    int numOfPoi = rs.getInt("num_of_poi");
                    String startLocationId = rs.getString("start_location_name");
                    String  endLocationId = rs.getString("end_location_name");
                    Timestamp dateCreated = rs.getTimestamp("date_created");

                    model.addRow(new Object[]{id, name, length, difficulty, duration, description, numOfPoi, startLocationId, endLocationId, dateCreated});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainPage());
    }
}
