package lk.ijse.dep13.sharedApp.service.audioApiImpl;

import lk.ijse.dep13.sharedApp.service.AudioAPI;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AudioApiImpl implements AudioAPI{

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private boolean stop = false;
    private AudioFormat format;
    private TargetDataLine microphone;
    private SourceDataLine speakers;

    public AudioApiImpl(Socket socket) {
        this.socket = socket;
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());

            format = new AudioFormat(44100, 16, 2, true, false);
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(targetInfo);
            speakers = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            microphone.open(format);
            speakers.open(format);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendAudio() {
        new Thread(() -> {
            try {
                microphone.start();
                byte[] data = new byte[microphone.getBufferSize() / 5];
                while (!stop) {
                    int read = microphone.read(data, 0, data.length);
                    oos.write(data, 0, read);
                    oos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                microphone.stop();
                microphone.close();
            }
        }).start();
    }

    @Override
    public void receiveAudio() {
        new Thread(() -> {
            try {
                speakers.start();
                byte[] data = new byte[4096];
                int read;
                while ((read = ois.read(data)) != -1 && !stop) {
                    speakers.write(data, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                speakers.stop();
                speakers.close();
            }
        }).start();
    }

    @Override
    public void stopAudio() {

    }
}
