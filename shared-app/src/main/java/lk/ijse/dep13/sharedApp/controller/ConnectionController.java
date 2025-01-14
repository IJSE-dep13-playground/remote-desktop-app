package lk.ijse.dep13.sharedApp.controller;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    public TextField txtServerIp;
    public Label lblH1;

    private static ConnectionController instance;

    public ConnectionController() {
        instance = this;
    }

    public static ConnectionController getInstance() {
        return instance;
    }


    public void init(String name) {
        lblH1.setText(name);
    }

    public void initConnection(String serverIP, String clientIP,String serverPort, String connectionTime) {
            txtServerIp.setText(serverIP);
            txtClientIp.setText(clientIP);
            txtPort.setText(serverPort);
            lblTime.setText(connectionTime);
    }

    public void connect(String serverIP, String clientIP,String serverPort, String connectionTime) {
        txtServerIp.setText(serverIP);
        txtClientIp.setText(clientIP);
        txtPort.setText(serverPort);
        lblTime.setText(connectionTime);
    }
}
