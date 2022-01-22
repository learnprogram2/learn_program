package cn.gasin.fs.datanode;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 向NameNode注册的服务.
 */
@Log4j2
public class NameNodeGroupOfferService {

    private volatile boolean shouldRun;

    // 和主nameNode通讯的actor组件
    private NameNodeServiceActor activeServiceActor;
    // 和standby-nameNode通讯的actor组件
    private NameNodeServiceActor standbyServiceActor;

    private List<NameNodeServiceActor> serviceActorList;


    public NameNodeGroupOfferService() {
        activeServiceActor = new NameNodeServiceActor();
        standbyServiceActor = new NameNodeServiceActor();
        shouldRun = true;
        serviceActorList.add(activeServiceActor);
        serviceActorList.add(standbyServiceActor);
    }

    /** 启动对NameNode的通讯服务 */
    public void start() {
        // 注册自己
        while (shouldRun && !register()) {
            log.warn("register wailed, retry.");
        }
    }

    public void stop() {
        shouldRun = false;
        for (NameNodeServiceActor nameNodeServiceActor : serviceActorList) {
            nameNodeServiceActor.stop();
        }
    }

    private boolean register() {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(2);
            activeServiceActor.register(countDownLatch);
            standbyServiceActor.register(countDownLatch);
            countDownLatch.await();
            return true;
        } catch (InterruptedException e) {
            log.error("nameNode group offer service register failed because interrupted.", e);
            return false;
        }
    }
}
