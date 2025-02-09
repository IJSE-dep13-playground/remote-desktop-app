package lk.ijse.dep13.sharedApp.service;

import java.io.*;
import java.net.Socket;

public interface ChatAPI {


     void sendMessage(String message) throws IOException;

     String receiveMessage() throws IOException;

}
