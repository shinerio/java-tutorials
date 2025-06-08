package com.shinerio.tutorial.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.shinerio.tutorial.excpt.SentinelBlockHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class HelloService {

    @SentinelResource(value = "say_hello", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "findAllBlockHandler")
    public Flux<String> sayHello() {
        return Flux.just("hello");
    }
}
