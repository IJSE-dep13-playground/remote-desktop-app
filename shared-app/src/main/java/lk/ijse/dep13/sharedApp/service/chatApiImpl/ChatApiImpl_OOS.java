package lk.ijse.dep13.sharedApp.service.chatApiImpl;

import lk.ijse.dep13.sharedApp.service.ChatAPI;
import lk.ijse.dep13.sharedApp.util.dto.Message;

import java.io.*;
import java.net.Socket;

public class ChatApiImpl_OOS implements ChatAPI {
    private Socket socket;
    private ObjectOutputStream oos;
    private BufferedReader in;
    private BufferedWriter out;
    private ObjectInputStream ois;

    public ChatApiImpl_OOS(Socket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(String message) throws IOException {
        Message reply = new Message(message);
        oos.writeObject(reply);
        oos.flush();
    }

    public String receiveMessage() throws IOException {
        try {
            Message reply = (Message) ois.readObject();
            return (reply.message());
        } catch (Exception e) {
            return "error";
        }
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
