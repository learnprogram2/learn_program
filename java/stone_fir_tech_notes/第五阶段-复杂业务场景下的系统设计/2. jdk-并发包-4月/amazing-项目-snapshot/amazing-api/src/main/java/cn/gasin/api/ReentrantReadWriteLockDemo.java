package cn.gasin.api;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockDemo {


    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.lock();
        readLock.unlock();

        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        writeLock.unlock();

        readLock.lock();
        writeLock.lock();
    }


//    /**
//     * Implements interruptible condition wait.
//     * 可打断的condition的wait方法
//     * <p>
//     * 1. 如果当前线程被interrupted了, 就正常抛出InterruptedException.
//     * 2. 这个方法是阻塞方法, 用signal或者interrupt方法可以中断.
//     */
//    public final void await() throws InterruptedException {
//        if (Thread.interrupted())
//            throw new InterruptedException();
//        // 1. 添加一个condition状态的node到一个conditionObject里面维护的队列里.
//        AbstractQueuedSynchronizer.Node node = addConditionWaiter();
//        // 2. 尝试把自己拿到锁state全部释放掉, 然后存起来state. 如果别的锁就会报错哦
//        int savedState = fullyRelease(node);
//        int interruptMode = 0;
//        // 3. 如果当前线程不在AQS的拿锁队列里面, 就part等着. 如果不被interrupt也就循环着.
//        //    因为signal已经把自己放在了拿锁队列里, 然后这里就会跳出来
//        while (!isOnSyncQueue(node)) {
//            LockSupport.park(this);
//            // 如果被打断也会break.
//            if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
//                break;
//        }
//        // 4. 阻塞拿锁, 拿锁成功返回false(没有interrupt), 也就过了..
//        if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
//            interruptMode = REINTERRUPT;
//        // 5. 这是更新了一下wait的队列
//        if (node.nextWaiter != null) // clean up if cancelled
//            unlinkCancelledWaiters();
//        if (interruptMode != 0)
//            reportInterruptAfterWait(interruptMode);
//    }




}
