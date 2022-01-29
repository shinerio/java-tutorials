package com.shinerio.tutorial.controller;

import com.shinerio.tutorial.document.Account;
import com.shinerio.tutorial.service.AccountCrudService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountCrudService accountCrudService;

    public AccountController(AccountCrudService accountCrudService) {
        this.accountCrudService = accountCrudService;
    }

    //curl -X GET localhost/account -k
    @GetMapping
    public Flux<Account> findAll() {
        return accountCrudService.findAll();
    }

    //curl -X GET localhost/account/61ed1fbca3bf06111b383ee8 -k
    @GetMapping("/{id}")
    public Mono<Account> findById(@PathVariable("id") String id) {
        return accountCrudService.findById(id);
    }

    //curl -X POST localhost/account -d '{"owner":"shinerio","value":1.0}' -k -H "Content-type:application/json"
    @PostMapping
    public Mono<Account> insert(@RequestBody Account account) {
        return accountCrudService.insert(account);
    }

    //curl -X DELETE localhost/account/61ed1fbca3bf06111b383ee8 -k
    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable("id") String id) {
        return accountCrudService.deleteById(id);
    }
}
