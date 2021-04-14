package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;

@Document(collection = "node")
public class Node {

    public static final String SEPARATOR = "/";
    public static final String ROOT_PATH = "/";

    @Id
    String id;
    String path;
    Permission permission;
    String parentId;
    Date createDate;
    NodeType type;
    String name;
    String fileId;

    public Node(){
        this(Node.ROOT_PATH,Permission.READ_WRITE,null,new Date(),NodeType.FOLDER,"root");
    }

    public Node(String path, Permission permission, String parentId, Date createDate, NodeType type, String name) {
        this.path = path;
        this.permission = permission;
        this.parentId = parentId;
        this.createDate = createDate;
        this.type = type;
        this.name = name;
    }

    public Update createUpdateObj(){
        Update update = new Update();
        update.set("parentId", this.getParentId())
                .set("permission", this.getPermission())
                .set("createDate", this.getCreateDate())
                .set("path", this.getPath())
                .set("type", this.getType())
                .set("name", this.getName())
        ;
        return update;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public NodeType getType() {
        return this.type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId(){
        return this.fileId;
    }

    public static String getDefaultFolderName(){
        return "folder_"+new Date().getTime();
    }

    public static String getDefaultFileName(){
        return "file_"+new Date().getTime();
    }

    public String getParentFolderPath(){
        return getPath().substring(0,getPath().lastIndexOf(SEPARATOR));
    }

    public boolean isFolder() {
        return this.getType() == NodeType.FOLDER;
    }

    public boolean isFile() {
        return this.getType() == NodeType.FILE;
    }

    public boolean isLink() {
        return this.getType() == NodeType.LINK;
    }
}