package lk.ijse.dep13.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public void btnCreateSessionOnAction(ActionEvent actionEvent) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9080);
            System.out.println("Server started on port 9080, Waiting for connection...");
        } catch (BindException e) {
            serverSocket = new ServerSocket(0);
            switchAlert(Alert.AlertType.INFORMATION,"Port Change","port 9080 already in use","Instead, server port is use" + serverSocket.getLocalPort());
        }
        while (true) {
            Socket clientSocket = serverSocket.accept();
            switchAlert(Alert.AlertType.INFORMATION,"Connected",null, "Client connected from " + clientSocket.getInetAddress().getHostAddress());
            System.out.println("Client connected");

            new Thread(() -> handleClient(clientSocket)).start();
        }
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
                ImageIO.write(screen, "png", baos);
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
    private void switchAlert(Alert.AlertType AlertType, String title, String header, String content) {
        Alert alert = new Alert(AlertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
