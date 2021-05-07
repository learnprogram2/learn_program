package cn.curator.demo;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Arrays;

public class Application {

    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("gspdatazk01u:2181,gspdatazk02u:2181,gspdatazk03u:2181")
                .authorization("digest", "subuser:sub123".getBytes())
                .sessionTimeoutMs(20000)
                .connectionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();

        // 这一个锁一个JVM就一个就好了, 因为里面判断isAcquiredInThisProcess是不是在本process里拿的锁, 就按照这个obj里面存的thread数量看的.
        InterProcessLock lock = new InterProcessMutex(curatorFramework, "/168043/subscription/QA/locks/lock_01");
        lock.acquire();
        Thread.sleep(10000);
        lock.release();

        curatorFramework.close();

        System.out.println("yes");

        InterProcessSemaphoreV2 semaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/168043/subscription/QA/locks/semaphore_01", 3);

        Lease acquire1 = semaphoreV2.acquire();
        Lease acquire2 = semaphoreV2.acquire();
        Lease acquire3 = semaphoreV2.acquire();
        semaphoreV2.returnLease(acquire1);
        semaphoreV2.returnAll(Arrays.asList(acquire2, acquire3));

        InterProcessSemaphoreMutex semaphoreMutex = new InterProcessSemaphoreMutex(curatorFramework, "/168043/subscription/QA/locks/semaphore_01");
        semaphoreMutex.acquire();


    }
}
