package lk.ijse.dep13.control;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteDesktopServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9080)) {
            System.out.println("Server started. Waiting for connection...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            Robot robot = new Robot();
            while (true) {
                // Capture the screen
                BufferedImage screen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screen, "png", baos);
                oos.writeObject(baos.toByteArray());
                oos.flush();

                // Receive mouse coordinates
                if (ois.available() > 0) {
                    Point mousePoint = (Point) ois.readObject();
                    robot.mouseMove(mousePoint.x, mousePoint.y);
                }
                Thread.sleep(1000 / 30); // 30 FPS
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
