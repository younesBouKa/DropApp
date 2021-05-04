package server.data;

import server.tools.tools;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FileVersion {

    private String fileId;
    private String userId;
    private Instant modifiedAt;
    private String originalName;
    private long fileSize;
    private String contentType;
    private String extension;
    private String versionHash;
    private String contentHash;
    private Map<String, Object> fields = new HashMap<>();

    public FileVersion(){
        this.modifiedAt = Instant.now();
    }

    public FileVersion(String fileId, String userId) {
        this.fileId = fileId;
        this.userId = userId;
        this.modifiedAt = Instant.now();
    }

    public FileVersion(String fileId, String userId, String originalName, long fileSize, String contentType, String extension, Map<String, Object> fields) {
        this(fileId, userId);
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.extension = extension;
        this.fields = fields;
    }

    public FileVersion(String fileId, String userId, String originalName, long fileSize, String contentType, String extension, Map<String, Object> fields, String contentHash) {
        this(fileId, userId, originalName, fileSize, contentType, extension, fields);
        this.contentHash = contentHash;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType!=null ? contentType : "";
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getVersionHash() {
        return hashVersion().versionHash;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public FileVersion hashVersion(){
        versionHash = tools.hash(getContentType()
                +getExtension()
                +getFileSize()
                +getContentHash()
                +getOriginalName()
                +getUserId()
        );
        return this;
    }
}
