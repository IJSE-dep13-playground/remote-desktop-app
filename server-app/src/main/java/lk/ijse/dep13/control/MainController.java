package lk.ijse.dep13.control;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class MainController {
    public VBox vBoxNavBar;
    public HBox hBoxVideo;
    public HBox hBoxChat;
    public HBox hBoxConnection;
    public HBox hBoxDeskMe;
    public Circle crlConnectionStatus;
    public Label lblConnection;
    public Label lblWelcome;
    public HBox hBoxSettings;
    public Pane pnHome;
    public Button btnCreateSession;
    public Button btnAbortSession;
    public Pane pnSession;
    public ImageView imgPreview;

    public void btnCreateSessionOnAction(ActionEvent actionEvent) {
    }

    public void btnAbortSessionOnAction(ActionEvent actionEvent) {
    }
}
