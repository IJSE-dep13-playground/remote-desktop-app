package lk.ijse.dep13.sharedApp.service.chatAPIImpl;

import lk.ijse.dep13.sharedApp.service.ChatAPI;

import java.io.*;
import java.net.Socket;

public class ChatAPIService implements ChatAPI {

    private BufferedReader reader;
    private BufferedWriter writer;


    public ChatAPIService(BufferedWriter bw,BufferedReader br) throws IOException {
        this.reader = br;
        this.writer = bw;
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    public String receiveMessage() throws IOException {
        return reader.readLine();
    }
}
