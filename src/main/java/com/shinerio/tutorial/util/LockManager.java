package com.shinerio.tutorial.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LockManager {

    private static final ConcurrentHashMap<String, InterProcessMutex> LOCK_MAP = new ConcurrentHashMap<>();

    private final String LOCK_PATH = "/java-tutorial/lock-space/%s";

    private final CuratorFramework curatorFramework;

    public LockManager(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    /**
     * 尝试获取分布式排他锁，仅能用于同步方法，加解锁需要同一个线程
     *
     * @param key        分布式锁 key
     * @param waitTime 超时时间
     * @param timeUnit   时间单位
     * @return 超时时间单位
     */
    public boolean tryLock(String key, int waitTime, TimeUnit timeUnit) {
        try {
            InterProcessMutex mutex = new InterProcessMutex(curatorFramework, String.format(LOCK_PATH, key));
            boolean locked = mutex.acquire(waitTime, timeUnit);
            if (locked) {
                log.info("申请锁({})成功", key);
                LOCK_MAP.put(key, mutex);
                return true;
            }
        } catch (Exception e) {
            log.error("申请锁({})失败", key, e);
        }
        log.info("申请锁({})失败", key);
        return false;
    }

    /**
     * 释放锁，仅能用于同步方法，加解锁需要同一个线程
     *
     * @param key 分布式锁 key
     */
    public void unLock(String key) {
        try {
            InterProcessMutex mutex = LOCK_MAP.get(key);
            if (mutex != null) {
                mutex.release();
                LOCK_MAP.remove(key);
            }
            log.info("解锁({})成功", key);
        } catch (Exception e) {
            log.error("解锁({})失败！", key);
        }
    }
}
