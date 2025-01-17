package lk.ijse.dep13.sharedApp.service;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public interface FileReceiverService {
    void initialize(ServerSocket serverSocket, Socket localSocket) throws IOException;
    void recieveFileFromServer() throws IOException;
}
