package lk.ijse.dep13.sharedApp.service.FileSenderServiceImpl;

import lk.ijse.dep13.sharedApp.service.FileSenderService;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

public class FileSenderService_OOS implements FileSenderService {

    private ObjectOutputStream oos;

    public FileSenderService_OOS(ObjectOutputStream oos){
        this.oos=oos;
    }

    @Override
    public void sendFile(File file,Socket fileTransferSocket) throws IOException {

        String name = file.getName();
        oos.writeObject(file);
        oos.writeObject(getFileExtension(name));
        oos.writeObject(getFileName(name));
        oos.flush();
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
