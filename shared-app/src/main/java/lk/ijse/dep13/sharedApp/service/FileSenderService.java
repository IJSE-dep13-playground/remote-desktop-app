package lk.ijse.dep13.sharedApp.service;

import java.io.File;
import java.io.IOException;

public interface FileSenderService {
    void sendFileToServer(File file) throws IOException;
    public String getFileExtension(String fileName);
}
