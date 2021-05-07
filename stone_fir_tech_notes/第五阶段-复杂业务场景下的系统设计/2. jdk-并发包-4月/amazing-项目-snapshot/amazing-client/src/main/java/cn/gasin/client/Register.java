package cn.gasin.client;

import cn.gasin.api.http.Response;
import cn.gasin.api.http.ResponseStatus;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.client.http.HttpClient;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static cn.gasin.client.config.ClientConfig.*;


/**
 * 注册服务
 */
@Log4j2
@AllArgsConstructor
public class Register extends Thread {

    private final HttpClient httpClient;


    @Override
    public void run() {
        super.run();

        // 1. 读取配置, 拼装注册请求
        RegisterRequest registerRequest = new RegisterRequest()
                .setServiceName(SERVICE_NAME).setInstanceId(INSTANCE_ID)
                .setInstanceIp(INSTANCE_IP).setInstancePort(INSTANCE_PORT);
        // 2. 使用工具发送请求.
        Response response = httpClient.sendRegisterRequest(registerRequest);
        if (ResponseStatus.SUCCESS.equals(response.getStatus())) {
            log.info("register success");
        } else {
            // 3. 重试机制, 或者异常机制
            log.error("register failed:{}", response.getMessage());
            return;
        }
    }

    public void shutDown() {
        // 什么也不用做暂时.
    }
}
