package com.writingcoach.web.api;

import com.writingcoach.domain.StaleDocumentException;
import com.writingcoach.service.DocumentNotFoundException;
import com.writingcoach.service.VersionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 把业务异常转换成稳定的 JSON 错误结构，避免接口直接返回堆栈信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ApiError> handleDocumentNotFound(DocumentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("DOCUMENT_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(VersionNotFoundException.class)
    public ResponseEntity<ApiError> handleVersionNotFound(VersionNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("VERSION_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(StaleDocumentException.class)
    public ResponseEntity<ApiError> handleStaleRevision(StaleDocumentException exception) {
        ApiError error = new ApiError(
                "STALE_REVISION", exception.getMessage(), exception.getCurrentRevision());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("请求内容不符合要求");
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }
}
