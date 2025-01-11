package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.w3c.dom.ls.LSOutput;

import javax.print.DocFlavor;
import java.io.*;

import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TextScreenClientController {

    public Button btnViewMessages;
    ArrayList<String> textMessages = new ArrayList<>();
    public TextArea txtAreaHistory;
    public TextArea txtScreen;
    Socket socket;
    InputStream is;
    boolean isConnected = false;

    public void initialize() {


        Task<String> task = new Task<>() {


            @Override
            protected String call() throws Exception {
                System.out.println(7);
                Socket socket = new Socket("192.168.1.6", 9090);
                System.out.println(8);
                InputStream is = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
               //InputStream is = new InputStream(bis);


            String chatHistory="";
                while (true){
                   char txt=(char)(is.read());

                   String y=txt+"";
                   chatHistory+=y;
                    updateValue(chatHistory);
                }
            }
        };

//        imgCamera.imageProperty().bind(task.valueProperty());
        txtAreaHistory.textProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    public void keyPressed(KeyEvent keyEvent) throws IOException {


    }

    public void btnConnectToServerOnAction(ActionEvent actionEvent) throws IOException, InterruptedException {

        socket = new Socket("192.168.229.165", 9090);
        txtAreaHistory.setText("succesfully connected to server");
        btnViewMessages.setDisable(false);


    }

    public void btnViewMsgsClicked(MouseEvent mouseEvent) throws IOException {
    }
}