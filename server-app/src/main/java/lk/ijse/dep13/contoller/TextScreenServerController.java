package lk.ijse.dep13.contoller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.ECField;

public class TextScreenServerController {
    public TextArea txtScreen;
    public TextArea txtAreaHistory;
    public Button btnStartServer;
    String txtHistory = "";
    ServerSocket serverSocket;
    Socket localSocket;
    OutputStream os;
    private volatile boolean connectionEstabilished=false;


    public void initialize() throws IOException {
        serverSocket = new ServerSocket(9090);
        txtAreaHistory.setText("waiting for the client to connect...");

        new Thread(()->{
          try{
              localSocket = serverSocket.accept();
              connectionEstabilished=true;
            os=localSocket.getOutputStream();
            txtAreaHistory.setText("client is online and connected");
            txtScreen.setDisable(false);
            txtScreen.requestFocus();
            txtAreaHistory.setEditable(false);
            txtScreen.setDisable(true);}catch (Exception e){}
        }).start();

        if(connectionEstabilished){
            System.out.println("connection establoshe");
           Task<String> task = new Task<>(){
               @Override
               protected String call() throws Exception {
                   InputStream is=localSocket.getInputStream();
                   while(true){
                       int read=is.read();
                       if(read==-1) break;
                      txtHistory+=(char)read+"";
                       updateValue(txtHistory);

                   }
                   return "connection lost";
               }
           };
           new Thread(task).start();;
           txtAreaHistory.textProperty().bind(task.valueProperty());
        }else{
            System.out.println("fail");
        }

    }


    public void keyPressed(KeyEvent keyEvent) throws IOException, InterruptedException {


        if (keyEvent.getCode() == KeyCode.ENTER) {
            String txt = txtScreen.getText();
            txtScreen.clear();
            os.write(txt.getBytes());
            os.flush();
        }

    }

    public void txtScreenOnMouseCLicked(MouseEvent mouseEvent) {
        Task<String> task = new Task<>(){
            @Override
            protected String call() throws Exception {
                InputStream is=localSocket.getInputStream();
                while(true){
                    int read=is.read();
                    if(read==-1) break;
                    txtHistory+=(char)read+"";
                    updateValue(txtHistory);

                }
                return "connection lost";
            }
        };
        new Thread(task).start();;
        txtAreaHistory.textProperty().bind(task.valueProperty());
        txtScreen.clear();
    }



    public void btnReStartServerOnAction(ActionEvent actionEvent) {
       try{ localSocket = serverSocket.accept();
        os=localSocket.getOutputStream();
        txtAreaHistory.setText("client is online and connected");
        txtScreen.setDisable(false);
        txtScreen.requestFocus();
        txtAreaHistory.setEditable(false);
        txtScreen.setDisable(true);
    }catch (Exception e){
           txtAreaHistory.setText("error occured while trying to start the server.");
       }}
}

