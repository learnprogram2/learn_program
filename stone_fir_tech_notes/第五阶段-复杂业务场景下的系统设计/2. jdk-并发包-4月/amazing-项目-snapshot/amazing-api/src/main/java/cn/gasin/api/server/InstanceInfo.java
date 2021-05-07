package cn.gasin.api.server;

import cn.gasin.api.http.register.RegisterRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@Log4j2
@Getter
@Setter
public class InstanceInfo {

    // 服务坐标: name&id
    String serviceName;
    String instanceId;

    // 地址坐标: instance address
    String instanceIp;
    Integer instancePort;

    private Lease lease = new Lease();
    private static long NOT_ALIVE_PERIOD = 90 * 1000;

    public void renew() {
        this.lease.renew();
    }

    public boolean isAlive() {
        return this.lease != null && lease.isAlive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceInfo that = (InstanceInfo) o;
        return serviceName.equals(that.serviceName) && instanceId.equals(that.instanceId) && instanceIp.equals(that.instanceIp) && instancePort.equals(that.instancePort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, instanceId, instanceIp, instancePort);
    }

    /**
     * 租约
     */
    class Lease {
        // 可以保证并发间的变量更新有立即的可见性.
        volatile long lastHeartbeatTime = System.currentTimeMillis();

        public void renew() {
            lastHeartbeatTime = System.currentTimeMillis();
            log.info("service [{}] instance [{}] renew", serviceName, instanceId);
        }

        public boolean isAlive() {
            // 是否存活: internal 内有续约
            long current = System.currentTimeMillis();
            return current - lastHeartbeatTime <= NOT_ALIVE_PERIOD;
        }
    }


    // ======================================================================================== utils
    // 原型模式:
    public static InstanceInfo copyFrom(RegisterRequest req) {
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setServiceName(req.getServiceName());
        instanceInfo.setInstanceId(req.getInstanceId());
        instanceInfo.setInstanceIp(req.getInstanceIp());
        instanceInfo.setInstancePort(req.getInstancePort());
        return instanceInfo;
    }

}
