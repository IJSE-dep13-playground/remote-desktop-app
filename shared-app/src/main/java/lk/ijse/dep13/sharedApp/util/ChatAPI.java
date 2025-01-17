package lk.ijse.dep13.sharedApp.util;

import com.sun.source.tree.TryTree;
import lk.ijse.dep13.sharedApp.util.dto.Message;

import java.io.*;
import java.net.Socket;

public interface ChatAPI {
//    private Socket socket;
//    private ObjectOutputStream oos;
//    private ObjectInputStream ois;
//
//
//    public ChatAPI(Socket socket) throws IOException {
//        this.socket = socket;
//        this.oos = new ObjectOutputStream(socket.getOutputStream());
//        this.ois = new ObjectInputStream(socket.getInputStream());
//
//    }

    public void sendMessage(String message) throws IOException ;


    public String receiveMessage() throws IOException;

    public void close() throws IOException ;
    }

