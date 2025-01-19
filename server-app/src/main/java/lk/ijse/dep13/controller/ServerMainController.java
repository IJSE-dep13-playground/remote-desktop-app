package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lk.ijse.dep13.sharedApp.controller.FileSenderController;
import lk.ijse.dep13.sharedApp.controller.MessageController;
import lk.ijse.dep13.sharedApp.controller.VideoCallController;
import lk.ijse.dep13.sharedApp.util.AudioRecorder;
import lk.ijse.dep13.sharedApp.util.SessionManager;
import lk.ijse.dep13.sharedApp.util.SharedAppRouter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
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
    private ServerSocket screenServerSocket = null;
    private Socket localSocket = null;
    private ServerSocket videoServerSocket = null;
    private ServerSocket audioServerSocket = null;
    private ServerSocket messageServerSocket = null;
    private Socket screenSocket = null;
    private ServerSocket fileTransferServerSocket=null;
    private boolean sessionActive = false;


    public void initialize() {
        updateServerStatus("Create a Session to connect...","#0066ff");
        btnEndSession.setDisable(true);
    }

    public void btnCreateSessionOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            if (initializeServerSockets()){
               listenForClient();
            }
        }).start();
    }

    private boolean initializeServerSockets(){
        try {
            serverSocket = new ServerSocket(9080);
            videoServerSocket = new ServerSocket(9081);
            audioServerSocket = new ServerSocket(9082);
            messageServerSocket = new ServerSocket(9083);
            screenServerSocket = new ServerSocket(9084);
            fileTransferServerSocket=new ServerSocket(9085);
            sessionActive = true;
            Platform.runLater(() -> {
                btnCreateSession.setDisable(true);
                btnEndSession.setDisable(false);
                updateServerStatus("Server started on port 9080. Waiting for connection..." + SessionManager.generateSessionID(), "green");
            });
            return true;
        } catch (BindException e) {
            // handle of port conflicts
            return handlePortConflicts();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handlePortConflicts(){
        try {
            serverSocket = new ServerSocket(0);
            videoServerSocket = new ServerSocket(0);
            audioServerSocket = new ServerSocket(0);
            messageServerSocket = new ServerSocket(0);
            screenServerSocket = new ServerSocket(0);
            fileTransferServerSocket = new ServerSocket(0);
            int newPort = serverSocket.getLocalPort();
            Platform.runLater(() -> {
                    updateServerStatus("Port 9080 already in use. Server started on port " + newPort,"orange");
                    btnCreateSession.setDisable(true);
                    btnEndSession.setDisable(false);
            System.out.println("Server started on port " + newPort);
            });
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> updateServerStatus("Failed to start the server.","red"));
            return false;
        }
    }

    private void listenForClient(){
            try {
                while (!serverSocket.isClosed()) {
                    localSocket = serverSocket.accept();
                    String clientAddress = localSocket.getInetAddress().getHostAddress();
                    Platform.runLater(() -> {
                        btnEndSession.setDisable(false);
                    });

                    // check session ID
                    handleClient(localSocket);
                }
            } catch(IOException e){
                Platform.runLater(() -> {
                    switchAlert(Alert.AlertType.INFORMATION, "Session stopped", null, "Session has been stopped");
                    updateServerStatus("Connection lost.", "red");
                });
            }
    }

    private void handleClient(Socket localSocket){
        new Thread(() -> {
            try(
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(localSocket.getOutputStream()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
            ) {
                // Validate Session ID
                String sessionID = br.readLine();
                if (SessionManager.validateSessionID(sessionID)) {
                    bw.write("VALID\n");
                    bw.flush();
                    System.out.println("Session ID validated: " + sessionID);

                    // Start writing displaying images
                    screenSocket = screenServerSocket.accept();
                    shareScreen(screenSocket);
                    SessionManager.removeSessionID(sessionID);
                    localSocket.close();
                } else {
                    bw.write("INVALID\n");
                    bw.flush();
                    System.out.println("Session ID invalid: " + sessionID);
                    localSocket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void shareScreen(Socket clientSocket) throws IOException {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ) {
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
            } catch(Exception e) {
            System.out.println("Error while sharing screen");
            Platform.runLater(()-> switchAlert(Alert.AlertType.ERROR, "Disconnected",null, " Client Disconnected from " + clientSocket.getInetAddress().getHostAddress()));
            cleanUpServerSockets();
        }
    }

    private void cleanUpServerSockets() {
        closeSocket(localSocket, "localSocket");
        closeSocket(screenSocket, "screenSocket");
        closeSocket(serverSocket, "serverSocket");
        closeSocket(screenServerSocket, "screenServerSocket");
        closeSocket(audioServerSocket, "audioServerSocket");
        closeSocket(videoServerSocket, "videoServerSocket");
        closeSocket(messageServerSocket, "messageServerSocket");

        // Reset UI buttons
        Platform.runLater(() -> {
            btnEndSession.setDisable(true);
            btnCreateSession.setDisable(false);
        });
    }

    private void updateServerStatus(String status, String color) {
        lblConnection.setText(status);
        crlStatus.setStyle("-fx-fill: " + color + ";");
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
        FXMLLoader loader = SharedAppRouter.getContainer(SharedAppRouter.Routes.FILE_SENDER);
        Scene scene = new Scene(loader.load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        if (sessionActive){
            try {
                System.out.println("waiting for the client");
                Socket fileTransferSocket =fileTransferServerSocket.accept();
                System.out.println("Client connected!");

                // Retrieve the controller from the same loader instance
                FileSenderController controller = loader.getController();
                controller.initialize(fileTransferSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hBoxVideoOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        FXMLLoader loader = SharedAppRouter.getContainer(SharedAppRouter.Routes.VIDEO_CALL);
        Scene scene = new Scene(loader.load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        if (sessionActive){
            try{
                Socket videoSocket = videoServerSocket.accept();
                VideoCallController controller = loader.getController();
                controller.initialize(videoSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hBoxChatOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        FXMLLoader loader = SharedAppRouter.getContainer(SharedAppRouter.Routes.MESSAGE);
        Scene scene = new Scene(loader.load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        if (sessionActive){
            try {
                Socket messageSocket = messageServerSocket.accept();
                System.out.println("Client connected!");

                // Retrieve the controller from the same loader instance
                MessageController controller = loader.getController();
                controller.initialize(messageSocket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

            // Close all sockets safely using the helper method
            closeSocket(localSocket, "localSocket");
            closeSocket(screenSocket, "screenSocket");
            closeSocket(serverSocket, "serverSocket");
            closeSocket(screenServerSocket, "screenServerSocket");
            closeSocket(audioServerSocket, "audioServerSocket");
            closeSocket(videoServerSocket, "videoServerSocket");
            closeSocket(messageServerSocket, "messageServerSocket");
            closeSocket(fileTransferServerSocket, "fileTransferServerSocket");

            Platform.runLater(() -> {
                updateServerStatus("Connection Closed", "red");
                btnEndSession.setDisable(true);
                btnCreateSession.setDisable(false);
            });
        } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            sessionActive = false;
            alert.close();
        }
    }

    private void closeSocket(Closeable socket, String socketName) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing " + socketName + ": " + e.getMessage());
            }
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
