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
import lk.ijse.dep13.util.SessionManager;

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
    public HBox hBoxConnection;

    private ServerSocket serverSocket = null;
    private ServerSocket imageServerSocket = null;
    private ServerSocket videoServerSocket = null;
    private ServerSocket audioServerSocket = null;

    private Socket imageSocket;

    private boolean sessionActive = false;


    public void initialize() {
        updateServerStatus("Server Need to Connect...","#0066ff");
        btnEndSession.setDisable(true);
    }

    public void btnCreateSessionOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            if (initializeServerSockets()){
                listenForClients();
            }
        }).start();
    }

    private boolean initializeServerSockets(){
        try{
            serverSocket = new ServerSocket(9080);
            imageServerSocket = new ServerSocket(9090);
            videoServerSocket = new ServerSocket(9081);
            audioServerSocket = new ServerSocket(9082);
            sessionActive = true;
            Platform.runLater(() -> updateServerStatus("Server started on port 9080. Waiting for connection..." + SessionManager.generateSessionId(), "green"));
            System.out.println("Server started on port 9080, Waiting for connection...");
            return true;
        } catch (BindException e){
            return handlePortConflict();
        } catch (IOException e){
            e.printStackTrace();
            Platform.runLater(() -> updateServerStatus("Failed to start the server.", "red"));
            return false;
        }
    }

    private boolean handlePortConflict() {
        try{
            serverSocket = new ServerSocket(0);
            imageServerSocket = new ServerSocket(0);
            videoServerSocket = new ServerSocket(0);
            audioServerSocket = new ServerSocket(0);
            int newPort = serverSocket.getLocalPort();
            Platform.runLater(() -> Platform.runLater(() -> updateServerStatus("Port 9080 already in use. Server started on port " + newPort, "orange")));
            System.out.println("Server started on port " + newPort);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> updateServerStatus("Failed to resolve port conflict.", "red"));
            return false;
        }
    }

    private void listenForClients(){
        try{
            while(!serverSocket.isClosed()){
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected from: " + clientIP);

                Platform.runLater(() -> {
                    updateServerStatus("Client connected from: " + clientIP, "green");
                    btnEndSession.setDisable(false);
                    switchAlert(Alert.AlertType.INFORMATION, "Connected", null, "Client connected from " + clientIP);
                });
                // Handle Client - Session ID
                handleClient(clientSocket);
            }
        } catch (IOException e){
            e.printStackTrace();
            Platform.runLater(() -> updateServerStatus("Connection lost.", "red"));
        }
    }

    private void updateServerStatus(String message, String color){
        lblConnection.setText(message);
        crlStatus.setStyle("-fx-fill: " + color + ";");
    }

    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
            ) {
                // Validate session ID
                String sessionId = reader.readLine();
                if (SessionManager.validateSessionId(sessionId)) {
                    writer.write("VALID\n");
                    writer.flush();
                    System.out.println("Session ID validated: " + sessionId);

                    btnEndSession.setDisable(false);
                    // Handle session
                    imageSocket = imageServerSocket.accept();
                    startSession(imageSocket);
                } else {
                    writer.write("INVALID\n");
                    writer.flush();
                    System.out.println("Invalid session ID: " + sessionId);
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startSession(Socket clientSocket) {
        try (
                 ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ){
            Robot robot = new Robot();
            while (!clientSocket.isClosed()) {
                // Capture the screen
                BufferedImage screen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screen, "jpeg", baos);
                byte[] imagesBytes = baos.toByteArray();

                oos.writeObject(imagesBytes);
                oos.flush();
                Thread.sleep(1000 / 30); // 30 FPS
            }
        } catch (Exception e) {
                    e.printStackTrace();
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
                imageSocket.close();
                videoServerSocket.close();
                audioServerSocket.close();
                serverSocket.close();
                Platform.runLater(() -> {
                    updateServerStatus("Connection Closed","red");
                    btnEndSession.setDisable(true);
                });
            } catch (IOException e) {
                System.out.println("Error closing local socket");
            }
        } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            sessionActive = false;
            alert.close();
        }
    }

    public void hBoxDeskmeOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.resizableProperty().setValue(false);
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.ABOUT).load());
        stage.setScene(scene);
        stage.show();
    }

    public void hBoxConnectionOnMouseClicked(MouseEvent mouseEvent) {

    }
}
