package lk.ijse.dep13.contoller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TextScreenServerController {
    public TextArea txtScreen;
    public TextArea txtAreaHistory;
    public Button btnStartServer;
    String txtHistory = "";
    ServerSocket serverSocket;
    Socket localSocket;
    OutputStream os;

    public void initialize() throws IOException {
        serverSocket = new ServerSocket(9090);

        txtAreaHistory.setEditable(false);
        txtScreen.setDisable(true);
    }


    public void keyPressed(KeyEvent keyEvent) throws IOException, InterruptedException {


        if (keyEvent.getCode() == KeyCode.ENTER) {


            String txt = txtScreen.getText();
            String deliverMsg=txt.replaceAll("[\\s\\n]+$", "");
            deliverMsg=deliverMsg+'\n';

            txtScreen.clear();

            os.write(txt.getBytes());

            os.flush();
           // os.close();


        }

    }

    public void txtScreenOnMouseCLicked(MouseEvent mouseEvent) {
        txtScreen.clear();
    }

    public void btnStartServerOnAction(ActionEvent actionEvent) throws IOException {

        txtAreaHistory.setText("waiting for the client to connect...");
        localSocket = serverSocket.accept();
        os=localSocket.getOutputStream();
        txtAreaHistory.setText("client is online and connected");
        txtScreen.setDisable(false);
        txtScreen.requestFocus();

    }
}

