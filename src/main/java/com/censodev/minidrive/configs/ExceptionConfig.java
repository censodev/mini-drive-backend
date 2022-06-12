package com.censodev.minidrive.configs;

import com.censodev.minidrive.dto.Res;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ExceptionConfig {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Res<Void>> handleMaxSizeException() {
        var msg = "Không thể tải lên tệp có dung lượng quá " + maxFileSize;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Res<>(null, msg));
    }

//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<Res<String>> handleResponseStatusException(ResponseStatusException e) {
//        return ResponseEntity.status(e.getStatus())
//                .body(new Res<>(e.getReason(), e.getMessage()));
//    }
}
