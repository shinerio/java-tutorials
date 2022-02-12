package com.shinerio.tutorial.excpt;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.shinerio.tutorial.document.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SentinelBlockHandler {
    /**
     * 包括参数和返回值在内的方法签名需要和原方法一致，加一个exception
     */
    public static Flux<Account> findAllBlockHandler(BlockException exception){
        throw new ApiCallLimit();
    }


    public static Mono<Account> findByIdBlockHandler(String id, BlockException exception){
        throw new ApiCallLimit();
    }

    public static Mono<Account> insertBlockHandler(Account account, BlockException exception){
        throw new ApiCallLimit();
    }

    public static Mono<Void> deleteBlockHandler(String id, BlockException exception){
        throw new ApiCallLimit();
    }
}
