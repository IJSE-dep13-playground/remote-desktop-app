package lk.ijse.dep13.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.*;

import java.net.Socket;

import java.util.ArrayList;


public class TextScreenClientController {

    public Button btnViewMessages;
    ArrayList<String> textMessages = new ArrayList<>();
    public TextArea txtAreaHistory;
    public TextArea txtScreen;
    Socket socket;
    InputStream is;
    boolean isConnected = false;
    OutputStream os;


    public void initialize() throws IOException {

       socket = new Socket("192.168.1.6", 9090);
        InputStream is = socket.getInputStream();
       os=socket.getOutputStream();



        Task<String> task = new Task<>() {


            @Override
            protected String call() throws Exception {
                System.out.println(7);

                System.out.println(8);
               ;
                BufferedInputStream bis = new BufferedInputStream(is);



                String chatHistory = "";
                while (true) {
                    int read = is.read();
                    char txt = (char) read;
                    if (read == -1) {

                        break;
                    }
                    String y = txt + "";
                    chatHistory += y;
                    updateValue(chatHistory);
                }
                return "connection lost...";
            }
        };

        txtAreaHistory.textProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    public void keyPressed(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode()== KeyCode.ENTER){
            String text=txtScreen.getText();
            os.write(text.getBytes());
            os.flush();
        }

    }

    public void btnConnectToServerOnAction(ActionEvent actionEvent) throws IOException, InterruptedException {

        socket = new Socket("192.168.229.165", 9090);
        txtAreaHistory.setText("succesfully connected to server");
        btnViewMessages.setDisable(false);


    }

    public void btnViewMsgsClicked(MouseEvent mouseEvent) throws IOException {
    }
}