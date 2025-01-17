package lk.ijse.dep13.sharedApp.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;
import lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl.FileReceiverServiceImpl01;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiverController {

    public TextArea txtAreaRecievedFiles;
    ServerSocket serverSocket;
    Socket localSocket;

    FileReceiverService fileReceiverService = new FileReceiverServiceImpl01();

    public void initialize() {
//txtAreaRecievedFiles.setText("jhjjhh");

    }

    private void recievedFromClient() {

    }

    public void btnOpenSocketOnAction(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9898);
                System.out.println(22);
                Platform.runLater(() -> {
                    txtAreaRecievedFiles.setText("waiting for the client to send files");
                });


                localSocket = serverSocket.accept();
                System.out.println(33);
                Platform.runLater(() -> {
                    txtAreaRecievedFiles.setText("client connected");
                });

            } catch (Exception e) {
            }
        }).start();

//        try {

//            txtAreaRecievedFiles.setText("waiting for the server to connect");
//            fileReceiverService.initialize(serverSocket, localSocket);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void btnRecieveOnAction(ActionEvent actionEvent) {
        try {
            InputStream is = localSocket.getInputStream();
          ObjectInputStream ois=new ObjectInputStream(is);
            File file=(File) ois.readObject();
            String ext=(String)ois.readObject();
            //file.createNewFile();

            File fileCopy=new File(System.getProperty("user.home"),"fileCopy"+"."+ext);
            //fileCopy.createNewFile();

           FileInputStream fis=new FileInputStream(file);
           FileOutputStream fos=new FileOutputStream(fileCopy);
           while(true){
               byte[] buffer=new byte[1024];
               int read=fis.read(buffer);
               if(read==-1)break;
               fos.write(buffer,0,read);
              // fos.flush();
           }
           fos.flush();
           fos.close();
            txtAreaRecievedFiles.setText("files copied succesfully");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
