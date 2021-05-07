package cn.gasin.fs.namenode.rpc;

import cn.gasin.fs.namenode.FSNameSystem;
import lombok.extern.log4j.Log4j2;

/**
 * NameNode的rpc服务的接口
 */
@Log4j2
public class NameNodeRpcServer {
    // service to deal with requests.
    private FSNameSystem fsNameSystem;

    public NameNodeRpcServer(FSNameSystem fsNamesystem) {
        this.fsNameSystem = fsNamesystem;
    }

    public void start() {
        log.info("namenode rpc server starting");
        // todo: implements this method to complete this rpcServer.
    }

    /**
     * API: make directory.
     *
     * @param path: the path of the new directory.
     */
    public Boolean mkdir(String path) throws Exception {
        return fsNameSystem.mkdir(path);
    }
}
