package lk.ijse.dep13.util;

import javafx.fxml.FXMLLoader;

public class AppRouter {
    public enum Routes{
        MAIN,SPLASH,FILERECIEVER
    }
    public static FXMLLoader getContainer(Routes route){
        FXMLLoader fxmlLoader = null;
        if (route == Routes.MAIN){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/MainScene.fxml"));
        } if (route == Routes.SPLASH){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/SplashScreen.fxml"));
        }
        if (route == Routes.FILERECIEVER){
            fxmlLoader = new FXMLLoader(AppRouter.class.getResource("/scene/SplashScreen.fxml"));
        }


        return fxmlLoader;
    }
}
