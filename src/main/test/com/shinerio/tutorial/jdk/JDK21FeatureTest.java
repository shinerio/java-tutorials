package com.shinerio.tutorial.jdk;

import com.shinerio.tutorial.other.MTLSHttpClientTest;
import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class JDK21FeatureTest {

    @Test
    public void testCreateVirtualThread() throws InterruptedException {
        Thread thread1 = Thread.ofVirtual().start(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Virtual Thread 1 is running");
        });

        Thread thread2 = Thread.startVirtualThread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Virtual Thread 2 is running");
        });

        try (var virtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            virtualThreadPerTaskExecutor.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignored) {
                }
                System.out.println("Virtual Thread 3 is running");
            });
        }

        ThreadFactory factory = Thread.ofVirtual().factory();
        Thread thread4 = factory.newThread(() -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Virtual Thread 4 is running");
        });
        thread4.start();

        thread1.join();
        thread2.join();
        thread4.join();
    }

    @Test
    public void testPerformance() {
        AtomicInteger threadNum = new AtomicInteger(0);
        // 开启线程 统计平台线程数
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfo = threadBean.dumpAllThreads(false, false);
            if (threadInfo.length > threadNum.get()) {
                threadNum.set(threadInfo.length);
            }
        }, 10, 10, TimeUnit.MILLISECONDS);

        long start = System.currentTimeMillis();
        // 虚拟线程
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        // 使用平台线程
        //ExecutorService executor = Executors.newFixedThreadPool(10000);
        for (int i = 0; i < 10000; i++) {
            executor.submit(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executor.close();
        System.out.printf("totalMillis：%dms\tmax platform thread/os thread num: %d\n", System.currentTimeMillis() - start, threadNum.get());
    }

    @Test
    public void testVirtualThreadUnload() throws InterruptedException {
        var threads = IntStream.range(0, 5).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
            System.out.println(Thread.currentThread());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread());
        })).toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    public void testVirtualThreadUnloadWhileDoHttpRequest() throws InterruptedException {
        var threads = IntStream.range(0, 5).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
            System.out.println(Thread.currentThread());
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://localhost:5000/account"))
                        .GET()
                        .build();
                MTLSHttpClientTest.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {
            }
            System.out.println(Thread.currentThread());
        })).toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
    }


    @Test
    public void testVirtualThreadSyncCannotUnload() throws InterruptedException {
        var threads = IntStream.range(0, 10).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
            synchronized (JDK21FeatureTest.class) {
                System.out.println(Thread.currentThread());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread());
            }
        })).toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    public void testVirtualThreadReentrantLockUnload() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        var threads = IntStream.range(0, 10).mapToObj(index -> Thread.ofVirtual().unstarted(() -> {
            lock.lock();
            try {
                System.out.println("with lock: " + Thread.currentThread());
                Thread.sleep(100);
                System.out.println("with lock: " + Thread.currentThread());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        })).toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
