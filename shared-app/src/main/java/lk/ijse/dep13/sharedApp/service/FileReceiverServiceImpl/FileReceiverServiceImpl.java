package lk.ijse.dep13.sharedApp.service.FileReceiverServiceImpl;

import javafx.scene.control.TextField;
import lk.ijse.dep13.sharedApp.service.FileReceiverService;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReceiverServiceImpl implements FileReceiverService {


    @Override
    public void receiveFile(Socket fileTransferSocket, TextField downloadLocation) throws IOException, ClassNotFoundException {

            InputStream is = fileTransferSocket.getInputStream();
            ObjectInputStream ois=new ObjectInputStream(is);
            File file=(File) ois.readObject();
            String ext=(String)ois.readObject();
            String fileName=(String)ois.readObject();
            //file.createNewFile();

            String separator = Paths.get("").getFileSystem().getSeparator();
            File saveDir=new File(System.getProperty("user.home"),"Desktop/transferredFiles");
            if(!saveDir.exists()){
                saveDir.mkdir();
            }

            File fileCopy=new File(saveDir,separator+fileName+"."+ext);
           // fileCopy.createNewFile();
            downloadLocation.setText(fileCopy.getAbsolutePath());
            //  String path= Files.getPA
            //fileCopy.createNewFile();

            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(fileCopy);
            while(true){
                byte[] buffer = new byte[1024];
                int read = fis.read(buffer);
                if(read == -1) break;
                fos.write(buffer,0,read);
            }
            fos.flush();
            fos.close();
        }
    }



