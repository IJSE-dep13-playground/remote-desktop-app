package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;
import lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl.FileReceiverServiceImpl01;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReceiverController {

    public TextArea txtAreaRecievedFiles;
    ServerSocket serverSocket;
    Socket localSocket;

    FileReceiverService fileReceiverService = new FileReceiverServiceImpl01();

    public void initialize() {


    }

    private void recievedFromClient() {

    }

    public void btnOpenSocketOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9898);
                System.out.println(22);
                Platform.runLater(() -> {
                    txtAreaRecievedFiles.setText("waiting for the client to send files");
                });


                localSocket = serverSocket.accept();
                System.out.println(33);
                Platform.runLater(() -> {
                    txtAreaRecievedFiles.setText("client connected");
                });

            } catch (Exception e) {
            }
        }).start();

//        try {

//            txtAreaRecievedFiles.setText("waiting for the server to connect");
//            fileReceiverService.initialize(serverSocket, localSocket);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void btnRecieveOnAction(ActionEvent actionEvent) {
        try {
            fileReceiverService.recieveFileFromServer(txtAreaRecievedFiles,localSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
