package server.exceptions;

import server.tools.MessageCode;

public class CustomException extends Exception {

    private static final long serialVersionUID = 1L;
    private final MessageCode messageCode;

    public CustomException(Message messageObj, Object... args) {
        super();
        messageObj.format(args);
        this.messageCode = new MessageCode(messageObj.getMessage(), messageObj.getCode());
    }

    public String getMessage() {
        return this.messageCode.getMessage();
    }

    public int getCode(){
        return this.messageCode.getCode();
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }
}