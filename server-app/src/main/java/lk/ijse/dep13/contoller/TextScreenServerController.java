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
    InputStream is;
    private boolean isServerSocketOpen = false;
    private boolean isClientAccepted = false;
    private boolean isInputStreamOpen = false;
    private boolean isOutPutStreamOpen = false;

    public void initialize() throws IOException {
        openServerSocket();
        new Thread(() -> {
            acceptClientRequest();
            acceptInputStream();
            readInput();
        }).start();
    }

    public void openServerSocket() {
        try {
            serverSocket = new ServerSocket(9090);
            isServerSocketOpen = true;
        } catch (Exception e) {

            txtAreaHistory.setText("could not open the server socket");
            isServerSocketOpen = false;
        }
    }

    public void acceptClientRequest() {
        if (isClientAccepted) return;
        if (isServerSocketOpen) {
            try {
                txtAreaHistory.setText("waiting for the client to request connection...");

                localSocket = serverSocket.accept();
                txtAreaHistory.setText("client request accepted");

                isClientAccepted = true;

            } catch (Exception e) {

                txtAreaHistory.setText("could not accept the client request");
                isClientAccepted = false;
            }
        } else {
            txtAreaHistory.setText("sever socket is not open");
            isClientAccepted = false;
        }
    }

    public void acceptInputStream() {
        if (isClientAccepted) {
            try {
                is = localSocket.getInputStream();
                txtAreaHistory.setText("inputstream is open");
                isInputStreamOpen = true;
            } catch (Exception e) {

                txtAreaHistory.setText("An error occured while opening inputstream");

                isInputStreamOpen = false;
            }
        } else {

            txtAreaHistory.setText("client is not accepted");
            isInputStreamOpen = false;
        }
    }

    public void readInput() {
        if (isInputStreamOpen) {
            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    int read;//=0;
                    while (true) {
                        read = is.read();
                        if (read == -1) break;
                        txtHistory += (char) read;
                        updateValue(txtHistory);
                    }
                    return "input stream closed";
                }
            };

            new Thread(task).start();
            txtAreaHistory.textProperty().bind(task.valueProperty());
        } else {
            txtAreaHistory.setText("error accepting input stream");
        }
    }

    public void btnReStartServerOnAction(ActionEvent actionEvent) {
        acceptClientRequest();
        acceptInputStream();
        readInput();

    }

    public void keyPressed(KeyEvent keyEvent) {
        if (isClientAccepted) {
            try {
                if (!isOutPutStreamOpen) {
                    os = localSocket.getOutputStream();
                    isOutPutStreamOpen = true;
                }
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    os.write(txtScreen.getText().getBytes());
                    os.flush();
                    txtScreen.clear();
                }

            } catch (Exception e) {
                txtAreaHistory.setText("an error occured while trying to opn outstream");
            }

        }
    }

    public void txtScreenOnMouseCLicked(MouseEvent mouseEvent) {
    }
}
