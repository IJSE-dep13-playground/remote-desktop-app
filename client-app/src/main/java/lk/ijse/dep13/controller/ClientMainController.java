package lk.ijse.dep13.controller;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import lk.ijse.dep13.sharedApp.controller.ConnectionController;
import lk.ijse.dep13.sharedApp.controller.MessageController;
import lk.ijse.dep13.sharedApp.controller.VideoCallController;
import lk.ijse.dep13.sharedApp.util.AudioRecorder;
import lk.ijse.dep13.sharedApp.util.SessionManager;
import lk.ijse.dep13.sharedApp.util.SharedAppRouter;
import lk.ijse.dep13.util.AppRouter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
    public HBox hBoxSettings;
    public Button btnAbortSession;
    public Pane pnSession;
    public ImageView imgPreview;
    public AnchorPane root;
    public Button btnJoinSession;
    public HBox hBoxFileSender;
    public ImageView imgVideo;
    public TextField txtSessionID;

    private Socket socket;
    private Socket videoSocket;
    private Socket audioSocket;
    private Socket messageSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean sessionActive = false;
    private long startTime;
    private long endTime;

    public void initialize() throws IOException {
        btnAbortSession.setDisable(true);
        imgPreview.fitWidthProperty().bind(pnSession.widthProperty());
        imgPreview.fitHeightProperty().bind(pnSession.heightProperty());
    }

    public void btnJoinSessionOnAction(ActionEvent actionEvent) {
        if (sessionActive) return; // if session already active , cant connect again
        if (SessionManager.validateSessionID(txtSessionID.getText().strip())) {
            connectToServer();
        } else {
            showAlert(Alert.AlertType.ERROR,null, "Session ID Failed", null, "Invalid Session ID");
        }
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                // get time before socket start
                startTime = System.currentTimeMillis();
                socket = new Socket("127.0.0.1", 9080);
                videoSocket = new Socket("127.0.0.1", 9081);
                audioSocket = new Socket("127.0.0.1", 9082);
                messageSocket = new Socket("127.0.0.1", 9083);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                sessionActive = true;
                Platform.runLater(() -> {
                    btnAbortSession.setDisable(false);
                    btnJoinSession.setDisable(true);
                    lblConnection.setText("Connected");
                    crlConnectionStatus.setStyle("-fx-fill: green;");
                });
                // Start receiving and displaying images
                startImageReceiver();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR,null,"Connection Issue",null,"Your connection is failed to connect");
                    lblConnection.setText("Connection Failed");
                    crlConnectionStatus.setStyle("-fx-fill: red;");
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

            // socket closed time
            endTime = System.currentTimeMillis();

            btnJoinSession.setDisable(false);
            btnAbortSession.setDisable(true);
            lblConnection.setText("Disconnected");
            crlConnectionStatus.setStyle("-fx-fill: red;");
            showAlert(Alert.AlertType.INFORMATION, null, "Session Ended", null, "The server has ended the session.");
        });

        try {
            if (socket != null && !socket.isClosed()) socket.close();
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

                // socket closed time
                endTime = System.currentTimeMillis();

                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                        videoSocket.close();
                        audioSocket.close();
                        Platform.runLater(() -> {
                            btnJoinSession.setDisable(false);
                            btnAbortSession.setDisable(true);
                            lblConnection.setText("Disconnected");
                            crlConnectionStatus.setStyle("-fx-fill: red;");
                            showAlert(Alert.AlertType.INFORMATION,null,"Abort session",null,"Session aborted");
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
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.FILE_SENDER).load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

    public void showAlert(Alert.AlertType alertType, String message, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void hBoxVideoOnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Stage stage = new Stage(StageStyle.UTILITY);
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.VIDEO_CALL).load());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

        // Access the shared controller
        VideoCallController videoCallController = VideoCallController.getInstance();

        if (videoSocket != null && !videoSocket.isClosed()) {
            new Thread(() -> {
                try {
                    sendVideoStream(videoSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            new Thread(() -> receiveVideoStream(videoSocket, videoCallController.imgVideoPreview)).start();
            new Thread(() -> startAudioStreaming(audioSocket)).start();
        }
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

    private void receiveVideoStream(Socket videoSocket, ImageView imgVideoPreview) {
        if (sessionActive) {
            Task<Image> task = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    InputStream is = videoSocket.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ObjectInputStream ois = new ObjectInputStream(bis);

                    while (!videoSocket.isClosed()) {
                        try{
                            byte[] bytes = (byte[]) ois.readObject();
                            updateValue(new Image(new ByteArrayInputStream(bytes)));
                        } catch (EOFException | SocketException e) {
                            System.out.println("Server has closed the connection");
                            break;
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            handleServerDisconnection();
                        }
                    }
                    return null;
                }
            };
            Platform.runLater(() -> {
                imgVideoPreview.imageProperty().bind(task.valueProperty());
            });
            new Thread(task).start();
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
                    System.err.println("Error in audio sending: " + e.getMessage());
                }
            }).start();

            // Start receiving and playing audio
            new Thread(() -> {
                try {
                    audioRecorder.startPlaying(audioSocket.getInputStream());
                } catch (IOException e) {
                    System.err.println("Error in audio receiving: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error in initializing audio recorder: " + e.getMessage());
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
        Scene scene = new Scene(SharedAppRouter.getContainer(SharedAppRouter.Routes.CONNECTION).load());
        stage.setScene(scene);
        stage.show();
        ConnectionController connectionController = ConnectionController.getInstance();
        connectionController.connect("127.0.0.1",InetAddress.getLocalHost().getHostAddress(),"9090",startTime+"");
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
                MessageController controller = loader.getController();
                controller.initialize(messageSocket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
