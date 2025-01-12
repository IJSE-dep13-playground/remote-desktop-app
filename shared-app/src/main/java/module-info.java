open module shared.app {
    exports lk.ijse.dep13.sharedApp.util; // Make AppRouter accessible
//    opens sharedScene to javafx.fxml;
    requires javafx.fxml;
    requires javafx.controls;
}