package server.models;


import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import server.exceptions.CustomException;

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
    private Map<String, Object> fields = new HashMap<>();
    private File file;

    public ZipRequest(){
        this.fields.put("from","WEB");
    }

    public InputStream getFileContent() throws CustomException {
        try {
            return new FileInputStream(getFile());
        }catch (IOException e){
            return null;
        }
    }

    public String getContentType(){
        try {
            return URLConnection.getFileNameMap().getContentTypeFor(getFile().getName());
        }catch (Exception e){
            return "";
        }
    }

    public String getExtension(){
        return getFile()!=null ? FilenameUtils.getExtension(getFile().getName()) : "";
    }

    public long getFileSize(){
        try {
            return Files.size(getFile().toPath());
        }catch (Exception e){
            return 0;
        }
    }

    public String getName() {
        return name;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<String> getNodesId() {
        return nodesId;
    }

    public void setNodesId(List<String> nodesId) {
        this.nodesId = nodesId;
    }
}
