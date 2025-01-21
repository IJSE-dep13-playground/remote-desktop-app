package lk.ijse.dep13.sharedApp.service;

import lk.ijse.dep13.sharedApp.util.dto.Message;

import java.io.*;
import java.net.Socket;

public class ChatAPI{
    private Socket socket;
    private ObjectOutputStream oos;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectInputStream ois;

    public ChatAPI(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    public String receiveMessage() throws IOException {
        return reader.readLine();
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
