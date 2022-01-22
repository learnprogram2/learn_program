package cn.gasin.server.controller;

import cn.gasin.api.http.BaseClientRequest;
import cn.gasin.api.http.Response;
import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.api.server.cluster.PeersReplicateBatch;
import cn.gasin.server.cluster.PeersReplicator;
import cn.gasin.server.registry.Registry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接受peers的同步请求
 */
@Log4j2
@RestController("/peer")
public class PeersReplicateController {

    @Autowired
    private Registry registry;

    /** cluster 同步的组件 */
    @Autowired
    private PeersReplicator peersReplicator;

    @Autowired
    private RegisterController registerController;


    @PostMapping("/batch")
    public Response republicBatch(PeersReplicateBatch batch) {

        List<BaseClientRequest> requestListFromPeers = batch.getBatchList();
        for (BaseClientRequest req : requestListFromPeers) {
            switch (req.getRequestType()) {
                case REGISTER:
                    registerController.register((RegisterRequest) req);
                    break;
                case UNREGISTER:
                    registerController.instanceOffline((RegisterRequest) req);
                    break;
                case HEARTBEAT:
                    registerController.register((HeartbeatRequest) req);
            }
        }

        return Response.success("succ~");
    }

}
