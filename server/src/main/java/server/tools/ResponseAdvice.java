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


@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if(body instanceof ResponseWrapper)
            return body;
        final ResponseWrapper<Object> output = new ResponseWrapper<>();
        output.setData(body);
        output.setDevMessage(null);
        output.setHttpCode(200);
        output.setOk(true);
        output.setUserMessage(null);
        return output;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper> handleAll(Exception ex, RedirectAttributes redirectAttributes) {
        final ResponseWrapper<MessageCode> output = new ResponseWrapper<>();
        MessageCode messageCode;
        if (ex instanceof CustomException)
            messageCode = ((CustomException) ex).getMessageCode();
        else
            messageCode = new MessageCode("Technical error!", -1);
        output.setUserMessage(messageCode.getMessage());
        output.setData(messageCode);
        output.setDevMessage(ex.toString());
        output.setHttpCode(200);
        output.setOk(false);
        return new ResponseEntity<ResponseWrapper>(output, new HttpHeaders(), HttpStatus.OK);
    }
}