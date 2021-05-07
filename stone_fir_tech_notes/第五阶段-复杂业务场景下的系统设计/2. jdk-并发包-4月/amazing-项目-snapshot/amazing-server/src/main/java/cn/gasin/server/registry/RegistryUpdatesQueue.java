package cn.gasin.server.registry;

import cn.gasin.api.server.InstanceInfo;
import cn.gasin.api.server.InstanceInfoChangedHolder;
import cn.gasin.api.server.InstanceInfoOperation;
import cn.gasin.api.server.config.ServiceConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.gasin.api.server.config.ServiceConfig.REGISTRY_UPDATES_CACHE_EXPIRE_INTERNAL;

/**
 * 注册表最新变动的缓存.
 */
@Log4j2
@Component
public class RegistryUpdatesQueue {

    /**
     * 这个list修改的时候也有并发问题, 但是, 这个数据不是很重要. 而且这个list的修改很集中, 大多数都是读取.
     * TODO: 这个队列里面没有去重
     */
    private final LinkedList<InstanceInfoChangedHolder> recentlyChangedQueue = new LinkedList<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock rLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock wLock = rwLock.writeLock();

    private UpdatesExpelDaemon updatesExpelDaemon;

    public RegistryUpdatesQueue() {
        this.updatesExpelDaemon = new UpdatesExpelDaemon();
        updatesExpelDaemon.start();
    }

    /**
     * 缓存一个刚刚更新的instance, 更新的操作是operation
     */
    public void offer(InstanceInfo instanceInfo, InstanceInfoOperation operation) {
        InstanceInfoChangedHolder infoChangedHolder = new InstanceInfoChangedHolder(instanceInfo, operation);
        try {
            wLock.lock();
            recentlyChangedQueue.offer(infoChangedHolder);
        } finally {
            wLock.unlock();
        }
    }

    public LinkedList<InstanceInfoChangedHolder> getRecentlyChangedQueueCopy() {
        try {
            rLock.lock();
            return new LinkedList<>(recentlyChangedQueue);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * > 一旦启动线程，便不必保留对 Thread 对象的引用。 线程将继续执行，直到该线程过程完成.
     * 所以必须关掉/设置成守护线程
     */
    class UpdatesExpelDaemon extends Thread {
        public UpdatesExpelDaemon() {
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(ServiceConfig.REGISTRY_UPDATES_CACHE_DAEMON_INTERNAL);
                    long timestamp = System.currentTimeMillis();

                    try {
                        wLock.lock();
                        while (recentlyChangedQueue.size() > 0) {
                            InstanceInfoChangedHolder infoChangedHolder = recentlyChangedQueue.peek();
                            // 如果没有过期的cache, 就下次循环了.
                            if (Objects.isNull(infoChangedHolder) ||
                                    timestamp - infoChangedHolder.getTimestamp() < REGISTRY_UPDATES_CACHE_EXPIRE_INTERNAL) {
                                continue;
                            }
                            // 把过期的缓存干掉
                            recentlyChangedQueue.poll();
                        }
                    } finally {
                        wLock.unlock();
                    }
                } catch (InterruptedException e) {
                    log.info("UpdatesExpelDaemon was interrupted, exit");
                    return;
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }
}
