package cn.gasin.client.http;

import cn.gasin.api.http.Response;
import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.api.http.register.QueryRegistryResponse;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.api.server.InstanceInfo;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static cn.gasin.client.config.ClientConfig.*;

/**
 * TODO http client, 功能类, 需要完善Http接口调用功能.
 */
@Log4j2
public class HttpClient {
    public Response sendRegisterRequest(RegisterRequest registerRequest) {
        return null;
    }

    public Response sendHeartbeat(HeartbeatRequest heartbeatRequest) {
        return null;
    }

    /** 拉取注册表 */
    public Map<String, Map<String, InstanceInfo>> fetchRegistry() {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setInstanceIp("192.168.0.1");
        instanceInfo.setInstanceId("instanceId");
        instanceInfo.setInstancePort(8080);
        instanceInfo.setServiceName("serviceName1");

        Map<String, Map<String, InstanceInfo>> registry = new HashMap<>();

        Map<String, InstanceInfo> serviceMap = new HashMap<>();

        registry.put("serviceName1", serviceMap);
        serviceMap.put(instanceInfo.getInstanceId(), instanceInfo);

        return registry;
    }

    /** 下线client */
    public void instanceOffline() {
        log.info("client下线");
        RegisterRequest registerRequest =
                new RegisterRequest()
                        .setServiceName(SERVICE_NAME)
                        .setInstanceId(INSTANCE_ID)
                .setInstanceIp(INSTANCE_IP)
                .setInstancePort(INSTANCE_PORT);

    }


    // HttpClient关闭
    public void shutDown() {
        log.info("httpClient 销毁");

        // 调用server的下线接口.
    }

    /** TODO: 拉取增量注册表, 待实现哈 */
    public QueryRegistryResponse fetchDeltaRegistry() {
        QueryRegistryResponse response = new QueryRegistryResponse();
        response.setInstanceCount(0);
        response.setDeltaInstanceInfoList(new LinkedList<>());
        return response;
    }
}
