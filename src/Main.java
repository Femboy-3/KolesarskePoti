import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Call the createAndShowGUI method to display the login UI
            SwingUtilities.invokeLater(() -> new Login());
        } catch (Exception e) {
            throw new RuntimeException("Error starting the application", e);
        }
    }
}
