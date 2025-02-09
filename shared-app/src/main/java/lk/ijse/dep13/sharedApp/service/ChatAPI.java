package lk.ijse.dep13.sharedApp.service;

import java.io.*;

public interface ChatAPI {
    void sendMessage(String message) throws IOException;

    String receiveMessage() throws IOException;
}