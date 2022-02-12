package com.shinerio.tutorial.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.shinerio.tutorial.document.Account;
import com.shinerio.tutorial.excpt.SentinelBlockHandler;
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

    @SentinelResource(value = "account_find_all", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "findAllBlockHandler")
    public Flux<Account> findAll() {
        return accountCrudRepository.findAll();
    }

    @SentinelResource(value = "account_find_by_id", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "findByIdBlockHandler")
    public Mono<Account> findById(String id) {
        return accountCrudRepository.findById(id);
    }

    @SentinelResource(value = "account_insert", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "insertBlockHandler")
    public Mono<Account> insert(Account account) {
        return accountCrudRepository.insert(account);
    }

    @SentinelResource(value = "account_delete", blockHandlerClass = SentinelBlockHandler.class, blockHandler = "deleteBlockHandler")
    public Mono<Void> deleteById(String id) {
        return accountCrudRepository.deleteById(id);
    }
}
