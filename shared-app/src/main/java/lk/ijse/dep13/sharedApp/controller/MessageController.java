package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import lk.ijse.dep13.sharedApp.service.ChatAPI;
import lk.ijse.dep13.sharedApp.service.chatAPIImpl.ChatAPIService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class MessageController {
    public TextArea txtAreaChat;
    public Button btnSend;
    public TextField txtInput;
    public Label lblH1;
    public AnchorPane root;


    String test = null;
    private ChatAPI chatAPI;
    private volatile boolean running = true;

    public void initialize(BufferedWriter bw, BufferedReader br) throws IOException {
        chatAPI = new ChatAPIService(bw,br);
        new Thread(() -> {
           l1 :while (running) {

                try {
                    String message = chatAPI.receiveMessage();
                    if(message.equals("closingTheChat-876213")) break l1;
                    Platform.runLater(() -> txtAreaChat.appendText("Client: " + message + "\n"));
                } catch (IOException e) {
                    Platform.runLater(() -> txtAreaChat.appendText("Connection closed.\n"));
                    throw new RuntimeException(e);
                }

            }

        }).start();

    }

    public void handleCloseRequest(WindowEvent event)  {
        running = false;
        System.out.println("Window has been closed");
        try {
        chatAPI.sendMessage("closingTheChat-876213");}catch (IOException e){
            e.printStackTrace();
        }


    }


    public void btnSend(ActionEvent actionEvent) {
        String message = txtInput.getText().strip();
        if (message.isEmpty()) return;

        try {
            chatAPI.sendMessage(message);
            txtAreaChat.appendText("You: " + message + "\n");
            txtInput.clear();
        } catch (IOException e) {
            txtAreaChat.appendText("Failed to send message.\n");
        }
    }
}