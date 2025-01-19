package lk.ijse.dep13.sharedApp.service;

import java.io.*;

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
    void sendMessage(String message) throws IOException ;

    String receiveMessage() throws IOException;

    void close() throws IOException ;
    }

