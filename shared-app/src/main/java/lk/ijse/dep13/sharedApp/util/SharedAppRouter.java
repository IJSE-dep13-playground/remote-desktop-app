package lk.ijse.dep13.sharedApp.util;

import javafx.fxml.FXMLLoader;

public class SharedAppRouter {
    public enum Routes{
        FILE_SENDER
    }
    public static FXMLLoader getContainer(Routes route){
        FXMLLoader fxmlLoader = null;
        if (route == Routes.FILE_SENDER){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/FileSender.fxml"));
        } return fxmlLoader;
    }
}
