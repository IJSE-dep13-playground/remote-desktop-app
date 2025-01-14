package lk.ijse.dep13.sharedApp.util;

import javafx.fxml.FXMLLoader;

public class SharedAppRouter {
    public enum Routes{
        FILE_SENDER,VIDEO_CALL,ABOUT, CONNECTION, MESSAGE
    }
    public static FXMLLoader getContainer(Routes route){
        FXMLLoader fxmlLoader = null;
        if (route == Routes.FILE_SENDER){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/FileSender.fxml"));
        } if (route == Routes.VIDEO_CALL){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/VideoCall.fxml"));
        } if (route == Routes.ABOUT){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/About.fxml"));
        } if (route == Routes.CONNECTION){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/Connection.fxml"));
        } if (route == Routes.MESSAGE){
            fxmlLoader = new FXMLLoader(SharedAppRouter.class.getResource("/sharedScene/Message.fxml"));
        }
        return fxmlLoader;
    }
}
