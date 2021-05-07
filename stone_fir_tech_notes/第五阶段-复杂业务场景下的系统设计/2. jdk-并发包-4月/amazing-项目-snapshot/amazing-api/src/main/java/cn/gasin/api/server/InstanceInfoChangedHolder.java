package cn.gasin.api.server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * 包装类, 增强: 时间, 操作
 */
@Log4j2
@Getter
@Setter
public class InstanceInfoChangedHolder {
    private long timestamp;
    private InstanceInfo instanceInfo;
    /**
     * 变更操作
     */
    private InstanceInfoOperation instanceInfoOperation;

    public InstanceInfoChangedHolder(InstanceInfo instanceInfo, InstanceInfoOperation instanceInfoOperation) {
        this.instanceInfo = instanceInfo;
        timestamp = System.currentTimeMillis();
        this.instanceInfoOperation = instanceInfoOperation;
    }
}
