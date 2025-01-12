open module server.app {
//    requires shared.app;
    requires transitive shared.app;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires webcam.capture;
}

