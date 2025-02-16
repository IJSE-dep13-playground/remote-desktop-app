package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lk.ijse.dep13.sharedApp.controller.ConnectionController;
import lk.ijse.dep13.sharedApp.controller.FileSenderController;
import lk.ijse.dep13.sharedApp.controller.MessageController;
import lk.ijse.dep13.sharedApp.controller.VideoCallController;
import lk.ijse.dep13.sharedApp.util.AudioRecorder;
import lk.ijse.dep13.sharedApp.util.SharedAppRouter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class ClientMainController {
    public VBox vBoxNavBar;
    public HBox hBoxVideo;
    public HBox hBoxChat;
    public HBox hBoxConnection;
    public HBox hBoxDeskMe;
    public Circle crlConnectionStatus;
    public Label lblConnection;
    public Label lblWelcome;
    public Button btnAbortSession;
    public Pane pnSession;
    public ImageView imgPreview;
    public AnchorPane root;
    public Button btnJoinSession;
    public HBox hBoxFileSender;
    public TextField txtSessionID;
    public TextField txtServerIP;
    public Label lblSessionID;
    public Label lblServerIP;

    private Socket screenShareSocket;
    private Socket videoSocket;
    private Socket audioSocket;
    private Socket messageSocket;
    private Socket fileTransferSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sessionActive = false;
    private String startTime;
    private String serverIP;
    private BufferedWriter bw;
    private BufferedReader br;
    private ObjectInputStream ois_ft;
    private ObjectOutputStream oos_ft;

    public void initialize() throws IOException {
        btnAbortSession.setDisable(true);
        imgPreview.fitWidthProperty().bind(pnSession.widthProperty());
        imgPreview.fitHeightProperty().bind(pnSession.heightProperty());
        updateServerStatus("Waiting for join with a Server", "#0066ff");
    }

    private void updateServerStatus(String status, String color) {
        lblConnection.setText(status);
        crlConnectionStatus.setStyle("-fx-fill: " + color + ";");
    }

    private void checkSessionID(){
        String sessionID = txtSessionID.getText();
        serverIP = txtServerIP.getText();
        new Thread(() -> {
            try (Socket socket = new Socket(serverIP, 9080);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send the session ID to the server
                writer.write(sessionID + "\n");
                writer.flush();

                // Read the response
                String response = reader.readLine();

                Platform.runLater(() -> {
                    if ("VALID".equals(response)) {
                        showAlert(Alert.AlertType.INFORMATION, "Session Valid", null, "You have entered a valid session ID.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Invalid Session ID", null, "The session ID you entered is invalid.");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Connection Issue", null, "Unable to connect to the server.");
                });
            }
            }).start();
    }

    public void btnJoinSessionOnAction(ActionEvent actionEvent) {
        if (sessionActive) return; // if session already active, cant connect again
        if (txtSessionID.getText().isBlank() || txtServerIP.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR,
                    (txtServerIP.getText().isBlank()) ? "Empty Server IP" :"Empty Session ID",
                    null, (txtServerIP.getText().isBlank()) ? "Enter the Server IP Address" : "Enter the session ID");
            return;
        }
        checkSessionID();
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Set time for session validation
                Platform.runLater(() -> {
                    if (sessionActive) {
                        showAlert(Alert.AlertType.INFORMATION, "Already Connected", null, "You are already connected.");
                        return;
                    }
                    try {
                        connectToServer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // get time before socket start
                LocalTime connectionStartedTime = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                startTime = connectionStartedTime.format(formatter);

                screenShareSocket = new Socket(serverIP, 9084);
                videoSocket = new Socket(serverIP, 9081);
                audioSocket = new Socket(serverIP, 9082);
                messageSocket = new Socket(serverIP, 9083);
                bw=new BufferedWriter(new OutputStreamWriter(messageSocket.getOutputStream()));
                br=new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
                fileTransferSocket =new Socket(serverIP,9085);
              ois_ft=new ObjectInputStream(fileTransferSocket.getInputStream());
              oos_ft=new ObjectOutputStream(fileTransferSocket.getOutputStream());

                oos = new ObjectOutputStream(screenShareSocket.getOutputStream());
                ois = new ObjectInputStream(new BufferedInputStream(screenShareSocket.getInputStream()));
                sessionActive = true;
                Platform.runLater(() -> {
                    btnAbortSession.setDisable(false);
                    btnJoinSession.setDisable(true);
                    txtSessionID.clear();
                    txtServerIP.clear();
                    updateServerStatus("Connected with Server", "green");
                });
                // Start receiving and displaying images
                startImageReceiver();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR,"Connection Issue",null,"Your connection is failed to connect");
                    updateServerStatus("Connection Failed", "crimson");
                });
            }
        }).start();
    }

    private void startImageReceiver() {
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() {
                try {
                    while (sessionActive) {
                        try{
                            byte[] imageBytes = (byte[]) ois.readObject();
                            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                            Image image = new Image(bais);
                            updateValue(image);
                        } catch (EOFException e){
                            System.err.println("Connection closed by server.");
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    handleServerDisconnection();
                }
                return null;
            }
        };
        imgPreview.imageProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    private void handleServerDisconnection() {
        Platform.runLater(() -> {
            sessionActive = false;
            btnJoinSession.setDisable(false);
            btnAbortSession.setDisable(true);
            updateServerStatus("Disconnected", "red");
        });

        try {
            if (screenShareSocket != null && !screenShareSocket.isClosed()) screenShareSocket.close();
            if (videoSocket != null && !videoSocket.isClosed()) videoSocket.close();
            if (audioSocket != null && !audioSocket.isClosed()) audioSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnAbortSessionOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Abort");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to abort the session?");
        Optional<ButtonType> result = alert.showAndWait();
        new Thread(() -> {
            if (result.get() == ButtonType.OK) {
                sessionActive = false;
                if (screenShareSocket != null && !screenShareSocket.isClosed()) {
                    try {
                        screenShareSocket.close();
                        videoSocket.close();
                        audioSocket.close();
                        Platform.runLater(() -> {
                            btnJoinSession.setDisable(false);
                            btnAbortSession.setDisable(true);
                            updateServerStatus("Disconnected", "red");
                            showAlert(Alert.AlertType.INFORMATION,"Abort session",null,"The session has been successfully terminated. All connections with the server have been closed.");
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } if (result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }).start();
    }

    public void hBoxFileSenderOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        FXMLLoader loader=SharedAppRouter.getContainer(SharedAppRouter.Routes.FILE_SENDER);
        Scene scene = new Scene(loader.load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
        if(sessionActive){
            FileSenderController fileSenderController=loader.getController();
            fileSenderController.initialize(fileTransferSocket,oos_ft,ois_ft);
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void hBoxVideoOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        FXMLLoader loader = SharedAppRouter.getContainer(SharedAppRouter.Routes.VIDEO_CALL);
        Scene scene = new Scene(loader.load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        VideoCallController videoCallController = loader.getController();
        if(sessionActive){
            try{
                System.out.println("server activate");
                videoCallController.initialize(videoSocket);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        MessageController controller = loader.getController();
        stage.setOnCloseRequest(event -> {
            controller.handleCloseRequest(event);
        });

        if (sessionActive){
            try {
                controller.initialize(bw,br);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hBoxDeskMeOnMouseClicked(MouseEvent mouseEvent) throws IOException {
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.resizableProperty().setValue(false);
            Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.ABOUT).load());
            stage.setScene(scene);
            stage.show();
    }

    public void hBoxConnectionOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.resizableProperty().setValue(false);
        FXMLLoader loader = SharedAppRouter.getContainer(SharedAppRouter.Routes.CONNECTION);
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        ConnectionController connectionController = ConnectionController.getInstance();
        connectionController.connect(InetAddress.getLocalHost().getHostAddress(), serverIP,"9090",startTime);
    }
}
