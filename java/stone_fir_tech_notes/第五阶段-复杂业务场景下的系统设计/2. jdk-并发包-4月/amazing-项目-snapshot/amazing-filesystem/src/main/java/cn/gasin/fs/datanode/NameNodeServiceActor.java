package cn.gasin.fs.datanode;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * 通讯组件
 */
@Log4j2
public class NameNodeServiceActor {
    /**
     * 向nameNode注册, TODO 还没有具体实现, 还没有TCP框架支持和NameNode通讯地址.
     */
    public void register(CountDownLatch countDownLatch) {
        CompletableFuture<Void> registerRes = CompletableFuture.runAsync(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                // 通讯
                log.info("register to NameNode...");
                Thread.sleep(1000);
            }
        });
        registerRes.whenComplete((unused, throwable) -> countDownLatch.countDown());
    }

    public void stop() {

    }
}
