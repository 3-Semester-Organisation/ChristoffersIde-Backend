package ccy.reactiveprogramingmonoandflux.exception;

import ccy.reactiveprogramingmonoandflux.dto.MyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiConnectivityException.class)
    public ResponseEntity<MyResponse> handleApiConnectivityException(ApiConnectivityException ex) {
        MyResponse response = new MyResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }


    @ExceptionHandler(NameDataNotFoundException.class)
    public ResponseEntity<MyResponse> handleApiConnectivityException(NameDataNotFoundException ex) {
        MyResponse response = new MyResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}
