package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.dep13.sharedApp.util.SharedAppRouter;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMainController {

    public Label lblWelcome;
    public HBox hBoxSession;
    public Button btnCreateSession;
    public Label lblCreateSession;
    public VBox vBoxNavBar;
    public AnchorPane root;
    public HBox hBoxFileSender;
    public HBox hBoxVideo;

    private ServerSocket serverSocket = null;
    private Socket localSocket = null;

    public void initialize() {

    }

    public void btnCreateSessionOnAction(ActionEvent actionEvent) throws IOException {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9080);
                System.out.println("Server started on port 9080, Waiting for connection...");
            } catch (BindException e) {
                try {
                    serverSocket = new ServerSocket(0);
                    int newPort = serverSocket.getLocalPort();
                    Platform.runLater(() -> switchAlert(Alert.AlertType.INFORMATION, "Port Change", "Port 9080 already in use", "Instead, server is using port " + newPort));
                    System.out.println("Server started on port " + newPort);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!serverSocket.isClosed()) {
                try{
                    localSocket = serverSocket.accept();
                    String clientAddress = localSocket.getInetAddress().getHostAddress();
                    System.out.println("Client connected from: " + clientAddress);

                    Platform.runLater(() -> switchAlert(Alert.AlertType.INFORMATION, "Connected", null, "Client connected from " + clientAddress));
                    new Thread(() -> handleClient(localSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void handleClient(Socket clientSocket) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            Robot robot = new Robot();
            while (true) {
                // Capture the screen
                BufferedImage screen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screen, "jpeg", baos);
                byte[] imagesBytes = baos.toByteArray();
                oos.writeObject(imagesBytes);
                oos.flush();

                Thread.sleep(100);

                // Receive mouse coordinates
                if (ois.available() > 0) {
                    Point mousePoint = (Point) ois.readObject();
                    robot.mouseMove(mousePoint.x, mousePoint.y);
                }
                Thread.sleep(1000 / 30); // 30 FPS
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void switchAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void hBoxFileSenderOnMouseClicked(MouseEvent mouseEvent) throws IOException, ClassNotFoundException {
        Stage stage = new Stage(StageStyle.UTILITY);
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.FILE_SENDER).load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    public void hBoxVideoOnMouseClicked(MouseEvent mouseEvent) {
//        Webcam webcam = Webcam.getDefault();
//        webcam.open();
//
//        new Thread(() -> {
//            try {
//                OutputStream os = localSocket.getOutputStream();
//                BufferedOutputStream bos = new BufferedOutputStream(os);
//                ObjectOutputStream oos = new ObjectOutputStream(bos);
//
//                while (true) {
//                    BufferedImage bufferedImage = webcam.getImage();
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    ImageIO.write(bufferedImage,"jpeg",baos);
//                    oos.writeObject(baos.toByteArray());
//                    oos.flush();
//                    Thread.sleep(1000/27);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }
}
