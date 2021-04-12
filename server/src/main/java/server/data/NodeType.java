package server.data;

public enum NodeType {
    FOLDER("folder",0b00001),
    FILE("file",0b00010),
    LINK("link",0b00100);

    String name;
    int code;

    NodeType(String name, int code){
        this.name= name;
        this.code= code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
