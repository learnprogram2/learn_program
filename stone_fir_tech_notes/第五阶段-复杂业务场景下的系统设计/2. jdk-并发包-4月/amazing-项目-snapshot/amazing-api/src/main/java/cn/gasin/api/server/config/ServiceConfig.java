package cn.gasin.api.server.config;

public class ServiceConfig {

    public static long REGISTRY_UPDATES_CACHE_DAEMON_INTERNAL = 30 * 1000L;
    // updates 缓存里的过期时间, 默认3分钟, 绰绰有余, 其实90s+30s就可以了, 因为心跳超时时间是90s, 驱逐线程间隔30s.
    public static long REGISTRY_UPDATES_CACHE_EXPIRE_INTERNAL = 3 * 60 * 1000L;


    /** server的registry双级缓存数据同步间隔 */
    public static final Long REGISTRY_TWO_LEVEL_CACHE_SYNC_INTERVAL = 30 * 1000L;

}
