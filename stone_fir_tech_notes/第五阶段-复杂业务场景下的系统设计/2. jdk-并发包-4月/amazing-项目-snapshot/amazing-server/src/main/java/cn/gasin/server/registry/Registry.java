package cn.gasin.server.registry;

import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.api.server.InstanceInfo;
import cn.gasin.api.server.InstanceInfoOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 内存中维护的注册表.
 */

@Log4j2
@Service // spring里面的单例. 不用写单例模式了.
public class Registry {
    /**
     * 注册表: Map<serviceName: Map<instanceId, RegistryInfo>>
     * Node: 把map当成hashmap来感受并发编程:
     * 1. registry遍历的时候不能有并发修改, 所以遍历都要加一把锁: this
     * 2. registry新增/修改/删除的时候, 要拿到this锁
     * 3. 查询无影响.
     */
    private final Map<String, Map<String, InstanceInfo>> registry = new ConcurrentHashMap<>();

    // registry rwLock. // lock也可以包在try里面, 因为unlock不能把别人的锁给解了, 会对比线程的. 不像是jedis里面的简单分布式锁会把别人的锁给解了.🔒
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();


    @Autowired
    private RegistryUpdatesQueue registryUpdatesQueue;

    /**
     * 拿到一份拷贝.
     * - 拷贝也要加锁: 内部用的是iterator, 依然是不能并发, 在这个时候不能有人修改注册表的原始map.
     * - 加读锁, 是因为拷贝的时候是foreach遍历, 过程中不允许有修改.
     */
    public Map<String, Map<String, InstanceInfo>> getRegistryCopy() {
        readLock.lock();
        try {
            return new HashMap<>(registry);
        } finally {
            readLock.unlock();
        }
    }

    public void register(InstanceInfo instanceInfo) {
        writeLock.lock();
        try {
            Map<String, InstanceInfo> serverMap = registry.get(instanceInfo.getServiceName());
            if (serverMap == null) {
                registry.putIfAbsent(instanceInfo.getServiceName(), new ConcurrentHashMap<>());
                serverMap = registry.get(instanceInfo.getServiceName());
            }
            // put
            serverMap.put(instanceInfo.getInstanceId(), instanceInfo);

            registryUpdatesQueue.offer(instanceInfo, InstanceInfoOperation.REGISTER);
        } finally {
            writeLock.unlock();
        }
    }


    /**
     * 心跳服务接口
     * FIXME: note: 暂时不把心跳看作instance的update, 也没有考虑到register-server的多节点方案, 心跳不会同步.
     * 这个不用加读锁把, 不修改, 就是看有没有对象.
     */
    public boolean heartbeat(HeartbeatRequest req) {
        Map<String, InstanceInfo> serviceMap = registry.get(req.getServiceName());
        if (Objects.nonNull(serviceMap) && serviceMap.containsKey(req.getInstanceId())) {
            InstanceInfo instanceInfo = serviceMap.get(req.getInstanceId()); // 一种极端的, 这里拿到的instanceInfo是null
            instanceInfo.renew();
            return true;
        }
        return false;
    }

    /** 下线服务 */
    public boolean instanceOffline(RegisterRequest req) {
        writeLock.lock();
        try {
            Map<String, InstanceInfo> serviceMap = registry.get(req.getServiceName());
            if (Objects.nonNull(serviceMap) && serviceMap.containsKey(req.getInstanceId())) {
                if (serviceMap.containsKey(req.getInstanceId())) {
                    // 1. 下线
                    InstanceInfo instanceInfo = serviceMap.remove(req.getInstanceId());
                    // 2. 更新缓存
                    registryUpdatesQueue.offer(instanceInfo, InstanceInfoOperation.OFFLINE);
                    return true;
                }
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 驱逐instance
     */
    public void expel(InstanceInfo instance) {
        writeLock.lock();
        try {
            Map<String, InstanceInfo> serviceMap = registry.get(instance.getServiceName());
            if (Objects.nonNull(serviceMap) && serviceMap.containsKey(instance.getInstanceId())) {
                if (serviceMap.containsKey(instance.getInstanceId())) {
                    // 1. 驱逐
                    InstanceInfo instanceInfo = serviceMap.remove(instance.getInstanceId());
                    // 2. 更新缓存
                    registryUpdatesQueue.offer(instanceInfo, InstanceInfoOperation.EXPELLED);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 虽然不是修改, 但是遍历也要防止别的线程修改抛异常
     */
    public Integer getInstanceCount() {
        writeLock.lock();
        try {
            int count = 0;
            Collection<Map<String, InstanceInfo>> values = registry.values();
            for (Map<String, InstanceInfo> value : values) {
                count += value.size();
            }
            return count;
        } finally {
            writeLock.unlock();
        }
    }

}
