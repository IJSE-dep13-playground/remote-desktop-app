package lk.ijse.dep13.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Message_Server_Controller {
    public TextField txtInput;
    public TextArea txtAreaChat;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    Thread oisThread;
    public Message_Server_Controller(ObjectOutputStream oos, ObjectInputStream ois){
        this.oos=oos;
        this.ois=ois;
      oisThread =  new Thread(
                ()->{

                    while (true){
                        try {
                            Message message=(Message) ois.readObject();
                            Platform.runLater(()->{
                                txtAreaChat.appendText("Client: " + message + "\n");

                            });
                        } catch (IOException e) {
                            txtAreaChat.appendText(e.getMessage());
                        } catch (ClassNotFoundException e) {
                           txtAreaChat.appendText(e.getMessage());
                        }
                    }
                }
        );
      oisThread.start();
    }
    //  Platform.runLater(() -> txtAreaChat.appendText("Client: " + message + "\n"));

    public void btnSendOnAction(ActionEvent actionEvent) throws IOException {
            String message=txtInput.getText();
            oos.writeObject(new Message(message));
            oos.flush();
    }


}
