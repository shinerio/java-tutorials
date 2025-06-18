package com.shinerio.tutorial.repository;

import com.shinerio.tutorial.JavaTutorialTests;
import com.shinerio.tutorial.util.JsonUtils;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

@Slf4j
class EtcdRepositoryTest extends JavaTutorialTests {

    @Resource
    private EtcdRepository etcdRepository;

    @AfterEach
    public void teardown() {
        etcdRepository.getByPrefix("/").flatMap(keyValue -> etcdRepository.delete(keyValue.key())).collectList().block();
    }

    @Test
    public void testPutAndGet() {
        StepVerifier.create(etcdRepository.put("/name", "shinerio").then(etcdRepository.get("/name")))
                .expectNext("shinerio")
                .verifyComplete();

        StepVerifier.create(etcdRepository.put("/personal_info", JsonUtils.toJSON(Map.of("name", "shinerio", "age", 30)))
                        .then(etcdRepository.get("/personal_info"))
                        .mapNotNull(JsonUtils::toMap))
                .expectNextMatches(personalInfo ->
                        personalInfo != null
                                && "shinerio".equals(personalInfo.get("name")) && 30 == (int) personalInfo.get("age"))
                .verifyComplete();
    }

    @Test
    public void testGetPrefix() {
        etcdRepository.put("/student/shinerio", "shinerio").then(etcdRepository.put("/student/tom", "tom")).block();

        StepVerifier.create(etcdRepository.getByPrefix("/student"))
                .expectNextMatches(keyValue -> keyValue.value().equals("shinerio"))
                .expectNextMatches(keyValue -> keyValue.value().equals("tom"))
                .verifyComplete();
    }

    @Test
    public void testWatchKey() {
        AtomicInteger count = new AtomicInteger(0);
        Watch.Watcher watch = etcdRepository.watch("/teacher", watchResponse -> {
            List<WatchEvent> events = watchResponse.getEvents();
            events.forEach(event -> {
                String key = event.getKeyValue().getKey().toString();
                String value = event.getKeyValue().getValue().toString();
                log.info("receive event: key: {}, value: {}", key, value);
                count.incrementAndGet();
            });
        });
        etcdRepository.put("/teacher/shinerio", "shinerio").then(etcdRepository.put("/teacher/tom", "tom")).block();
        await().timeout(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(100)).until(() -> count.get() == 2);
        watch.close();
    }
}