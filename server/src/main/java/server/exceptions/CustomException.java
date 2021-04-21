package server.exceptions;

import server.tools.MessageCode;

public class CustomException extends Exception {

    private static final long serialVersionUID = 1L;
    private final Message messageObject; // for message formatting
    private final MessageCode messageCode; // for response body
    private final Exception devException; // for developer debugging

    public CustomException(Message messageObj, Object... args) {
        this(null, messageObj, args);
    }

    public CustomException(Exception ex, Message messageObj, Object... args) {
        super();
        this.devException = ex; // wrapped exception
        this.messageObject = messageObj; // message enum
        this.messageObject.format(args); // format message with args
        this.messageCode = new MessageCode(messageObj.getMessage(), messageObj.getCode()); // creating error response body
    }

    public String getMessage() {
        return this.messageObject.getMessage();
    }

    public int getCode(){
        return this.messageObject.getCode();
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }

    public Message getMessageObject() {
        return messageObject;
    }

    public Exception getDevException() {
        return devException;
    }
}