package lk.ijse.dep13.sharedApp.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.net.URI;

public class AboutController {

    public Label lblH1;
    public Label lblVersion;
    public Label lblReleased;
    public Button btnGitHub;
    public TextArea txtFieldDescript;
    public Label lblLicense;
    public Pane pnAbout;
    public AnchorPane root;

    public void lblLicenseOnMouseClicked(MouseEvent mouseEvent) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/IJSE-dep13-playground/remote-desktop-app/blob/main/license.txt"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void btnGitHubOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/IJSE-dep13-playground/remote-desktop-app"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
