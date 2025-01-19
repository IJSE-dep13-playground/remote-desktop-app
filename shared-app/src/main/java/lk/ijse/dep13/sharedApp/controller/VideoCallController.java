package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.dep13.sharedApp.service.VideoAPI;
import lk.ijse.dep13.sharedApp.service.videoApiImpl.VideoApiImpl;

import java.net.Socket;
import java.util.Optional;

public class VideoCallController {
    public ImageView imgVideoPreview;
    public ImageView imgCallDisconnect;

    private VideoAPI videoAPI;

    public void initialize(Socket socket) {
        try{
            this.videoAPI = new VideoApiImpl(socket);

            // Receiving video in a separate thread
            new Thread(() -> {
                try {
                    videoAPI.receiveVideo(imgVideoPreview);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Sending video in a separate thread
            new Thread(() -> {
                try {
                    videoAPI.sendVideo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void imgCallDisconnectOnMouseClicked(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("End Call");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to disconnect the video call?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    videoAPI.close();
                    Platform.runLater(() -> {
                        Stage stage = (Stage) imgCallDisconnect.getScene().getWindow();
                        stage.close();
                    });
                } catch (Exception e) {
                    System.out.println("Error disconnecting video call: " + e.getMessage());
                }
            }
        });
    }
}
