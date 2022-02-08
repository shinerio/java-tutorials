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
     * 尝试获取分布式排他锁
     *
     * @param key        分布式锁 key
     * @param expireTime 超时时间
     * @param timeUnit   时间单位
     * @return 超时时间单位
     */
    public boolean tryLock(String key, int expireTime, TimeUnit timeUnit) {
        try {
            InterProcessMutex mutex = new InterProcessMutex(curatorFramework, String.format(LOCK_PATH, key));
            boolean locked = mutex.acquire(expireTime, timeUnit);
            if (locked) {
                log.info("申请锁(" + key + ")成功");
                LOCK_MAP.put(key, mutex);
                return true;
            }
        } catch (Exception e) {
            log.error("申请锁(" + key + ")失败,错误：{}", e);
        }
        log.warn("申请锁(" + key + ")失败");
        return false;
    }

    /**
     * 释放锁
     *
     * @param key 分布式锁 key
     */
    public void unLock(String key) {
        try {
            InterProcessMutex mutex = LOCK_MAP.get(key);
            if (mutex != null) {
                mutex.release();
            }
            log.info("解锁(" + key + ")成功");
        } catch (Exception e) {
            log.error("解锁(" + key + ")失败！");
        }
    }
}
