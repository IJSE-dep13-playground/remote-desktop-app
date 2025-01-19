package lk.ijse.dep13.sharedApp.controller;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class ConnectionController {

    public Label lblPort;
    public TextField txtPort;
    public Label lblTime;
    public Circle crlStatus;
    public Label lblStatus;
    public Label lblClientIp;
    public TextField txtClientIp;
    public Label lblServerIp;
    public Label lblH1;

    private static ConnectionController instance;
    public Label lblConnectionStatus;
    public TextField txtTime;
    public ImageView imgBackground;
    public TextField txtServerIp;

    public ConnectionController() {
        instance = this;
    }

    public static ConnectionController getInstance() {
        return instance;
    }

    public void initConnection(String serverIP, String clientIP,String serverPort, String connectionTime) {
            txtServerIp.setText(serverIP);
            txtClientIp.setText(clientIP);
            txtPort.setText(serverPort);
            txtTime.setText(connectionTime);
    }

    public void connect(String clientIP,String serverIP, String port, String connectionTime) {
        txtClientIp.setText(clientIP);
        txtServerIp.setText(serverIP);
        txtPort.setText(port);
        txtTime.setText(connectionTime);
    }
}
