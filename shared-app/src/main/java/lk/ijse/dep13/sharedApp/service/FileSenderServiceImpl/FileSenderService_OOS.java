package lk.ijse.dep13.sharedApp.service.FileSenderServiceImpl;

import lk.ijse.dep13.sharedApp.service.FileSenderService;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

public class FileSenderService_OOS implements FileSenderService {
    @Override
    public void sendFileToServer(File file,Socket fileTransferSocket) throws IOException {


        OutputStream os = fileTransferSocket.getOutputStream();


        // Send the file data
        FileInputStream fis = new FileInputStream(file);
        String name = file.getName();

        ObjectOutputStream oos=new ObjectOutputStream(os);
        oos.writeObject(file);
        oos.writeObject(getFileExtension(name));
        oos.writeObject(getFileName(name));
        oos.flush();
        oos.close();
    }

    @Override
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    @Override
    public String getFileName(String fileName) {

        String separator = Paths.get("").getFileSystem().getSeparator();

        String[] parts = fileName.split(separator);


        String fileNameWithoutExtension = parts[parts.length - 1].split("\\.")[0];
        return fileNameWithoutExtension;
    }
}
