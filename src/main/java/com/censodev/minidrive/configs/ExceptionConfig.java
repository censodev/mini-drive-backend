package com.censodev.minidrive.configs;

import com.censodev.minidrive.data.dto.Res;
import com.censodev.minidrive.exceptions.BusinessException;
import com.censodev.minidrive.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Res<String>> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new Res<>(null, e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Res<String>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new Res<>(null, e.getMessage()));
    }
}
