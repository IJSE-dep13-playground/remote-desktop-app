package lk.ijse.dep13.sharedApp.service.videoApiImpl;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import lk.ijse.dep13.sharedApp.service.VideoAPI;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class VideoApiImpl implements VideoAPI {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Webcam webcam;
    private boolean running = true;

    public VideoApiImpl(Socket videoSocket) throws IOException {
        this.socket = videoSocket;
        this.oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())) ;
        this.webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
        } else {
            throw new IOException("Webcam could not be opened");
        }
    }

    public void sendVideo() {
        new Thread(() -> {
            System.out.println("Sending video");
        while(running) {
                try {
                    BufferedImage bufferedImage = webcam.getImage();
                    if (bufferedImage != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "jpg", baos);
                        byte[] imageBytes = baos.toByteArray();
                        oos.writeInt(imageBytes.length);
                        oos.write(imageBytes);
                        oos.flush();
                    }
                    Thread.sleep(1000/30);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Video sending error: " + e.getMessage());
                        running = false;
                    }
                }
        }).start();
    }

    public void receiveVideo(ImageView videoPreview) {
       Task<Image> task = new Task<>() {
           @Override
           protected Image call() throws Exception {
               while(true) {
                   try{
                       int length = ois.readInt();
                       byte[] bytes = new byte[length];
                       ois.readFully(bytes);
                       Image image = new Image(new ByteArrayInputStream(bytes));
                       updateValue(image);
                   } catch (IOException e) {
                       System.err.println("Video receiving error: " + e.getMessage());
                       running = false;
                       break;
                   }
               }
               return null;
           }
       };
       videoPreview.imageProperty().bind(task.valueProperty());
       new Thread(task).start();
    }

    public void close() {
        running = false;
        try {
            if (webcam != null) {
                webcam.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
