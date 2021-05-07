package cn.gasin.server.registry;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.gasin.api.server.config.ServiceConfig.REGISTRY_TWO_LEVEL_CACHE_SYNC_INTERVAL;

/**
 * registry设计为二级缓存.
 * 1. 每一层的缓存都存储两个东西: 全部的registry, 最近更新的instanceQueue缓存
 * 一级缓存: registry 的读写cache, 存储全部注册的instance
 * 二级缓存: registry 的读cache, 存储delta的registry, registry的更新部分缓存. 使用RegistryUpdatesCache
 */
@Log4j2
@Service
public class RegistryCache {

    @Autowired
    private Registry registry;
    @Autowired
    private RegistryUpdatesQueue registryUpdatesQueue;
    /**
     * 两级缓存:
     */
    private final Map<String, Object> rwCache = new HashMap<>();
    private final Map<String, Object> rCache = new HashMap<>();
    public static final String FULL_REGISTRY = "FULL_REGISTRY";
    public static final String DELTA_REGISTRY = "DELTA_REGISTRY";

    // 缓存定期更新的线程
    private final CacheSyncDaemon twoLevelCacheSyncDaemon = new CacheSyncDaemon();

    // 缓存操作的锁🔒
    //      // 因为加了readLock之后,不能加writeLock, 所以这是锁升级的obj. 只有在加了读锁后再加这个锁, 把这个锁当成拿到读锁后的升级写锁.
    //      // 注意: 这个在读锁并发下, 依然会出问题, 但是readLock只在一个地方有, 所以暂时解决.
    private final Object lockForRwCache = new Object();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock rLockForRCache = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock wLockForRCache = rwLock.writeLock();


    public RegistryCache() {
        twoLevelCacheSyncDaemon.start();
    }


    public Object get(String cacheKey) {
        Object cacheValue = null;

        // read cache里面为null, 就更新read-write cache, 然后从rwCache里拿一份给read cache.
        cacheValue = rCache.get(cacheKey);
        if (cacheValue == null) {
            synchronized (lockForRwCache) {
                try {
                    rLockForRCache.lock();

                    if (rCache.get(cacheKey) == null) {
                        if (FULL_REGISTRY.equals(cacheKey)) {
                            cacheValue = registry.getRegistryCopy();
                        } else if (DELTA_REGISTRY.equals(cacheKey)) {
                            cacheValue = registryUpdatesQueue.getRecentlyChangedQueueCopy();
                        }
                        rwCache.put(cacheKey, cacheValue);
                    }
                    rCache.put(cacheKey, cacheValue);
                } finally {
                    rLockForRCache.unlock();
                }
            }
        }

        return cacheValue;
    }


    /** 过期 rwCache 的全部缓存 */
    public void invalidRwCache() {
        synchronized (lockForRwCache) {
            rwCache.remove(FULL_REGISTRY);
            rwCache.remove(DELTA_REGISTRY);
        }
    }

    // 负责两级缓存的同步
    class CacheSyncDaemon extends Thread {
        public CacheSyncDaemon() {
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(REGISTRY_TWO_LEVEL_CACHE_SYNC_INTERVAL);
                    synchronized (lockForRwCache) {
                        wLockForRCache.lock(); // 加写锁.
                        try {
                            if (rwCache.get(FULL_REGISTRY) == null) {
                                rCache.put(FULL_REGISTRY, null);
                            }
                            if (rwCache.get(DELTA_REGISTRY) == null) {
                                rCache.put(DELTA_REGISTRY, null);
                            }
                        } finally {
                            wLockForRCache.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("双级缓存同步线程被打断, 取消双击缓存", e);
                    log.warn("registry cache sync thread was interrupted, cancel the next sync work.", e);
                }
            }
        }
    }
}
