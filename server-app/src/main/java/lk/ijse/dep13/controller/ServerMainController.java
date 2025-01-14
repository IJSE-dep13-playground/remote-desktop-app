package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.dep13.sharedApp.controller.VideoCallController;
import lk.ijse.dep13.sharedApp.util.AudioRecorder;
import lk.ijse.dep13.sharedApp.util.SharedAppRouter;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;

public class ServerMainController {

    public Label lblWelcome;
    public HBox hBoxSession;
    public Button btnCreateSession;
    public Label lblCreateSession;
    public VBox vBoxNavBar;
    public AnchorPane root;
    public HBox hBoxFileSender;
    public HBox hBoxVideo;
    public Label lblConnection;
    public Circle crlStatus;
    public Button btnEndSession;

    private ServerSocket serverSocket = null;
    private Socket localSocket = null;
    private ServerSocket videoServerSocket = null;
    private ServerSocket audioServerSocket = null;
    private boolean sessionActive = false;


    public void initialize() {
        lblConnection.setText("Server Need to Connect...");
        crlStatus.setStyle("-fx-fill: #0066ff");
    }

    public void btnCreateSessionOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9080);
                videoServerSocket = new ServerSocket(9081);
                audioServerSocket = new ServerSocket(9082);
                sessionActive = true;
                Platform.runLater(() -> {
                    lblConnection.setText("Server started on port 9080. Waiting for connection...");
                    crlStatus.setStyle("-fx-fill: green");
                });
                System.out.println("Server started on port 9080, Waiting for connection...");
            } catch (BindException e) {
                try {
                    serverSocket = new ServerSocket(0);
                    videoServerSocket = new ServerSocket(0);
                    audioServerSocket = new ServerSocket(0);
                    int newPort = serverSocket.getLocalPort();
                    Platform.runLater(() -> {
                        lblConnection.setText("Port 9080 already in use. Server started on port " + newPort);
                        crlStatus.setStyle("-fx-fill:  orange;");
                    });
                    System.out.println("Server started on port " + newPort);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        lblConnection.setText("Failed to start the server.");
                        crlStatus.setStyle("-fx-fill:  red;");
                    });
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

                    Platform.runLater(() -> {
                        lblConnection.setText("Client connected from: " + clientAddress);
                        crlStatus.setStyle("-fx-fill:  green;");
                    });

                    Platform.runLater(() -> switchAlert(Alert.AlertType.INFORMATION, "Connected", null, "Client connected from " + clientAddress));
                    // Start writing displaying images
                    new Thread(() -> handleClient(localSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        lblConnection.setText("Connection lost.");
                        crlStatus.setStyle("-fx-fill:  red;");
                    });
                }
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            Robot robot = new Robot();
            while (!clientSocket.isClosed()) {
                try{
                // Capture the screen
                BufferedImage screen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screen, "jpeg", baos);
                byte[] imagesBytes = baos.toByteArray();

                oos.writeObject(imagesBytes);
                oos.flush();
                Thread.sleep(1000 / 30); // 30 FPS

                } catch (SocketException e){
                    System.err.println("Client disconnected or socket closed " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
               lblConnection.setText("Client disconnected.");
               crlStatus.setStyle("-fx-fill: red;");
            });
        }
    }
    private static void switchAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void hBoxFileSenderOnMouseClicked(MouseEvent mouseEvent) throws IOException{
        Stage stage = new Stage(StageStyle.UTILITY);
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.FILE_SENDER).load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    public void hBoxVideoOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.VIDEO_CALL).load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        // Access the shared controller
        VideoCallController videoCallController = VideoCallController.getInstance();

        new Thread(() -> {
            try {
                sendVideoStream(videoServerSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                receiveVideoStream(videoServerSocket.accept(), videoCallController.imgVideoPreview);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        // Add audio streaming functionality
        new Thread(() -> {
            try {
                Socket audioSocket = audioServerSocket.accept();
                startAudioStreaming(audioSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void sendVideoStream(Socket videoSocket) throws IOException {
        try{
            Webcam webcam = Webcam.getDefault();
            webcam.open();
            try {
                OutputStream os = videoSocket.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                ObjectOutputStream oos = new ObjectOutputStream(bos);

                while (!videoSocket.isClosed()) {
                    BufferedImage bufferedImage = webcam.getImage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage,"jpeg",baos);

                    System.out.println("Sending frame of size: " + baos.toByteArray());
                    oos.writeObject(baos.toByteArray());
                    oos.flush();
                    Thread.sleep(1000/30);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Video call disconnected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveVideoStream(Socket videoSocket, ImageView imgVideoPreview) throws IOException {
            Task<Image> task = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    InputStream is = videoSocket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ObjectInputStream ois = new ObjectInputStream(bis);

                    while (!videoSocket.isClosed()) {
                        byte[] bytes = (byte[]) ois.readObject();
                        updateValue(new Image(new ByteArrayInputStream(bytes)));
                    }
                    return null;
                }
            };
            Platform.runLater(() -> {
                imgVideoPreview.imageProperty().bind(task.valueProperty());
            });
            new Thread(task).start();
    }

    private void startAudioStreaming(Socket audioSocket) {
        try {
            AudioRecorder audioRecorder = new AudioRecorder();

            // Start sending audio
            new Thread(() -> {
                try {
                    audioRecorder.startRecording(audioSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Start receiving and playing audio
            new Thread(() -> {
                try {
                    audioRecorder.startPlaying(audioSocket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnEndSessionOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("End Session");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to end the session?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sessionActive = false;
            try{
                localSocket.close();
                videoServerSocket.close();
                audioServerSocket.close();
                serverSocket.close();
                Platform.runLater(() -> {
                   lblConnection.setText("Connection Closed");
                   crlStatus.setStyle("-fx-text-fill: red");
                });
            } catch (IOException e) {
                System.out.println("Error closing local socket");
            }
        } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            sessionActive = false;
            alert.close();
        }
    }

}
