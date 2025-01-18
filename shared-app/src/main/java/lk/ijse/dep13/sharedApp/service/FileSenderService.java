package lk.ijse.dep13.sharedApp.service;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public interface FileSenderService {
    void sendFile(File file, Socket fileTransferSocket) throws IOException;
    String getFileExtension(String fileName);
    String getFileName(String fileName);
}
