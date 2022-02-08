package com.shinerio.tutorial.excpt;

import org.springframework.http.HttpStatus;

public class ConcurrentOperationExcept extends BusinessExcept {
    public ConcurrentOperationExcept() {
        super(HttpStatus.BAD_REQUEST, "Resource is under operation, wait for a moment!");
    }
}
