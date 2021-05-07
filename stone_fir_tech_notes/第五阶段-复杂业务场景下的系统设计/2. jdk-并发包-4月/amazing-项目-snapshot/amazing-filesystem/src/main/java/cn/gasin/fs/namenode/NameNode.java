package cn.gasin.fs.namenode;


import cn.gasin.fs.namenode.rpc.NameNodeRpcServer;
import lombok.extern.log4j.Log4j2;

/**
 * the main class of FS.
 */
@Log4j2
public class NameNode {
    private volatile boolean shouldRun = false;

    // services
    private FSNameSystem fsNamesystem;
    private NameNodeRpcServer rpcServer;

    public NameNode() {
        initialize();
    }

    /**
     * prepare all steps before running.
     */
    public void initialize() {
        // maintain all the metadata.
        this.fsNamesystem = new FSNameSystem();
        // rpc protocol.
        this.rpcServer = new NameNodeRpcServer(fsNamesystem);
        this.rpcServer.start();
    }

    // 关闭的时候把flag关闭, 然后interrupt当前obj就好了就好了
    public synchronized void join() {
        while (shouldRun) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.info("name node was interrupted");
            }
        }
    }

    public static void main(String[] args) {
        NameNode nn = new NameNode();
        nn.join(); // 始终阻塞在这里running.
    }

}
