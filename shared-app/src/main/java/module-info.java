open module shared.app {
    exports lk.ijse.dep13.sharedApp.util;
    exports lk.ijse.dep13.sharedApp.controller;// Make AppRouter accessible
//    opens sharedScene to javafx.fxml;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires jdk.compiler;
    requires bridj;
}