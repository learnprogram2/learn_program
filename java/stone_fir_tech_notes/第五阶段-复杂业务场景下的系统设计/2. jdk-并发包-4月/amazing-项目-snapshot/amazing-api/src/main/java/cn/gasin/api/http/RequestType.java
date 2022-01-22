package cn.gasin.api.http;

/**
 * 三种请求类型: 注册, 心跳, 取消注册
 */
public enum RequestType {
    REGISTER,
    HEARTBEAT,
    UNREGISTER
}