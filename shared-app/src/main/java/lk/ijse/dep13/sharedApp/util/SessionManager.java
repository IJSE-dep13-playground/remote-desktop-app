package lk.ijse.dep13.sharedApp.util;

import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static final Set<String> sessionIDs = new HashSet<>();

    // Server Invoke this method
    public static String generateSessionID() {
        String sessionID;
        do{
            sessionID = String.format("%06d", (int)(Math.random()*1000000));
        } while(sessionIDs.contains(sessionID));
        sessionIDs.add(sessionID);
        return sessionID;
    }

    public static Alert createSessionIDAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session ID");
        alert.setHeaderText(null);
        alert.setContentText(sessionIDs.toString());
        return alert;
    }

    // Client Invoke this method
    public static boolean validateSessionID(String sessionID) {
       return sessionIDs.contains(sessionID);
    }

    // Server Invoke this method after validation completed
    public static void removeSessionID(String sessionID) {
        sessionIDs.remove(sessionID);
    }

    public static void main(String[] args) {
        String sessionID = generateSessionID();
        System.out.println("Session ID: " + sessionID);
    }
}
