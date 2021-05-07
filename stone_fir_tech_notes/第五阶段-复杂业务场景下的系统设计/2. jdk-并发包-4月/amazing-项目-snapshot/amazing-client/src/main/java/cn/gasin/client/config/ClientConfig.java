package cn.gasin.client.config;

import lombok.Getter;

public class ClientConfig {

    // config
    public static final String SERVICE_NAME = "demoProject";
    public static final String INSTANCE_ID = "instance01";
    public static final String INSTANCE_IP = "192.168.0.1";
    public static final Integer INSTANCE_PORT = 9001;


    // registry maintain config
    /** 服务注册表拉取间隔时间 */
    public static final Long REGISTRY_FETCH_INTERVAL = 30 * 1000L;

}
