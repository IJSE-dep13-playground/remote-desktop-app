package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.stage.WindowEvent;
import lk.ijse.dep13.sharedApp.service.videoApiImpl.VideoApiImpl;


import java.io.IOException;
import java.net.Socket;

public class VideoCallController {
    public ImageView imgVideoPreview;
    public ImageView imgCallDisconnect;
    public VideoApiImpl videoApi;

    public void initialize(Socket videoSocket) throws IOException{
        System.out.println("Initializing video call...");
        new Thread(() -> {
            try {
                videoApi = new VideoApiImpl(videoSocket);
                videoApi.sendVideo();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

//        Platform.runLater(() -> {videoApi.receiveVideo(imgVideoPreview);});
    }

    public void handleCloseRequest(WindowEvent event)  {
        if (videoApi != null) {
            videoApi.close();
        }
    }

    public void imgCallDisconnectOnMouseClicked(MouseEvent mouseEvent) {
        if (videoApi != null) {
            videoApi.close();
        }
    }
}
