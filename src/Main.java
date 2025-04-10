import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import util.DatabaseConnection;
import view.MainFrame;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database
                try {
                    LOGGER.info("Initializing database connection...");
                    DatabaseConnection.initializeDatabase();
                    LOGGER.info("Database initialized successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not connect to database: " + e.getMessage(), e);
                    System.out.println("Could not connect to database: " + e.getMessage());
                    System.out.println("Switching to in-memory storage.");
                    DatabaseConnection.setUseInMemoryStorage(true);
                    LOGGER.info("Switched to in-memory storage");
                }
                
                // Apply platform-specific enhancements
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and show main frame
                LOGGER.info("Starting application UI");
                new MainFrame();
                LOGGER.info("Application started successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error starting application", e);
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting application: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

