package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep13.sharedApp.util.ChatAPI;

import java.io.IOException;
import java.net.Socket;

public class MessageController {
    public TextArea txtAreaChat;
    public Button btnSend;
    public TextField txtInput;
    public Label lblH1;
    public AnchorPane root;

    private ChatAPI chatAPI;

    public void initialize(Socket socket) {
        try {
            this.chatAPI = new ChatAPI(socket);

            // Start a thread to listen for incoming messages
            new Thread(() -> {
                try {
                    while (true) {
                        String message = chatAPI.receiveMessage();
                        Platform.runLater(() -> txtAreaChat.appendText("Client: " + message + "\n"));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> txtAreaChat.appendText("Connection closed.\n"));
                }
            }).start();

        } catch (IOException e) {
            txtAreaChat.appendText("Failed to initialize chat.\n");
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