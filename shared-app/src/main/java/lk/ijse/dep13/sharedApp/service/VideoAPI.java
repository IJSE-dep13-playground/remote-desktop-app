package lk.ijse.dep13.sharedApp.service;

import javafx.scene.image.ImageView;

public interface VideoAPI {
    void sendVideo();
    void receiveVideo(ImageView videoPreview);
    void close();
}
