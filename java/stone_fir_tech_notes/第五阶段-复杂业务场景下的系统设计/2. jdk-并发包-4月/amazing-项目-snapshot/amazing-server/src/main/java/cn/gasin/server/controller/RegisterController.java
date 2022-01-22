package cn.gasin.server.controller;

import cn.gasin.api.http.Response;
import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.api.http.register.QueryRegistryResponse;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.api.server.InstanceInfo;
import cn.gasin.api.server.InstanceInfoChangedHolder;
import cn.gasin.server.cluster.PeersReplicator;
import cn.gasin.server.heartbeat.HeartbeatRate;
import cn.gasin.server.heartbeat.SelfProtectionPolicy;
import cn.gasin.server.registry.Registry;
import cn.gasin.server.registry.RegistryCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController("/")
public class RegisterController {

    @Autowired
    private Registry registry;
    @Autowired
    private HeartbeatRate heartbeatRate;
    @Autowired
    private SelfProtectionPolicy selfProtectionPolicy;
    @Autowired
    private RegistryCache registryCache;

    /** cluster 同步的组件 */
    @Autowired
    private PeersReplicator peersReplicator;


    /**
     * 注册接口
     *
     * @param req instance信息
     */
    @PostMapping("/register")
    public Response register(@RequestBody RegisterRequest req) {
        // register
        InstanceInfo instanceInfo = InstanceInfo.copyFrom(req);
        registry.register(instanceInfo);
        // 更新阈值
        selfProtectionPolicy.instanceRegister();
        // 更新缓存
        registryCache.invalidRwCache();
        // 集群同步
        peersReplicator.replicateRegister(req);


        return Response.success(null);
    }

    /**
     * 心跳接口, 这个最后要抽象到heartbeat portal注册一个heartbeatHandler里面去处理整个逻辑.
     *
     * @param req instance信息
     */
    @PostMapping("/heartbeat")
    public Response register(@RequestBody HeartbeatRequest req) {
        // heartbeat
        if (!registry.heartbeat(req)) {
            return Response.failed("instance haven't in registry! please register first.");
        }

        // 心跳成功, 计数
        heartbeatRate.count();

        // 集群同步
        peersReplicator.replicateHeartbeat(req);


        return Response.success(null);
    }

    /** 下线client */
    @PutMapping("/instanceOffline")
    public Response instanceOffline(@RequestBody RegisterRequest req) {
        log.info("接收到client下线请求: {}", req);

        if (registry.instanceOffline(req)) {
            // 更新自我保护阈值
            selfProtectionPolicy.instanceDead();
            // 更新缓存
            registryCache.invalidRwCache();
            // 集群同步
            peersReplicator.replicateOffline(req);

            return Response.success(null);
        }
        return Response.failed("下线失败");
    }

    /** 拿全量注册表 */
    @GetMapping("/registry")
    public QueryRegistryResponse getAllRegistry() {
        QueryRegistryResponse response = QueryRegistryResponse.success(null);
        response.setInstanceInfoMap((Map<String, Map<String, InstanceInfo>>) registryCache.get(RegistryCache.FULL_REGISTRY));
        return response;
    }

    /** 拿增量注册表 */
    @GetMapping("/registry/delta")
    public QueryRegistryResponse getDeltaRegistry() {
        QueryRegistryResponse response = QueryRegistryResponse.success(null);
        response.setDeltaInstanceInfoList((List<InstanceInfoChangedHolder>) registryCache.get(RegistryCache.DELTA_REGISTRY));
        response.setInstanceCount(registry.getInstanceCount());
        return response;
    }


}
