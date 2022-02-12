package com.shinerio.tutorial.excpt;

import org.springframework.http.HttpStatus;

public class ApiCallLimit extends BusinessExcept {

    public ApiCallLimit() {
        super(HttpStatus.BAD_REQUEST, "Under flow control, wait for a moment");
    }
}
