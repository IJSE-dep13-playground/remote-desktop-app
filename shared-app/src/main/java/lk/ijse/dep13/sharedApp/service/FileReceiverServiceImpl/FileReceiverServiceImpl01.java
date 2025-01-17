package lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class FileReceiverServiceImpl01 implements FileReceiverService {
    @Override
    public void initialize(ServerSocket serverSocket, Socket localSocket) throws IOException {
        serverSocket=new ServerSocket(9080);
       localSocket=serverSocket.accept();

    }

    @Override
    public void recieveFileFromServer(TextArea txtAreaRecievedFiles, Socket localSocket) throws IOException, ClassNotFoundException {

            InputStream is = localSocket.getInputStream();
            ObjectInputStream ois=new ObjectInputStream(is);
            File file=(File) ois.readObject();
            String ext=(String)ois.readObject();
            String fileName=(String)ois.readObject();
            //file.createNewFile();

            String separator = Paths.get("").getFileSystem().getSeparator();
            File fileCopy=new File(System.getProperty("user.home"),"transferredFiles"+separator+fileName+"."+ext);
            //  String path= Files.getPA
            //fileCopy.createNewFile();

            FileInputStream fis=new FileInputStream(file);
            FileOutputStream fos=new FileOutputStream(fileCopy);
            while(true){
                byte[] buffer=new byte[1024];
                int read=fis.read(buffer);
                if(read==-1)break;
                fos.write(buffer,0,read);
                // fos.flush();
            }
            fos.flush();
            fos.close();
            txtAreaRecievedFiles.setText("files copied succesfully");
            fos.close();
        }
    }



