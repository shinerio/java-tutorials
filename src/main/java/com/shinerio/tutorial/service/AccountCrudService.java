package com.shinerio.tutorial.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.shinerio.tutorial.document.Account;
import com.shinerio.tutorial.repository.AccountCrudRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountCrudService {

    private final AccountCrudRepository accountCrudRepository;

    public AccountCrudService(AccountCrudRepository accountCrudRepository) {
        this.accountCrudRepository = accountCrudRepository;
    }

    @SentinelResource("account_find_all")
    public Flux<Account> findAll() {
        return accountCrudRepository.findAll();
    }

    @SentinelResource("account_find_by_id")
    public Mono<Account> findById(String id) {
        return accountCrudRepository.findById(id);
    }

    public Mono<Account> insert(Account account) {
        return accountCrudRepository.insert(account);
    }

    public Mono<Void> deleteById(String id) {
        return accountCrudRepository.deleteById(id);
    }
}
