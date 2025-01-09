package lk.ijse.dep13.contoller;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TextScreenController  {
    public TextArea txtScreen;
    public TextArea txtAreaHistory;
    String txtHistory="";

    public void startText(){


    }

    public void keyPressed(KeyEvent keyEvent) throws IOException {
        ServerSocket serverSocket=new ServerSocket(9090);

        if(keyEvent.getCode()== KeyCode.ENTER){

            String deliverMsg=txtScreen.getText();
            txtHistory+=deliverMsg;
            txtAreaHistory.setText(txtHistory);
            txtScreen.clear();
            Socket localSocket=serverSocket.accept();
            OutputStream oos=localSocket.getOutputStream();
            oos.write(deliverMsg.getBytes());
            oos.flush();
            txtScreen.setText("message delivered");
        }
    }
}

