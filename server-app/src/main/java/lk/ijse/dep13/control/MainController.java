package lk.ijse.dep13.control;

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

public class MainController {
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
    public Button btnCreateSession;
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

    public void btnCreateSessionOnAction(ActionEvent actionEvent) throws IOException {
        if (sessionActive) return;
            try {
                socket = new Socket("192.168.229.138", 9080);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                sessionActive = true;
                btnAbortSession.setDisable(false);
                btnCreateSession.setDisable(true);
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

    public void btnAbortSessionOnAction(ActionEvent actionEvent) {
        try {
            sessionActive = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            btnCreateSession.setDisable(false);
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

    }
}
