import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WatchDog {
    private static final String LOG_FILE_NAME = "java_activity_log.txt";

    public static void main(String[] args) {
        // Specify the directory to monitor
        Path directoryToMonitor = Paths.get("src"); // Change this to the directory you want to monitor
        monitorDirectory(directoryToMonitor);
    }

    private static void monitorDirectory(Path path) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the directory with the watch service
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            System.out.println("Monitoring directory: " + path.toAbsolutePath());

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take(); // Wait for a watch key to be available
                } catch (InterruptedException e) {
                    System.out.println("Directory monitoring interrupted.");
                    return;
                }

                List<WatchEvent<?>> events = key.pollEvents();
                for (WatchEvent<?> event : events) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Get the filename from the event context
                    Path filePath = ((WatchEvent<Path>) event).context();
                    String fileName = filePath.getFileName().toString();

                    // Filter for .java files only
                    if (fileName.endsWith(".java")) {
                        logActivity(kind.name(), fileName);
                    }
                }

                // Reset the key to continue watching for events
                boolean valid = key.reset();
                if (!valid) {
                    break; // Exit the loop if the key is no longer valid
                }
            }
        } catch (IOException e) {
            System.err.println("Error monitoring directory: " + e.getMessage());
        }
    }

    private static void logActivity(String action, String fileName) {
        // Format the log message
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("Timestamp: %s, Action: %s, File: %s", timestamp, action, fileName);

        System.out.println(logMessage); // Print to console (optional)
        appendToLogFile(logMessage);
    }

    private static void appendToLogFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_NAME, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
