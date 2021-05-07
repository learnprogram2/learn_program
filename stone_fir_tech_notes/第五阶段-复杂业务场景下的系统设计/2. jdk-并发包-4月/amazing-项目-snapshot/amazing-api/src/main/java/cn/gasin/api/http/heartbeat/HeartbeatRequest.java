package cn.gasin.api.http.heartbeat;

import cn.gasin.api.http.BaseClientRequest;
import cn.gasin.api.http.RequestType;
import lombok.Getter;
import lombok.Setter;

/**
 * 心跳请求: 只需要发送自己的坐标就好了吧~
 */
@Getter
@Setter
public class HeartbeatRequest extends BaseClientRequest {
    public HeartbeatRequest() {
        super.setRequestType(RequestType.HEARTBEAT);
    }

    public HeartbeatRequest setServiceName(String serviceName) {
        setServiceName(serviceName);
        return this;
    }

    public HeartbeatRequest setInstanceId(String instanceId) {
        setInstanceId(instanceId);
        return this;
    }

}
