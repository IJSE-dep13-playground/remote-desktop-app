package lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl;

import javafx.application.Platform;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiverServiceImpl01 implements FileReceiverService {
    @Override
    public void initialize(ServerSocket serverSocket, Socket localSocket) throws IOException {
        serverSocket=new ServerSocket(9080);
       localSocket=serverSocket.accept();

    }

    @Override
    public void recieveFileFromServer() throws IOException{

    }


}
