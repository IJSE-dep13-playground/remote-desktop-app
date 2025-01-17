package lk.ijse.dep13.sharedApp.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;
import lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl.FileReceiverServiceImpl01;
import lk.ijse.dep13.sharedApp.service.FileSenderService;
import lk.ijse.dep13.sharedApp.service.FileSenderServiceImpl.FileSenderService_OOS;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileSenderController {
    public AnchorPane root;
    public TextField txtFileLocation;
    public Button btnSend;
    public Label lblH2;
    public Label lblH1;
    public Button btnBrowse;
    private Socket localSocket;

    public File file;
    private boolean isClient;  // flag to determine if it's client or server

    FileSenderService fileSenderService=new FileSenderService_OOS();
   FileReceiverService fileReceiverService=new FileReceiverServiceImpl01();

    // Set whether this is a client or server instance
    public void setIsClient(boolean isClient) {
        this.isClient = isClient;
        if (isClient) {
            lblH1.setText("Client - File Sender");
        } else {
            lblH1.setText("Server - File Sender");
        }
    }

    public void initialize(Socket socket) {
        btnSend.setDisable(true);
        this.localSocket=socket;

    }

    public void btnSendOnAction(ActionEvent actionEvent) throws IOException {
//        if (isClient) {
//            sendFileToServer();
//        } else {
//            receiveFileFromClient();
//        }
        sendFileToServer();
    }

    private void sendFileToServer() throws IOException {
       fileSenderService.sendFileToServer(file);
    }
//
//    private void receiveFileFromClient() throws IOException {
//        // Start a server socket to listen for client file transfer requests
//        try {
//            ServerSocket serverSocket = new ServerSocket(9898);
//            Socket socket = serverSocket.accept();  // Accept client connection
//
//            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
//            FileOutputStream fos = new FileOutputStream("received_file");
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = bis.read(buffer)) != -1) {
//                bos.write(buffer, 0, read);
//            }
//
//            System.out.println("File received successfully.");
//            bos.close();
//            bis.close();
//            socket.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//    }

    public void btnBrowseOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null && file.exists()) {
            txtFileLocation.setText(file.getAbsolutePath());
            btnSend.setDisable(false);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a file");
            alert.showAndWait();
        }
    }


    public void btnRecieveOnAction(ActionEvent actionEvent) {

            try {
                fileReceiverService.recieveFileFromClient(localSocket);

            }catch (Exception e){
                e.printStackTrace();
    }
}}
