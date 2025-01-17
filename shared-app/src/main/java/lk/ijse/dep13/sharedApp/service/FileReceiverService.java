package lk.ijse.dep13.sharedApp.service;


import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public interface FileReceiverService {
    void initialize(ServerSocket serverSocket, Socket localSocket) throws IOException;
    void recieveFileFromClient (Socket localSocket) throws IOException, ClassNotFoundException;
}
