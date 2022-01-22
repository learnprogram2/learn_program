package cn.gasin.api.server.cluster;

import cn.gasin.api.http.BaseClientRequest;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
public class PeersReplicateBatch {
    @Getter
    private List<BaseClientRequest> batchList;


    // 创建一个batch, 加个锁吧.
    public synchronized static PeersReplicateBatch createBatch(LinkedBlockingQueue<BaseClientRequest> cacheQueue) {
        PeersReplicateBatch batch = new PeersReplicateBatch();
        batch.batchList = new ArrayList<>(cacheQueue.size());

        Iterator<BaseClientRequest> iterator = cacheQueue.iterator();
        while (iterator.hasNext()) {
            BaseClientRequest next = iterator.next();
            batch.batchList.add(next);
            iterator.remove();
        }

        return batch;
    }
}
