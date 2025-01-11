package lk.ijse.dep13.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.*;
import java.net.Socket;

public class ClientMainController {
    public VBox vBoxNavBar;
    public HBox hBoxVideo;
    public HBox hBoxChat;
    public HBox hBoxConnection;
    public HBox hBoxDeskMe;
    public Circle crlConnectionStatus;
    public Label lblConnection;
    public Label lblWelcome;
    public HBox hBoxSettings;
    public Pane pnHome;
    public Button btnAbortSession;
    public Pane pnSession;
    public ImageView imgPreview;
    public AnchorPane root;
    public Button btnJoinSession;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sessionActive = false;


    public void initialize() throws IOException {
        imgPreview.fitWidthProperty().bind(pnSession.widthProperty());
        imgPreview.fitHeightProperty().bind(pnSession.heightProperty());
    }

    public void btnAbortSessionOnAction(ActionEvent actionEvent) {
        try {
            sessionActive = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            btnJoinSession.setDisable(false);
            btnAbortSession.setDisable(true);
            lblConnection.setText("Disconnected");
            crlConnectionStatus.setStyle("-fx-fill: red;");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startImageReceiver() {
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() {
                try {
                    while (sessionActive) {
                        byte[] imageBytes = (byte[]) ois.readObject();
                        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                        Image image = new Image(bais);
                        updateValue(image);
                    }
                } catch (Exception e) {
                    if (sessionActive) e.printStackTrace();
                }
                return null;
            }
        };
        imgPreview.imageProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    public void btnJoinSessionOnAction(ActionEvent actionEvent) {
        if (sessionActive) return;
        try {
            socket = new Socket("127.0.0.1", 9080);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            sessionActive = true;
            btnAbortSession.setDisable(false);
            btnJoinSession.setDisable(true);
            lblConnection.setText("Connected");
            crlConnectionStatus.setStyle("-fx-fill: green;");

            // Start receiving and displaying images
            startImageReceiver();
        } catch (IOException e) {
            e.printStackTrace();
            lblConnection.setText("Connection Failed");
            crlConnectionStatus.setStyle("-fx-fill: red;");
        }
    }
}
