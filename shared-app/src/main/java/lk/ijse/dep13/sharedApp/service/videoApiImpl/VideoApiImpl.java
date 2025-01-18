package lk.ijse.dep13.sharedApp.service.videoApiImpl;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lk.ijse.dep13.sharedApp.service.VideoAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class VideoApiImpl implements VideoAPI {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public VideoApiImpl(Socket socket) {
        this.socket = socket;
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendVideo() {
        Webcam webcam = null;
            try {
                webcam = Webcam.getDefault();
                webcam.open();
                OutputStream os = socket.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                ObjectOutputStream oos = new ObjectOutputStream(bos);

                while (!socket.isClosed()) {
                    BufferedImage bufferedImage = webcam.getImage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage,"jpeg",baos);

                    oos.writeObject(baos.toByteArray());
                    oos.flush();
                    Thread.sleep(1000/30);
                }
            } catch (Exception e) {
                System.out.println("Error during video streaming: " + e.getMessage());
            } finally {
                if (webcam != null && webcam.isOpen()) {
                    webcam.close();
                }
            }
        }

    @Override
    public void receiveVideo(ImageView videoPreview) {
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try (InputStream is = socket.getInputStream();
                     BufferedInputStream bis = new BufferedInputStream(is);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {

                    while (!socket.isClosed() && !isCancelled()) {
                        try {
                            byte[] bytes = (byte[]) ois.readObject();
                            updateValue(new Image(new ByteArrayInputStream(bytes)));
                        } catch (EOFException | SocketException e) {
                            System.out.println("Server has closed the connection");
                            break;
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        };
        Platform.runLater(() -> videoPreview.imageProperty().bind(task.valueProperty()));
        new Thread(task).start();
    }

    @Override
    public void close() {
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing video resources: " + e.getMessage());
        }
    }
}
