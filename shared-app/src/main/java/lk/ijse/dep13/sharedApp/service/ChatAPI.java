package lk.ijse.dep13.sharedApp.service;

import java.io.*;
import java.net.Socket;

public interface ChatAPI {


    public void sendMessage(String message) throws IOException;

    public String receiveMessage() throws IOException;

}
