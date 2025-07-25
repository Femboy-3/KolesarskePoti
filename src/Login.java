import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    private JTextField emailField;
    private JPasswordField passwordField;

    public Login() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> openRegisterForm());

        setVisible(true);
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (login(email, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            dispose(); // Close login window
            new MainPage(); // Open main page
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password.");
        }
    }

    private void openRegisterForm() {
        dispose();
        new Register();
    }

    public static boolean login(String email, String password) {
        boolean isLoggedIn = false;

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                String query = "{? = call login_user(?, ?)}";
                try (CallableStatement stmt = conn.prepareCall(query)) {
                    stmt.registerOutParameter(1, Types.BOOLEAN);
                    stmt.setString(2, email);
                    stmt.setString(3, password);
                    stmt.execute();
                    isLoggedIn = stmt.getBoolean(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isLoggedIn;
    }
}
