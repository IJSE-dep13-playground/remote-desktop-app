package lk.ijse.dep13.sharedApp.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;
import lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl.FileReceiverServiceImpl;
import lk.ijse.dep13.sharedApp.service.FileSenderService;
import lk.ijse.dep13.sharedApp.service.FileSenderServiceImpl.FileSenderService_OOS;

import java.io.*;
import java.net.Socket;

public class FileSenderController {
    public AnchorPane root;
    public TextField txtFileLocation;
    public Button btnSend;
    public Label lblH2;
    public Label lblH1;
    public Button btnBrowse;
    public TextField txtSavedLocation;
    private Socket fileTransferSocket;

    public File file;

    FileSenderService fileSenderService = new FileSenderService_OOS();
    FileReceiverService fileReceiverService = new FileReceiverServiceImpl();

    public void initialize(Socket socket) {

        btnSend.setDisable(true);
        this.fileTransferSocket=socket;
    }

    public void btnSendOnAction(ActionEvent actionEvent){
        new Thread(() -> {
            try {
                fileSenderService.sendFile(file,fileTransferSocket);
            } catch (IOException e) {
                System.out.println("Error sending file");
                throw new RuntimeException(e);
            }
        }).start();
    }

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

    public void btnReceiveOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                fileReceiverService.receiveFile(fileTransferSocket,txtSavedLocation);
            }catch (Exception e){
                System.out.println("Error receiving file");
                e.printStackTrace();
            }
        }).start();
}}
