package se.mau.localzero.auth.observer;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FileLoggerObserver implements SessionObserver {
    @Override
    public void update(SessionEvent event) {
        String message = "";

        if (event.getEventType() == EventType.LOGIN) {
            message = "User has been logged in successfully: " + event.getUsername();
        } else if (event.getEventType() == EventType.LOGOUT) {
            message = "user has been logged out successfully: " + event.getUsername();
        }

        try (FileWriter fileWriter = new FileWriter("src/main/java/se/mau/localzero/auth/observer/SessionLogger.txt", true)) {
            fileWriter.write(message + " - Date: " + LocalDate.now() + " Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}