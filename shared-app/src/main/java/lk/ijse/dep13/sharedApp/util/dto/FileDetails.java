package lk.ijse.dep13.sharedApp.util.dto;

import java.io.Serializable;

public class FileDetails implements Serializable {
    private byte[] content;
    private String extension;

    public FileDetails(byte[] content, String extension) {
        this.content = content;
        this.extension = extension;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
