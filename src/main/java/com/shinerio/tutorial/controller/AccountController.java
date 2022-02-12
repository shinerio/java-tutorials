package com.shinerio.tutorial.controller;

import com.shinerio.tutorial.document.Account;
import com.shinerio.tutorial.excpt.ConcurrentOperationExcept;
import com.shinerio.tutorial.service.AccountCrudService;
import com.shinerio.tutorial.util.LockManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/account")
@Api(tags="用户管理")
public class AccountController {

    private final AccountCrudService accountCrudService;
    private final LockManager lockManager;
    private final Scheduler accountLockScheduler = Schedulers.newSingle("accountLocker");

    public AccountController(AccountCrudService accountCrudService, LockManager lockManager) {
        this.accountCrudService = accountCrudService;
        this.lockManager = lockManager;
    }

    //curl -X GET localhost:5000/account -k
    @ApiOperation("用户列表")
    @GetMapping
    public Flux<Account> findAll() {
        return accountCrudService.findAll();
    }

    //curl -X GET localhost:5000/account/61ed1fbca3bf06111b383ee8 -k
    @ApiOperation("用户详情")
    @GetMapping("/{id}")
    public Mono<Account> findById(@PathVariable("id") String id) {
        return accountCrudService.findById(id);
    }

    //curl -X POST localhost:5000/account -d '{"owner":"shinerio","value":1.0}' -k -H "Content-type:application/json"
    @ApiOperation("创建用户")
    @PostMapping
    public Mono<Account> insert(@RequestBody Account account) {
        return accountCrudService.insert(account);
    }

    //curl -X DELETE localhost:5000/account/61ed1fbca3bf06111b383ee8 -k
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable("id") String id) {
        return Mono.just(id)
                .publishOn(accountLockScheduler).map(i -> {
                    if (!getDistributedLock(i)) {
                        throw new ConcurrentOperationExcept();
                    }
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .flatMap(accountCrudService::deleteById)
                .publishOn(accountLockScheduler)   // lock and unlock must on the same thread
                .doFinally(signalType -> releaseDistributedLock(id));
    }

    private boolean getDistributedLock(String id) {
        return lockManager.tryLock("account_" + id, 3, TimeUnit.SECONDS);
    }

    private void releaseDistributedLock(String id) {
        lockManager.unLock("account_" + id);
    }
}
