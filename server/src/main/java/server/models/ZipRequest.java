package server.models;


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZipRequest implements Serializable {

    private String name;
    private String parentId;
    private List<String> nodesId;
    private byte[] content;
    private String originalName;
    private long fileSize;
    private String extension;
    private String contentType;
    private Map<String, Object> fields = new HashMap<>();

    public ZipRequest(){
        this.fields.put("from","WEB");
    }

    public void updateWithFile(File file){
        if(file==null || !file.canRead())
            return;
        try{
            setFileSize(Files.size(file.toPath()));
            try (InputStream in = new FileInputStream(file)){
                setContent(IOUtils.toByteArray(in));
            }
            setOriginalName(file.getName());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getContentType(){
        try {
            return contentType!=null? contentType : URLConnection.getFileNameMap().getContentTypeFor(getOriginalName());
        }catch (Exception e){
            return "";
        }
    }

    public String getExtension(){
        return extension!=null? extension : FilenameUtils.getExtension(getOriginalName());
    }

    public long getFileSize(){
        return content!= null ? content.length :  fileSize;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName!=null ? originalName : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public List<String> getNodesId() {
        return nodesId;
    }

    public void setNodesId(List<String> nodesId) {
        this.nodesId = nodesId;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
