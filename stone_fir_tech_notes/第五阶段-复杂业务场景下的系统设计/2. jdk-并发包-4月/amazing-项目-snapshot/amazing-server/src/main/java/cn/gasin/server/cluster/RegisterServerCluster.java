package cn.gasin.server.cluster;

import cn.gasin.api.server.cluster.PeersReplicateBatch;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Log4j2
@Component
public class RegisterServerCluster {
    @Getter
    private LinkedBlockingQueue<String> peers = new LinkedBlockingQueue<>();

    public RegisterServerCluster() {
        // TODO init peers from config.
    }

}
