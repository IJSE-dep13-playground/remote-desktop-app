package lk.ijse.dep13.util;

import javafx.fxml.FXMLLoader;

public class AppRouter {
    public enum Routes{
        MAIN,SPLASH,TEXTSCREEN
    }
    public static FXMLLoader getContainer(Routes route){
        FXMLLoader fxmlLoader = null;
        if (route == Routes.MAIN){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/MainScene.fxml"));
        } if (route == Routes.SPLASH){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/SplashScreen.fxml"));
        } if (route == Routes.TEXTSCREEN){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/TextScreen.fxml"));
        }return fxmlLoader;
    }
}
