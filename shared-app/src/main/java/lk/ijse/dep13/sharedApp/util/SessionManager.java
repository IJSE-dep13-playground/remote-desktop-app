package lk.ijse.dep13.sharedApp.util;

import javafx.scene.control.Alert;

import java.util.ArrayList;

public class SessionManager {

    public static final ArrayList<String> sessionIDs = new ArrayList<>();
    public static boolean validConnection = false;

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
        System.out.println("Session IDs: " + sessionIDs);
        System.out.println("Session ID to validate: " + sessionID);
        if (sessionIDs.contains(sessionID)){
            validConnection = true;
            return true;
        } else {
            validConnection = false;
            return false;
        }
    }

    // Server Invoke this method after validation completed
    public static void removeSessionID(String sessionID) {
        sessionIDs.remove(sessionID);
    }

}
