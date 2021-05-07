package cn.gasin.client;

import cn.gasin.api.http.Response;
import cn.gasin.api.http.ResponseStatus;
import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.client.http.HttpClient;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static cn.gasin.client.config.ClientConfig.INSTANCE_ID;
import static cn.gasin.client.config.ClientConfig.SERVICE_NAME;


/**
 * client的心跳服务
 */
@Log4j2
@AllArgsConstructor
public class Heartbeat extends Thread {

    private final RegisterClientWorker registerClientWorker;
    private final HttpClient httpClient;

    @Override
    public void run() {
        while (registerClientWorker.running()) {
            // 1. 睡一小会, 发送心跳请求
            try {
                HeartbeatRequest heartbeatRequest = new HeartbeatRequest().setInstanceId(INSTANCE_ID).setServiceName(SERVICE_NAME);
                Response hbResponse = httpClient.sendHeartbeat(heartbeatRequest);
                if (ResponseStatus.SUCCESS.equals(hbResponse.getStatus())) {
                    log.info("heartbeat succ: {}", System.currentTimeMillis());
                } else {
                    // 异常处理: 这里如果注册不成功, 要有很多事情做了
                    log.error("heartbeat failed: {}: {}", System.currentTimeMillis(), hbResponse.getMessage());
                }
                sleep(30 * 1000);
            } catch (InterruptedException e) {
                log.error("注册被中断");
                break;
            } catch (Exception e) {
                log.error("注册出问题了. 等会重试");
                sleepAWhile(30 * 1000);
            }
        }
    }

    private void sleepAWhile(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error("sleep interrupt:{}, ", Thread.interrupted(), e);
        }
    }

    public void shutDown() {
        this.interrupt();
    }
}
