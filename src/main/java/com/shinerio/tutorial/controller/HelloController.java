package com.shinerio.tutorial.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/hello")
@Api(tags="hello")
public class HelloController {

    @ApiOperation("用户列表")
    @GetMapping
    public Flux<String> findAll() {
        return Flux.just("hello");
    }

}
