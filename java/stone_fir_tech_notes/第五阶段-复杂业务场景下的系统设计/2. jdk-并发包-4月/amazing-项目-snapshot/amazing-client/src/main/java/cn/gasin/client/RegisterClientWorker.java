package cn.gasin.client;

import cn.gasin.client.http.HttpClient;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;


/**
 * 这个是client组件的入口: registry的客户端服务入口, 可以把RegisterClientWorker看成服务的入口和总和.
 */
@Log4j2
public class RegisterClientWorker extends Thread {


    // 这个可以设为volatile. atomic也是维护了一个volatile的变量.
    private volatile boolean running = true;

    // 维护的各种服务
    HttpClient httpClient;
    Register register;
    Heartbeat heartbeat;
    Registry registry;

    public static void main(String[] args) {
        new RegisterClientWorker().start();
    }

    public RegisterClientWorker() {
        httpClient = new HttpClient();
    }

    @SneakyThrows
    @Override
    public void run() {
        super.run();

        // register
        register = new Register(httpClient);
        register.start();
        register.join();

        // heartbeat
        heartbeat = new Heartbeat(this, httpClient);
        heartbeat.start();

        // pull registry
        registry = new Registry(this, httpClient);
        register.start();

        // enable unregister request sender
    }


    // 关闭服务.
    public void shutdown() {
        running = false;
        register.shutDown();
        heartbeat.shutDown();
        registry.shutDown();
        httpClient.instanceOffline();
        httpClient.shutDown();
    }


    public boolean running() {
        return running;
    }

}
