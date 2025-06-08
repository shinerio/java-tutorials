package com.shinerio.tutorial.controller;


import com.shinerio.tutorial.service.HelloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/hello")
@Tag(name="hello world")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @Operation(summary = "echo hello")
    @GetMapping
    public Flux<String> findAll() {
        return helloService.sayHello();
    }

}
