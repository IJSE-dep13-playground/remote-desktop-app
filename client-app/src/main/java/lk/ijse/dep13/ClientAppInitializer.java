package lk.ijse.dep13;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.dep13.util.AppRouter;

import java.io.IOException;

public class ClientAppInitializer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Scene scene = new Scene(AppRouter.getContainer(AppRouter.Routes.TEXTSCREENCLIENT).load());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("text screen client");
        primaryStage.centerOnScreen();

    }
}
