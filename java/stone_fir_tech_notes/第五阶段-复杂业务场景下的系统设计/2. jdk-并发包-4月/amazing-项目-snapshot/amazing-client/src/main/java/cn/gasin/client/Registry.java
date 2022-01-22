package cn.gasin.client;

import cn.gasin.api.http.register.QueryRegistryResponse;
import cn.gasin.api.server.InstanceInfo;
import cn.gasin.api.server.InstanceInfoChangedHolder;
import cn.gasin.api.server.InstanceInfoOperation;
import cn.gasin.client.http.HttpClient;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.gasin.client.config.ClientConfig.REGISTRY_FETCH_INTERVAL;

/**
 * client端的注册表
 */
@Log4j2
public class Registry extends Thread {
    /**
     * 注册表: Map<serviceName: Map<instanceId, RegistryInfo>>
     */
    private volatile Map<String, Map<String, InstanceInfo>> registry;
    // 优化一: 把registry包装到一个obj里面, 用AtomicReference, 这个优化不做, 太麻烦, 而且并发量不高.
    // 优化二: 使用AtomicStampedReference来避免ABA问题, 这个就有点扯了.
    // 问题是: 就算是用了atomicReference也不能避免这个map的并发操作不出问题啊.
    private AtomicInteger registryVersion = new AtomicInteger(0); // 对registry的修改, 使用version做好ABA的防护.

    private final RegisterClientWorker registerClientWorker;
    private final HttpClient httpClient;

    private volatile boolean initialize = false;

    public Registry(RegisterClientWorker registerClientWorker, HttpClient httpClient) {
        this.registerClientWorker = registerClientWorker;
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        super.run();

        // 初始化拉全部注册表
        while (!initialize) {
            int expectedVersion = registryVersion.get();
            Map<String, Map<String, InstanceInfo>> serverRegistry = httpClient.fetchRegistry();
            if (registryVersion.compareAndSet(expectedVersion, expectedVersion + 1)) {
                initialize = true;
                registry = serverRegistry;
            }
        }

        // 每30s拉取注册表
        while (registerClientWorker.running()) {
            try {
                int expectedVersion = registryVersion.get();
                // 直接拿来替换, 暂时不过滤啊做别的处理.
                QueryRegistryResponse deltaResponse = httpClient.fetchDeltaRegistry();
                if (registryVersion.compareAndSet(expectedVersion, expectedVersion + 1)) {
                    synchronized (Registry.class) {
                        mergeIntoRegistry(deltaResponse.getDeltaInstanceInfoList());
                        log.info("fetch registry success, contains [{}] service.", registry.size());
                        // 优化: 校验 merge的对不对
                        int clientInstanceCount = 0;
                        for (Map<String, InstanceInfo> value : registry.values()) {
                            clientInstanceCount += value.size();
                        }
                        if (deltaResponse.getInstanceCount() != clientInstanceCount) {
                            registry = httpClient.fetchRegistry();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("拉去注册表失败", e);
            } finally {
                try {
                    sleep(REGISTRY_FETCH_INTERVAL);
                } catch (InterruptedException e) {
                    log.error("拉去注册表, sleep被唤醒.");
                    if (Thread.interrupted())
                        break;
                }
            }
        }

    }

    private void mergeIntoRegistry(List<InstanceInfoChangedHolder> fetchDeltaRegistry) {
        if (fetchDeltaRegistry.size() == 0) {
            log.info("no registry updates.");
            return;
        }

        // todo: 这里要判断更新的时间, 不能老的把新的替换掉了.
        while (fetchDeltaRegistry.size() > 0) {
            for (InstanceInfoChangedHolder infoChangedHolder : fetchDeltaRegistry) {
                InstanceInfo instanceInfo = infoChangedHolder.getInstanceInfo();
                // 新服务直接加进去
                if (infoChangedHolder.getInstanceInfoOperation().equals(InstanceInfoOperation.REGISTER)) {
                    if (!registry.containsKey(instanceInfo.getServiceName())) {
                        Map<String, InstanceInfo> serviceMap = new HashMap<>();
                        serviceMap.put(instanceInfo.getInstanceId(), instanceInfo);
                        registry.put(instanceInfo.getServiceName(), serviceMap);
                        continue;
                    }
                    Map<String, InstanceInfo> serviceMap = registry.get(instanceInfo.getServiceName());
                    if (!serviceMap.containsKey(instanceInfo.getInstanceId())) {
                        serviceMap.put(instanceInfo.getInstanceId(), instanceInfo);
                    } else {
                        InstanceInfo contained = serviceMap.get(instanceInfo.getInstanceId());
                        if (!contained.equals(instanceInfo)) {
                            serviceMap.put(instanceInfo.getInstanceId(), instanceInfo);
                        }
                    }
                } else if (infoChangedHolder.getInstanceInfoOperation().equals(InstanceInfoOperation.EXPELLED)
                        || infoChangedHolder.getInstanceInfoOperation().equals(InstanceInfoOperation.OFFLINE)) {
                    Map<String, InstanceInfo> serviceMap = registry.get(instanceInfo.getServiceName());
                    if (serviceMap != null) {
                        serviceMap.remove(instanceInfo.getInstanceId());
                    }
                }
            }
        }

    }

    public void shutDown() {
        registry = null;
        this.interrupt();
    }

    public Map<String, Map<String, InstanceInfo>> getRegistry() {
        // todo: 要防止并发修改, 拿出去查询就好了, 别搞修改.
        return registry;
    }
}
