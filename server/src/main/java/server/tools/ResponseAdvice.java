package server.tools;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import server.exceptions.CustomException;

import java.util.logging.Logger;


@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Logger logger = Logger.getLogger(ResponseAdvice.class.getName());
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if(body instanceof ResponseWrapper) // if already an exception was wrapped, just return it
            return body;
        final ResponseWrapper<Object> output = new ResponseWrapper<>();
        output.setData(body); // data
        output.setDevMessage(null); // no message for dev
        output.setHttpCode(200);
        output.setOk(true);
        output.setUserMessage(null); // no message for user
        return output;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper> handleAll(Exception ex, RedirectAttributes redirectAttributes) {
        final ResponseWrapper<MessageCode> output = new ResponseWrapper<>();
        MessageCode messageCode;
        if (ex instanceof CustomException){ // if exception was wrapped
            CustomException exception = ((CustomException) ex);
            messageCode = exception.getMessageCode(); // get error to send in body (code + formatted message)
            if(exception.getDevException()!=null) // if no known exception was wrapped
                output.setDevMessage(exception.getDevException().toString()); // put it in devMessage
            else
                output.setDevMessage(messageCode.getMessage()); // else just put formatted message
        }
        else{ // if exception was not wrapped
            messageCode = new MessageCode("Technical error!", -1); // error in body will be technical error
            output.setDevMessage(ex.toString()); // put exception in devMessage
        }
        ex.printStackTrace();
        output.setUserMessage(messageCode.getMessage()); // formatted message
        output.setData(messageCode); // error in body
        output.setHttpCode(200); // always send response
        output.setOk(false);
        return new ResponseEntity<ResponseWrapper>(output, new HttpHeaders(), HttpStatus.OK);
    }
}