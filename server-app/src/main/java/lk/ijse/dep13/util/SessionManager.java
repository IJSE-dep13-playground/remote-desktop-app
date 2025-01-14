package lk.ijse.dep13.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static final Set<String> sessionIDs = new HashSet<>();

    public static synchronized String generateSessionId() {
        String generatedID;
        do {
            generatedID = String.format("%06d", (int) (Math.random() * 1000000)); // 6-digit ID
        } while (sessionIDs.contains(generatedID));
        sessionIDs.add(generatedID);
        return generatedID;
    }

    public static synchronized boolean validateSessionId(String sessionId) {
        return sessionIDs.contains(sessionId);
    }

    public static synchronized void removeSessionId(String sessionId) {
        sessionIDs.remove(sessionId);
    }

    public static void main(String[] args) {
        String sessionId = generateSessionId();
        System.out.println("Generated Session ID: " + sessionId);
        System.out.println("Validation Result: " + validateSessionId(sessionId));
    }
}
