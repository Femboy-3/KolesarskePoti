import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class RouteDetailsPage {
    private JFrame frame;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public RouteDetailsPage(int routeId) {
        frame = new JFrame("Route Details - ID: " + routeId);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel("Details for Route ID: " + routeId);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        frame.add(label, BorderLayout.NORTH);

        JList<String> commentList = new JList<>(getComments(routeId));
        JScrollPane commentScrollPane = new JScrollPane(commentList);
        frame.add(commentScrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private String[] getComments(int routeId) {
        ArrayList<String> comments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT comment_text FROM get_route_comments(?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, routeId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    comments.add(rs.getString("comment_text"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return comments.toArray(new String[0]);
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new RouteDetailsPage(5));
    }
}
