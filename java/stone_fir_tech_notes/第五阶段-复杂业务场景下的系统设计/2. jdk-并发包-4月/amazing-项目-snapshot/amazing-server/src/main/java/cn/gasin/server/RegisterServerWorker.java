package cn.gasin.server;

import cn.gasin.server.registry.RegistryExpel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RegisterServerWorker extends Thread {

    public static void main(String[] args) {
        new RegisterServerWorker().start();
    }

    @Override
    public void run() {
        super.run();
        // receive register request:
        // start a web server
        ConfigurableApplicationContext context = SpringApplication.run(RegisterServerWorker.class);


        // check unlive instance
        // internal 60s, timeout 90s.
        try {
            context.getBean(RegistryExpel.class).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
