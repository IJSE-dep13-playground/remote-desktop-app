package lk.ijse.dep13.contoller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.ECField;

public class TextScreenServerController {
    public TextArea txtScreen;
    public TextArea txtAreaHistory;
    String txtHistory = "";
    ServerSocket serverSocket;
    Socket localSocket;
    OutputStream os;
    private volatile boolean connectionEstabilished = false;
    private boolean serverSocketOpen = false;
    //private  boolean clientAccepted=false;


    public void initialize() throws IOException {
        openServerSocket();
     openOutputStream();



    }

    public void openServerSocket() {
        try {
            serverSocket = new ServerSocket(9090);
            serverSocketOpen = true;
            txtAreaHistory.setText("server started.");
        } catch (Exception e) {
            serverSocketOpen=false;
            txtAreaHistory.setText("error starting the server");
        }
    }


    public boolean accepetClient() {
        try {
            localSocket = serverSocket.accept();
           return true;
        } catch (Exception e) {
            txtAreaHistory.setText("Error occured while trying to accept the client");
            return false;
        }
    }

    public void openOutputStream() {

        new Thread(() -> {
            try {
                if (accepetClient()){
                connectionEstabilished = true;
                os = localSocket.getOutputStream();

                txtScreen.setDisable(false);
                txtScreen.requestFocus();
                txtAreaHistory.setEditable(false);
                txtScreen.setDisable(true);}
            } catch (Exception e) {
            }
        }).start();

    }

    public void openInputStream() {
        openOutputStream();
        if (connectionEstabilished) {
            System.out.println("connection established");
            txtScreen.setDisable(false);

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {

                    InputStream is = localSocket.getInputStream();
                    while (true) {
                        int read = is.read();
                        if (read == -1) break;
                        txtHistory += (char) read + "";
                        updateValue(txtHistory);

                    }
                    return "connection lost";
                }
            };
            new Thread(task).start();
            ;
            txtAreaHistory.textProperty().bind(task.valueProperty());
        } else {

            txtAreaHistory.setText("client is offline");
        }
    }

    public void keyPressed(KeyEvent keyEvent) throws IOException, InterruptedException {


        if (keyEvent.getCode() == KeyCode.ENTER) {
            String txt = txtScreen.getText();
            txtScreen.clear();
            os.write(txt.getBytes());
            os.flush();
        }

    }

    public void txtScreenOnMouseCLicked(MouseEvent mouseEvent) {

        txtScreen.clear();
    }


    public void btnReStartServerOnAction(ActionEvent actionEvent) {
        openInputStream();

    }
}
