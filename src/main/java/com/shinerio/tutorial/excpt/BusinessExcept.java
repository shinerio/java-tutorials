package com.shinerio.tutorial.excpt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class BusinessExcept extends RuntimeException {
    private HttpStatus code;
    private String errMsg;

    public  BusinessExcept(HttpStatus code, String errMsg){
        this.code = code;
        this.errMsg = errMsg;
    }
}
