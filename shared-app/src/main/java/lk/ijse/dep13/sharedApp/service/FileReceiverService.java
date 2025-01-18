package lk.ijse.dep13.sharedApp.service;


import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public interface FileReceiverService {

    void receiveFile (Socket localSocket, TextField downloadLocation) throws IOException, ClassNotFoundException;
}
