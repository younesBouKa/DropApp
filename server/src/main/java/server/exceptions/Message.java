package server.exceptions;

public enum Message {

    NO_PERMISSION("You don't have permission '%s' on resource '%s'"),

    EMPTY_PARAMS("Empty parameter '%s'"),
    EMPTY_RESULT("Empty result '%s'"),
    NULL_PARAMS("Null parameter '%s'"),
    UNKNOWN_EXCEPTION("Unknown exception '%s'"),

    NO_NODE_WITH_GIVEN_ID("No node with given id '%s'"),
    NO_NODE_WITH_GIVEN_PATH("No node with given path '%s'"),
    NO_NODE_WITH_GIVEN_PARENT_ID("No node with given parent id '%s'"),
    NO_NODE_WITH_GIVEN_PARENT_PATH("No node with given parent path '%s'"),

    NO_CONTENT_TYPE_FOR_NODE("No content type for this node '%s'"),
    NO_CONTENT_SIZE_FOR_NODE("No content size for this node '%s'"),
    DELETING_FOLDER_NODE("Node that you try to delete is folder '%s'"),

    ZIP_FILE_PATH_NOT_FOUND("Path to zip file not found '%s'"),
    ERROR_WHILE_ZIPPING_FILE("Error while zipping file '%s'"),

    FILE_ALREADY_EXISTS_WITH_SAME_NAME("File already exists with same name '%s'"),
    FILE_ALREADY_EXISTS_WITH_SAME_PATH("File already exists with same path '%s'"),

    NO_NODE_WITH_GIVEN_SOURCE_PATH("No node with given source path '%s'"),
    CANT_CREATE_NODE_WITH_GIVEN_DESTINATION_PATH("Can't create node with destination path '%s'"),
    CANT_COPY_FOLDER_TO_FILE("Can't copy folder to file '%s', '%s'"),
    CANT_CREATE_FILE_COPY("Can't create file copy '%s'"),
    CANT_CREATE_FOLDER_COPY("Can't create folder copy '%s'"),
    CANT_CREATE_FOLDER("Can't create folder '%s'"),
    CANT_CREATE_FOLDER_FROM_PATH("Can't create folder from path '%s'"),

    ERROR_WHILE_SAVING_FILE("Error while saving file '%s'"),
    ERROR_WHILE_READING_FILE_CONTENT("Error while reading file content '%s'"),
    NO_CONTENT_FOR_FILE_ID("No content for file with id '%s'"),

    ERROR_WHILE_STREAMING_FILE_CONTENT("Error while streaming file content '%s'"),

    ERROR_WHILE_DELETING_FILE("Error while deleting file '%s'"),
    // used in api v0
    SPACE_ALREADY_EXISTS_WITH_SAME_KEYS("Space already exists with same keys { name: '%s', ownerId: '%s'"),
    NODE_ALREADY_EXISTS_WITH_SAME_KEYS("Node already exists with same keys { name: '%s', ownerId: '%s', parentId: '%s'"),
    NO_SPACE_WITH_GIVEN_ID("No space with given id '%s'"),
    ERROR_WHILE_PARSING_NODE_INFO("Error while parsing node info '%s'"),

    // used in user security
    USERNAME_ALREADY_TAKEN("Username already taken '%s'"),
    EMAIL_ALREADY_TAKEN("Email already taken '%s'"),

    // file content
    INDEX_OUT_OF_BOUND("Index out of bound '%d' length '%d'"),
    ;

    private String message;
    private final int code;

    Message(String message) {
        this.message = message;
        this.code = this.ordinal();
    }

    public String getMessage() {
        return message;
    }

    public int getCode(){
        return this.code;
    }

    public String format(Object[] args) {
        if(args==null || args.length==0)
            this.message = this.message.replaceAll("%s","");
        else
            this.message = String.format(this.message, args);
        return this.message;
    }
}
