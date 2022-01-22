package cn.gasin.api.http.register;

import cn.gasin.api.http.BaseClientRequest;
import lombok.*;


/**
 * 注册请求, 唯一标识一个client的标识.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest extends BaseClientRequest {

    // 地址坐标: instance address
    String instanceIp;
    Integer instancePort;

    public RegisterRequest setServiceName(String serviceName) {
        setServiceName(serviceName);
        return this;
    }

    public RegisterRequest setInstanceId(String instanceId) {
        setInstanceId(instanceId);
        return this;
    }

    public RegisterRequest setInstanceIp(String instanceIp) {
        setInstanceIp(instanceIp);
        return this;
    }

    public RegisterRequest setInstancePort(Integer instancePort) {
        setInstancePort(instancePort);
        return this;
    }


}
