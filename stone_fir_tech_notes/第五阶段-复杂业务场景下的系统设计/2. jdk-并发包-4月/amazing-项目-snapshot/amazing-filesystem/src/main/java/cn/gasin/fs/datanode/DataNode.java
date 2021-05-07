package cn.gasin.fs.datanode;

import lombok.extern.log4j.Log4j2;

/**
 * 这里是datanode的入口类型
 */
@Log4j2
public class DataNode {
    private volatile boolean shouldRun = false;

    private NameNodeGroupOfferService offerService;

    public DataNode() {
        shouldRun = true;
        offerService = new NameNodeGroupOfferService();
        offerService.start();
    }

    private void start() {
        while (shouldRun) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("data node was interrupted, stop datanode service");
                shouldRun = false;
            }
        }
    }


    public static void main(String[] args) {
        DataNode dataNode = new DataNode();
        dataNode.start();
    }


}
