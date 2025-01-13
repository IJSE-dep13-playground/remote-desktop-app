package lk.ijse.dep13.sharedApp.controller;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class VideoCallController {
    public ImageView imgVideoPreview;
    public ImageView imgCallDisconnect;

    private static VideoCallController instance;

    public VideoCallController() {
        instance = this;
    }

    public static VideoCallController getInstance() {
        return instance;
    }

    public void imgCallDisconnectOnMouseClicked(MouseEvent mouseEvent) {

    }
}
