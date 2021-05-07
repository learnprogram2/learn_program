package cn.gasin.server;

public class ServerConfig {
    /** 服务注册表期待心跳频率 */
    public static final int EXPECT_HEARTBEAT_FREQUENCY = 2;
    public static final float EXPECT_HEARTBEAT_THRESHOLD_FACTOR = 0.85f;

    public static void main(String[] args) {
        System.out.println(EXPECT_HEARTBEAT_THRESHOLD_FACTOR);
    }

}
