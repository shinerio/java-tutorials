package com.shinerio.tutorial.repository;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchResponse;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.function.Consumer;

@Repository
public class EtcdRepository {

    private final KV kvClient;
    private final Watch watchClient;

    public EtcdRepository(KV kvClient, Watch watchClient) {
        this.kvClient = kvClient;
        this.watchClient = watchClient;
    }

    public Mono<String> get(String key) {
        ByteSequence keyBytes = ByteSequence.from(key, Charset.defaultCharset());
        return Mono.defer(() -> Mono.fromCompletionStage(kvClient.get(keyBytes)))
                .flatMap(response -> {
                    if (response.getKvs().isEmpty()) {
                        return Mono.empty();
                    }
                    return Mono.just(response.getKvs().getFirst().getValue().toString());
                });
    }

    public Flux<KeyValue> getByPrefix(String prefix) {
        ByteSequence prefixBytes = ByteSequence.from(prefix, Charset.defaultCharset());
        GetOption getOption = GetOption.builder()
                .isPrefix(true)
                .build();

        return Mono.defer(() -> Mono.fromCompletionStage(kvClient.get(prefixBytes, getOption)))
                .flatMapMany(response -> Flux.fromIterable(response.getKvs()))
                .map(kv -> new KeyValue(
                        kv.getKey().toString(),
                        kv.getValue().toString()));
    }

    public Mono<Void> put(String key, String value) {
        ByteSequence keyBytes = ByteSequence.from(key, Charset.defaultCharset());
        ByteSequence valueBytes = ByteSequence.from(value, Charset.defaultCharset());
        // fromCompletionStage在流定义阶段已经提交了future任务，如果不使用defer，则future任务在流定义阶段就会执行，导致串行多个任务的时候顺序不受控。
        // 使用Mono.defer延迟future任务在流传播阶段提交，保证多个put.put或put.get等操作的顺序受控。
        return Mono.defer(() -> Mono.fromCompletionStage(kvClient.put(keyBytes, valueBytes))).then();
    }

    public Mono<Void> delete(String key) {
        ByteSequence keyBytes = ByteSequence.from(key, Charset.defaultCharset());
        return Mono.defer(() -> Mono.fromCompletionStage(kvClient.delete(keyBytes))).then();
    }

    public Watch.Watcher watch(String prefix, Consumer<WatchResponse> consumer) {
        ByteSequence prefixBytes = ByteSequence.from(prefix, Charset.defaultCharset());
        WatchOption watchOption = WatchOption.builder()
                .isPrefix(true)
                .build();

        return watchClient.watch(prefixBytes, watchOption, Watch.listener(consumer));
    }

    public record KeyValue(String key, String value) {
    }

}
