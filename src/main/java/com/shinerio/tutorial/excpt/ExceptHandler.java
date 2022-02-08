package com.shinerio.tutorial.excpt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class ExceptHandler {

    /**
     * 处理BusinessException异常
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = BusinessExcept.class)
    @ResponseBody
    public ResponseEntity<String> handle(BusinessExcept e) {
        return new ResponseEntity<>(e.getErrMsg(), e.getCode());
    }

    /**
     * 处理其他异常
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<String> handle(Exception e) {
        log.error("Unknown exception caught", e);
        return new ResponseEntity<>("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
