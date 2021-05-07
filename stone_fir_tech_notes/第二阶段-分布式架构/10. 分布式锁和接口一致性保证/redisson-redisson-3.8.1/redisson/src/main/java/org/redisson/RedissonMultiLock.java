/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.redisson.misc.TransferListener;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * Groups multiple independent locks and manages them as one lock.
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonMultiLock implements Lock {

    final List<RLock> locks = new ArrayList<RLock>();
    
    /**
     *  用法:
     * RLock lock1 = clientInstance1.getLock("lock1");
     * RLock lock2 = clientInstance2.getLock("lock2");
     * RLock lock3 = clientInstance3.getLock("lock3");
     *
     * RedissonMultiLock lock = new RedissonMultiLock(lock1, lock2, lock3);
     * lock.lock();
     * // perform long running operation...
     * lock.unlock();
     *
     * Creates instance with multiple {@link RLock} objects.
     * Each RLock object could be created by own Redisson instance.
     *
     * @param locks - array of locks
     */
    public RedissonMultiLock(RLock... locks) {
        if (locks.length == 0) {
            throw new IllegalArgumentException("Lock objects are not defined");
        }
        this.locks.addAll(Arrays.asList(locks));
    }

    // TODO: E.1: multiLock拿锁.
    @Override
    public void lock() {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            // 这里在当前线程抛出一个InterruptException异常.
            Thread.currentThread().interrupt();
        }
    }

    public void lock(long leaseTime, TimeUnit unit) {
        try {
            lockInterruptibly(leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit) {
        long baseWaitTime = locks.size() * 1500;
        long waitTime = -1;
        if (leaseTime == -1) {
            waitTime = baseWaitTime;
            unit = TimeUnit.MILLISECONDS;
        } else {
            waitTime = unit.toMillis(leaseTime);
            if (waitTime <= 2000) {
                waitTime = 2000;
            } else if (waitTime <= baseWaitTime) {
                waitTime = ThreadLocalRandom.current().nextLong(waitTime/2, waitTime);
            } else {
                waitTime = ThreadLocalRandom.current().nextLong(baseWaitTime, waitTime);
            }
            waitTime = unit.convert(waitTime, TimeUnit.MILLISECONDS);
        }

        RPromise<Void> result = new RedissonPromise<Void>();
        tryLockAsync(leaseTime, unit, waitTime, result);
        return result;
    }

    protected void tryLockAsync(final long leaseTime, final TimeUnit unit, final long waitTime, final RPromise<Void> result) {
        tryLockAsync(waitTime, leaseTime, unit).addListener(new FutureListener<Boolean>() {
            @Override
            public void operationComplete(Future<Boolean> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }
                
                if (future.getNow()) {
                    result.trySuccess(null);
                } else {
                    tryLockAsync(leaseTime, unit, waitTime, result);
                }
            }
        });
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {
        lockInterruptibly(-1, null);
    }

    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {
        // 1. 根据锁数量设置等待时间, 每个锁1.5s.
        long baseWaitTime = locks.size() * 1500;
        long waitTime = -1;
        if (leaseTime == -1) {
            waitTime = baseWaitTime; // 没设置lease时间就把等待时间设置成用每个锁1.5s
            unit = TimeUnit.MILLISECONDS;
        } else {
            // 设置了leaseTime就取leaseTime和baseWaitTime之间的数. 太复杂了, 滚蛋吧.
            waitTime = unit.toMillis(leaseTime); // 设置了lease时间就把 等待时间 依赖 lease时间
            if (waitTime <= 2000) { // 最少要等2s
                waitTime = 2000;
            } else if (waitTime <= baseWaitTime) { // 如果设置的时间比每个锁1.5s要小, 就取[leaseTime/2, leaseTime]的随机
                waitTime = ThreadLocalRandom.current().nextLong(waitTime/2, waitTime);
            } else {
                waitTime = ThreadLocalRandom.current().nextLong(baseWaitTime, waitTime);
            }
            waitTime = unit.convert(waitTime, TimeUnit.MILLISECONDS);
        }
        
        while (true) {
            // 死循环尝试加锁,
            if (tryLock(waitTime, leaseTime, unit)) {
                return;
            }
        }
    }

    @Override
    public boolean tryLock() {
        try {
            return tryLock(-1, -1, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    protected void unlockInner(Collection<RLock> locks) {
        List<RFuture<Void>> futures = new ArrayList<RFuture<Void>>(locks.size());
        for (RLock lock : locks) {
            futures.add(lock.unlockAsync());
        }

        for (RFuture<Void> unlockFuture : futures) {
            unlockFuture.awaitUninterruptibly();
        }
    }
    
    protected RFuture<Void> unlockInnerAsync(Collection<RLock> locks, long threadId) {
        if (locks.isEmpty()) {
            return RedissonPromise.newSucceededFuture(null);
        }
        
        final RPromise<Void> result = new RedissonPromise<Void>();
        final AtomicInteger counter = new AtomicInteger(locks.size());
        for (RLock lock : locks) {
            lock.unlockAsync(threadId).addListener(new FutureListener<Void>() {
                @Override
                public void operationComplete(Future<Void> future) throws Exception {
                    if (!future.isSuccess()) {
                        result.tryFailure(future.cause());
                        return;
                    }
                    
                    if (counter.decrementAndGet() == 0) {
                        result.trySuccess(null);
                    }
                }
            });
        }
        return result;
    }


    @Override
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        return tryLock(waitTime, -1, unit);
    }
    
    protected int failedLocksLimit() {
        return 0;
    }

    // TODO: E.1.2: multiLock拿锁. 实际逻辑
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
//        try {
//            return tryLockAsync(waitTime, leaseTime, unit).get();
//        } catch (ExecutionException e) {
//            throw new IllegalStateException(e);
//        }
        long newLeaseTime = -1;
        if (leaseTime != -1) { // 设置过leaseTime, 就把newLeaseTime = 小锁个数 * 3, 不走这里.
            newLeaseTime = unit.toMillis(waitTime)*2;
        }
        
        long time = System.currentTimeMillis();
        long remainTime = -1;
        if (waitTime != -1) { // waitTime一般不会为-1, remainTIme设置成要等待的时间.
            remainTime = unit.toMillis(waitTime);
        }
        long lockWaitTime = calcLockWaitTime(remainTime);

        // 默认不允许有failed的lock
        int failedLocksLimit = failedLocksLimit();
        List<RLock> acquiredLocks = new ArrayList<RLock>(locks.size());
        // 2. 遍历所有的lock, 一个个拿锁.
        for (ListIterator<RLock> iterator = locks.listIterator(); iterator.hasNext();) {
            RLock lock = iterator.next();
            boolean lockAcquired;
            try {
                if (waitTime == -1 && leaseTime == -1) {
                    // 自始至终没有设置过waitTime和leaseTime的时候... 正常拿lock这里不会走的
                    lockAcquired = lock.tryLock();
                } else {
                    // awaitTime = 剩余的wait时间(lock数*3s), newLeaseTime=lock数*3s
                    long awaitTime = Math.min(lockWaitTime, remainTime);
                    lockAcquired = lock.tryLock(awaitTime, newLeaseTime, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                lockAcquired = false;
            }

            if (lockAcquired) {
                // 4. 锁成功了就存起来
                acquiredLocks.add(lock);
            } else {
                // 5. 锁失败了, 检查失败锁是否达到了 limit, 达到了就跳出去.
                if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                    break;
                }

                // 如果不允许失败,
                if (failedLocksLimit == 0) {
                    // 就把拿到的锁都是放掉
                    unlockInner(acquiredLocks);
                    if (waitTime == -1 && leaseTime == -1) {
                        return false;
                    }
                    // 重新来过, 从头再遍历
                    failedLocksLimit = failedLocksLimit();
                    acquiredLocks.clear();
                    // reset iterator
                    while (iterator.hasPrevious()) {
                        iterator.previous();
                    }
                } else {
                    // 允许失败就把允许失败的个数减一.
                    failedLocksLimit--;
                }
            }

            // 总等待时间还剩下, 就重算一下, 到点了就释放, 返回false
            if (remainTime != -1) {
                remainTime -= (System.currentTimeMillis() - time);
                time = System.currentTimeMillis();
                if (remainTime <= 0) {
                    unlockInner(acquiredLocks);
                    return false;
                }
            }
        }

        // 我们没设置过. 但是如果设置了leaseTime, 把所有的锁重新设置一下成最初的的leaseTime
        if (leaseTime != -1) {
            List<RFuture<Boolean>> futures = new ArrayList<RFuture<Boolean>>(acquiredLocks.size());
            for (RLock rLock : acquiredLocks) {
                // 所有的lock都设置一下lease时间.
                RFuture<Boolean> future = rLock.expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS);
                futures.add(future);
            }
            
            for (RFuture<Boolean> rFuture : futures) {
                rFuture.syncUninterruptibly();
            }
        }
        
        return true;
    }

    private void tryAcquireLockAsync(final ListIterator<RLock> iterator, final List<RLock> acquiredLocks, final RPromise<Boolean> result, 
            final long lockWaitTime, final long waitTime, final long leaseTime, final long newLeaseTime, 
            final AtomicLong remainTime, final AtomicLong time, final AtomicInteger failedLocksLimit, final TimeUnit unit, final long threadId) {
        if (!iterator.hasNext()) {
            checkLeaseTimeAsync(acquiredLocks, result, leaseTime, unit);
            return;
        }

        final RLock lock = iterator.next();
        RPromise<Boolean> lockAcquired = new RedissonPromise<Boolean>();
        if (waitTime == -1 && leaseTime == -1) {
            lock.tryLockAsync(threadId)
                .addListener(new TransferListener<Boolean>(lockAcquired));
        } else {
            long awaitTime = Math.min(lockWaitTime, remainTime.get());
            lock.tryLockAsync(awaitTime, newLeaseTime, TimeUnit.MILLISECONDS, threadId)
                .addListener(new TransferListener<Boolean>(lockAcquired));;
        }
        
        lockAcquired.addListener(new FutureListener<Boolean>() {
            @Override
            public void operationComplete(Future<Boolean> future) throws Exception {
                boolean lockAcquired = false;
                if (future.getNow() != null) {
                    lockAcquired = future.getNow();
                }
                
                if (lockAcquired) {
                    acquiredLocks.add(lock);
                } else {
                    if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                        checkLeaseTimeAsync(acquiredLocks, result, leaseTime, unit);
                        return;
                    }

                    if (failedLocksLimit.get() == 0) {
                        unlockInnerAsync(acquiredLocks, threadId).addListener(new FutureListener<Void>() {
                            @Override
                            public void operationComplete(Future<Void> future) throws Exception {
                                if (!future.isSuccess()) {
                                    result.tryFailure(future.cause());
                                    return;
                                }
                                
                                if (waitTime == -1 && leaseTime == -1) {
                                    result.trySuccess(false);
                                    return;
                                }
                                
                                failedLocksLimit.set(failedLocksLimit());
                                acquiredLocks.clear();
                                // reset iterator
                                while (iterator.hasPrevious()) {
                                    iterator.previous();
                                }
                                
                                checkRemainTimeAsync(iterator, acquiredLocks, result, 
                                        lockWaitTime, waitTime, leaseTime, newLeaseTime, 
                                        remainTime, time, failedLocksLimit, unit, threadId);
                            }
                        });
                        return;
                    } else {
                        failedLocksLimit.decrementAndGet();
                    }
                }
                
                checkRemainTimeAsync(iterator, acquiredLocks, result, 
                        lockWaitTime, waitTime, leaseTime, newLeaseTime, 
                        remainTime, time, failedLocksLimit, unit, threadId);
            }
        });
    }

    private void checkLeaseTimeAsync(List<RLock> acquiredLocks, final RPromise<Boolean> result, long leaseTime, TimeUnit unit) {
        if (leaseTime != -1) {
            final AtomicInteger counter = new AtomicInteger(locks.size());
            for (RLock rLock : acquiredLocks) {
                RFuture<Boolean> future = rLock.expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS);
                future.addListener(new FutureListener<Boolean>() {
                    @Override
                    public void operationComplete(Future<Boolean> future) throws Exception {
                        if (!future.isSuccess()) {
                            result.tryFailure(future.cause());
                            return;
                        }
                        
                        if (counter.decrementAndGet() == 0) {
                            result.trySuccess(true);
                        }
                    }
                });
            }
            return;
        }
        
        result.trySuccess(true);
    }
    
    protected void checkRemainTimeAsync(ListIterator<RLock> iterator, List<RLock> acquiredLocks, final RPromise<Boolean> result, 
            long lockWaitTime, long waitTime, long leaseTime, long newLeaseTime, 
            AtomicLong remainTime, AtomicLong time, AtomicInteger failedLocksLimit, TimeUnit unit, long threadId) {
        if (remainTime.get() != -1) {
            remainTime.addAndGet(-(System.currentTimeMillis() - time.get()));
            time.set(System.currentTimeMillis());;
            if (remainTime.get() <= 0) {
                unlockInnerAsync(acquiredLocks, threadId).addListener(new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        if (!future.isSuccess()) {
                            result.tryFailure(future.cause());
                            return;
                        }
                        
                        result.trySuccess(false);
                    }
                });
                return;
            }
        }
        
        tryAcquireLockAsync(iterator, acquiredLocks, result, lockWaitTime, waitTime, 
                leaseTime, newLeaseTime, remainTime, time, failedLocksLimit, unit, threadId);
    }
    
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit) {
        RPromise<Boolean> result = new RedissonPromise<Boolean>();
        long newLeaseTime = -1;
        if (leaseTime != -1) {
            newLeaseTime = unit.toMillis(waitTime)*2;
        }
        
        AtomicLong time = new AtomicLong(System.currentTimeMillis());
        AtomicLong remainTime = new AtomicLong(-1);
        if (waitTime != -1) {
            remainTime.set(unit.toMillis(waitTime));
        }
        long lockWaitTime = calcLockWaitTime(remainTime.get());
        
        AtomicInteger failedLocksLimit = new AtomicInteger(failedLocksLimit());
        List<RLock> acquiredLocks = new ArrayList<RLock>(locks.size());
        long threadId = Thread.currentThread().getId();
        tryAcquireLockAsync(locks.listIterator(), acquiredLocks, result, 
                lockWaitTime, waitTime, leaseTime, newLeaseTime, 
                remainTime, time, failedLocksLimit, unit, threadId);
        
        return result;
    }

    
    protected long calcLockWaitTime(long remainTime) {
        return remainTime;
    }

    public RFuture<Void> unlockAsync(long threadId) {
        return unlockInnerAsync(locks, threadId);
    }
    
    @Override
    public void unlock() {
        List<RFuture<Void>> futures = new ArrayList<RFuture<Void>>(locks.size());

        for (RLock lock : locks) {
            futures.add(lock.unlockAsync());
        }

        for (RFuture<Void> future : futures) {
            future.syncUninterruptibly();
        }
    }


    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
