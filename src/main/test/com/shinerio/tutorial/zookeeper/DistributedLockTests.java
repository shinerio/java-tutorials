package com.shinerio.tutorial.zookeeper;

import com.shinerio.tutorial.JavaTutorialTests;
import com.shinerio.tutorial.util.LockManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DistributedLockTests extends JavaTutorialTests {

    @Resource
    private LockManager lockManager;

    public static class Counter {
        public int count = 0;
    }

    @Test
    public void lockTest() throws InterruptedException {
        Counter counter = new Counter();
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            for (int i = 0; i < 100; i++) {
                forkJoinPool.submit(() -> {
                    try {
                        String key = "test";
                        // 获取锁
                        if (lockManager.tryLock(key, 10000, TimeUnit.SECONDS)) {
                            for (int j = 0; j < 1000; j++) {
                                counter.count++;
                            }
                            TimeUnit.SECONDS.sleep(10);
                            lockManager.unLock(key);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                });
            }
            forkJoinPool.shutdown();
            Assertions.assertTrue(forkJoinPool.awaitTermination(30, TimeUnit.SECONDS));
        }

        Assertions.assertEquals(100 * 1000, counter.count);
    }
}
