package lk.ijse.dep13.sharedApp.util;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioRecorder {
    private volatile boolean stop = false;
    private AudioFormat format;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private ByteArrayOutputStream buffer;

    public AudioRecorder() throws LineUnavailableException {
        format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(targetInfo);
        speakers = (SourceDataLine) AudioSystem.getLine(sourceInfo);
        microphone.open(format);
        speakers.open(format);
    }

    public void startRecording(OutputStream outputStream) {
        new Thread(() -> {
            try {
                microphone.start();
                byte[] data = new byte[microphone.getBufferSize() / 5];
                while (!stop) {
                    int read = microphone.read(data, 0, data.length);
                    outputStream.write(data, 0, read);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                microphone.stop();
                microphone.close();
            }
        }).start();
    }

    public void startPlaying(InputStream inputStream) {
        new Thread(() -> {
            try {
                speakers.start();
                byte[] data = new byte[4096];
                int read;
                while ((read = inputStream.read(data)) != -1 && !stop) {
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

    public void stop() {
        stop = true;
    }
}
