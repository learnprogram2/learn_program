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

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;

import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.client.RedisException;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommand.ValueType;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.IntegerReplayConvertor;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.redisson.pubsub.LockPubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.internal.PlatformDependent;

/**
 * Distributed implementation of {@link java.util.concurrent.locks.Lock}
 * Implements reentrant lock.<br>
 * Lock will be removed automatically if client disconnects.
 * <p>
 * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order.
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonLock extends RedissonExpirable implements RLock {

    public static class ExpirationEntry {
        
        private long threadId;
        private Timeout timeout;
        
        public ExpirationEntry(long threadId, Timeout timeout) {
            super();
            this.threadId = threadId;
            this.timeout = timeout;
        }
        
        public long getThreadId() {
            return threadId;
        }
        
        public Timeout getTimeout() {
            return timeout;
        }
        
    }
    
    private static final Logger log = LoggerFactory.getLogger(RedissonLock.class);
    
    private static final ConcurrentMap<String, ExpirationEntry> expirationRenewalMap = PlatformDependent.newConcurrentHashMap();
    protected long internalLockLeaseTime;

    final UUID id;
    final String entryName;

    protected static final LockPubSub PUBSUB = new LockPubSub();

    final CommandAsyncExecutor commandExecutor;

    public RedissonLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;
        this.id = commandExecutor.getConnectionManager().getId();
        this.internalLockLeaseTime = commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout();
        this.entryName = id + ":" + name;
    }

    protected String getEntryName() {
        return entryName;
    }

    String getChannelName() {
        // redisson_lock__channel:{keyName} 这个keyName是我们getLock填的lockName
        return prefixName("redisson_lock__channel", getName());
    }

    protected String getLockName(long threadId) {
        return id + ":" + threadId;
    }

    @Override
    public void lock() {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        try {
            lockInterruptibly(leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {
        lockInterruptibly(-1, null);
    }

    @Override
    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(leaseTime, unit, threadId);
        // lock acquired : 这里, 如果返回的ttl==null说明上锁成功, 如果不等于null, 说明没上锁, 返回的别人的有效期. 看上锁的script可以明白.
        if (ttl == null) {
            return;
        }

        // TODO: B.1 上锁失败的起始点.
        // 这里是订阅一个channel, "redisson_lock__channel:{keyName}"
        RFuture<RedissonLockEntry> future = subscribe(threadId);
        commandExecutor.syncSubscription(future);

        try {
            while (true) {
                // 再尝试拿一下锁, 拿到了就break.
                ttl = tryAcquire(leaseTime, unit, threadId);
                // lock acquired
                if (ttl == null) {
                    break;
                }

                // 没拿到看看当前锁的ttl, 如果还存活, 就等ttl时长.
                // waiting for message
                if (ttl >= 0) {
                    getEntry(threadId).getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                } else {
                    // 如果当前lock过期了, 就直接acquire拿了. FIXME: ??这个不太清楚是什么.
                    getEntry(threadId).getLatch().acquire();
                }
            }
        } finally {
            unsubscribe(future, threadId);
        }
//        get(lockAsync(leaseTime, unit));
    }
    
    private Long tryAcquire(long leaseTime, TimeUnit unit, long threadId) {
        return get(tryAcquireAsync(leaseTime, unit, threadId));
    }
    
    private RFuture<Boolean> tryAcquireOnceAsync(long leaseTime, TimeUnit unit, final long threadId) {
        if (leaseTime != -1) {
            return tryLockInnerAsync(leaseTime, unit, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
        }
        RFuture<Boolean> ttlRemainingFuture = tryLockInnerAsync(commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout(), TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
        ttlRemainingFuture.addListener(new FutureListener<Boolean>() {
            @Override
            public void operationComplete(Future<Boolean> future) throws Exception {
                if (!future.isSuccess()) {
                    return;
                }

                Boolean ttlRemaining = future.getNow();
                // lock acquired
                if (ttlRemaining) {
                    scheduleExpirationRenewal(threadId);
                }
            }
        });
        return ttlRemainingFuture;
    }

    private <T> RFuture<Long> tryAcquireAsync(long leaseTime, TimeUnit unit, final long threadId) {
        if (leaseTime != -1) {
            // 如果设定锁释放时间, 直接拿锁, 不会设置看门狗
            return tryLockInnerAsync(leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
        }
        RFuture<Long> ttlRemainingFuture = tryLockInnerAsync(commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout(), TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
        ttlRemainingFuture.addListener(new FutureListener<Long>() {
            @Override
            public void operationComplete(Future<Long> future) throws Exception {
                if (!future.isSuccess()) {
                    return;
                }
                // TODO: B.0: 这里拿到future的结果, 如果上锁成功了, 就预约renewal
                Long ttlRemaining = future.getNow();
                // lock acquired
                if (ttlRemaining == null) {
                    scheduleExpirationRenewal(threadId);
                }
            }
        });
        return ttlRemainingFuture;
    }

    @Override
    public boolean tryLock() {
        return get(tryLockAsync());
    }

    // 重要: 上锁成功后的续约. 注册一个leaseTime/3 间隔的定时任务.
    private void scheduleExpirationRenewal(final long threadId) {
        if (expirationRenewalMap.containsKey(getEntryName())) {
            return;
        }

        Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {

                // 执行一段script, 这个只要知道: keyName, LockName(connectionMgrID:threadId) 就可以续约了.
                // 续约成功返回1, lockName已经不存在了就返回0.
                RFuture<Boolean> future = renewExpirationAsync(threadId);
                
                future.addListener(new FutureListener<Boolean>() {
                    @Override
                    public void operationComplete(Future<Boolean> future) throws Exception {
                        expirationRenewalMap.remove(getEntryName());
                        if (!future.isSuccess()) {
                            log.error("Can't update lock " + getName() + " expiration", future.cause());
                            return;
                        }
                        
                        if (future.getNow()) { // 续约成功了.
                            // reschedule itself
                            scheduleExpirationRenewal(threadId);
                        }
                        // 如果返回false(0), 就是没有这个lockName了, 不用task再执行了.
                    }
                });
            }

        }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);

        if (expirationRenewalMap.putIfAbsent(getEntryName(), new ExpirationEntry(threadId, task)) != null) {
            // 如果exporationRenewalMap里面有(也就是返回!=null), 就把当前的task取消掉.
            task.cancel();
        }
    }

    protected RFuture<Boolean> renewExpirationAsync(long threadId) {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                // 再把keyName续约internalLockLeaseTime长的时间.
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                            "return 1; " +
                        "end; " +
                        // lockName已经不存在了就返回0.
                        "return 0;",
            Collections.<Object>singletonList(getName()), 
            internalLockLeaseTime, getLockName(threadId));
    }

    void cancelExpirationRenewal(Long threadId) {
        ExpirationEntry task = expirationRenewalMap.get(getEntryName());
        if (task != null && (threadId == null || task.getThreadId() == threadId)) {
            expirationRenewalMap.remove(getEntryName());
            task.getTimeout().cancel();
        }
    }

    <T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        internalLockLeaseTime = unit.toMillis(leaseTime);

        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
                  "if (redis.call('exists', KEYS[1]) == 0) then " +
                      "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                      "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "return redis.call('pttl', KEYS[1]);",
                    Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
    }
    
    private void acquireFailed(long threadId) {
        get(acquireFailedAsync(threadId));
    }
    
    protected RFuture<Void> acquireFailedAsync(long threadId) {
        return RedissonPromise.newSucceededFuture(null);
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        long time = unit.toMillis(waitTime);
        long current = System.currentTimeMillis();
        final long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(leaseTime, unit, threadId);
        // 如果第一次拿到锁, 直接返回
        // lock acquired
        if (ttl == null) {
            return true;
        }

        // TODO: B.2.1: 拿超时锁失败, 处理重试.
        // 1. 总时间减去失败的拿锁操作消耗的时间
        time -= (System.currentTimeMillis() - current);
        if (time <= 0) {
            // 超时直接返回
            acquireFailed(threadId);
            return false;
        }

        // 2. 这里应该是监听channel里面的消息,然后睡一会.
        current = System.currentTimeMillis();
        final RFuture<RedissonLockEntry> subscribeFuture = subscribe(threadId);
        if (!await(subscribeFuture, time, TimeUnit.MILLISECONDS)) {
            if (!subscribeFuture.cancel(false)) {
                subscribeFuture.addListener(new FutureListener<RedissonLockEntry>() {
                    @Override
                    public void operationComplete(Future<RedissonLockEntry> future) throws Exception {
                        if (subscribeFuture.isSuccess()) {
                            unsubscribe(subscribeFuture, threadId);
                        }
                    }
                });
            }
            acquireFailed(threadId);
            return false;
        }

        try {
            // 3. 睡醒了再看, 如果超时了返回false.
            time -= (System.currentTimeMillis() - current);
            if (time <= 0) {
                acquireFailed(threadId);
                return false;
            }
        
            while (true) {
                long currentTime = System.currentTimeMillis();
                ttl = tryAcquire(leaseTime, unit, threadId);
                // lock acquired
                if (ttl == null) {
                    return true;
                }

                time -= (System.currentTimeMillis() - currentTime);
                if (time <= 0) {
                    acquireFailed(threadId);
                    return false;
                }

                // waiting for message
                currentTime = System.currentTimeMillis();
                if (ttl >= 0 && ttl < time) {
                    getEntry(threadId).getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                } else {
                    getEntry(threadId).getLatch().tryAcquire(time, TimeUnit.MILLISECONDS);
                }

                time -= (System.currentTimeMillis() - currentTime);
                if (time <= 0) {
                    acquireFailed(threadId);
                    return false;
                }
            }
        } finally {
            unsubscribe(subscribeFuture, threadId);
        }
//        return get(tryLockAsync(waitTime, leaseTime, unit));
    }

    protected RedissonLockEntry getEntry(long threadId) {
        return PUBSUB.getEntry(getEntryName());
    }

    protected RFuture<RedissonLockEntry> subscribe(long threadId) {
        return PUBSUB.subscribe(
                getEntryName(),   // "connectionManagerId:keyName", 第一个connectionMgrId就是一个UUID
                getChannelName(), // "redisson_lock__channel:{keyName}" 这个keyName是我们getLock填的lockName
                commandExecutor.getConnectionManager().getSubscribeService());
    }

    protected void unsubscribe(RFuture<RedissonLockEntry> future, long threadId) {
        PUBSUB.unsubscribe(future.getNow(), getEntryName(), getChannelName(), commandExecutor.getConnectionManager().getSubscribeService());
    }

    // TODO: B.2: 上有超时时间的锁入口
    @Override
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        return tryLock(waitTime, -1, unit);
    }

    @Override
    public void unlock() {
        try {
            // 2. 释放锁如果失败(不成功就会抛错), 就抛出去.
            get(
                    // 1. 先尝试释放锁
                    unlockAsync(Thread.currentThread().getId())
            );
        } catch (RedisException e) {
            if (e.getCause() instanceof IllegalMonitorStateException) {
                throw (IllegalMonitorStateException)e.getCause();
            } else {
                throw e;
            }
        }
        
//        Future<Void> future = unlockAsync();
//        future.awaitUninterruptibly();
//        if (future.isSuccess()) { // 如果释放锁成功, 就直接return
//            return;
//        }
//        if (future.cause() instanceof IllegalMonitorStateException) { // 释放锁失败的时候就把e抱出去.
//            throw (IllegalMonitorStateException)future.cause();
//        }
//        throw commandExecutor.convertException(future);
    }

    @Override
    public Condition newCondition() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forceUnlock() {
        return get(forceUnlockAsync());
    }

    @Override
    public RFuture<Boolean> forceUnlockAsync() {
        cancelExpirationRenewal(null);
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('del', KEYS[1]) == 1) then "
                + "redis.call('publish', KEYS[2], ARGV[1]); "
                + "return 1 "
                + "else "
                + "return 0 "
                + "end",
                Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.unlockMessage);
    }

    @Override
    public boolean isLocked() {
        return isExists();
    }

    @Override
    public RFuture<Boolean> isExistsAsync() {
        return commandExecutor.writeAsync(getName(), codec, RedisCommands.EXISTS, getName());
    }

    @Override
    public boolean isHeldByCurrentThread() {
        RFuture<Boolean> future = commandExecutor.writeAsync(getName(), LongCodec.INSTANCE, RedisCommands.HEXISTS, getName(), getLockName(Thread.currentThread().getId()));
        return get(future);
    }

    private static final RedisCommand<Integer> HGET = new RedisCommand<Integer>("HGET", ValueType.MAP_VALUE, new IntegerReplayConvertor(0));
    
    public RFuture<Integer> getHoldCountAsync() {
        return commandExecutor.writeAsync(getName(), LongCodec.INSTANCE, HGET, getName(), getLockName(Thread.currentThread().getId()));
    }
    
    @Override
    public int getHoldCount() {
        return get(getHoldCountAsync());
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return forceUnlockAsync();
    }

    @Override
    public RFuture<Void> unlockAsync() {
        long threadId = Thread.currentThread().getId();
        return unlockAsync(threadId);
    }

    protected RFuture<Boolean> unlockInnerAsync(long threadId) {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                // 1. 如果key没有了, 就channel里面把消息散播出去. 返回1
                "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('publish', KEYS[2], ARGV[1]); " +
                    "return 1; " +
                "end;" +
                        // 2. 如果lockName不存在, 说明锁已经被别人占了, 返回null
                "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                    "return nil;" +
                "end; " +
                        // 3. 可重入锁, 把当前lockName的value减一.
                "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                        // 4. 如果还不到释放锁, 就延期一下, 返回0.
                "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 0; " +
                "else " +
                        // 5. 如果应该释放锁, 就把keyName干掉, 然后发布消息, 返回1.
                    "redis.call('del', KEYS[1]); " +
                    "redis.call('publish', KEYS[2], ARGV[1]); " +
                    "return 1; "+
                "end; " +
                "return nil;",
                Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.unlockMessage, internalLockLeaseTime, getLockName(threadId));

    }

    // TODO: C.0 释放锁的入口
    @Override
    public RFuture<Void> unlockAsync(final long threadId) {
        final RPromise<Void> result = new RedissonPromise<Void>();
        // 1. 先尝试释放锁
        RFuture<Boolean> future = unlockInnerAsync(threadId);

        future.addListener(new FutureListener<Boolean>() {
            @Override
            public void operationComplete(Future<Boolean> future) throws Exception {
                if (!future.isSuccess()) {
                    // 如果没成功, 就把看门狗的renewal给干掉.
                    cancelExpirationRenewal(threadId);
                    result.tryFailure(future.cause());
                    return;
                }

                Boolean opStatus = future.getNow();
                if (opStatus == null) {
                    // 失败了, 就抛错
                    IllegalMonitorStateException cause = new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: "
                            + id + " thread-id: " + threadId);
                    result.tryFailure(cause);
                    return;
                }
                if (opStatus) {
                    // 成功了, 也把看门狗取消掉
                    cancelExpirationRenewal(null);
                }
                result.trySuccess(null);
            }
        });

        return result;
    }

    @Override
    public RFuture<Void> lockAsync() {
        return lockAsync(-1, null);
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit) {
        final long currentThreadId = Thread.currentThread().getId();
        return lockAsync(leaseTime, unit, currentThreadId);
    }

    @Override
    public RFuture<Void> lockAsync(long currentThreadId) {
        return lockAsync(-1, null, currentThreadId);
    }
    
    @Override
    public RFuture<Void> lockAsync(final long leaseTime, final TimeUnit unit, final long currentThreadId) {
        final RPromise<Void> result = new RedissonPromise<Void>();
        RFuture<Long> ttlFuture = tryAcquireAsync(leaseTime, unit, currentThreadId);
        ttlFuture.addListener(new FutureListener<Long>() {
            @Override
            public void operationComplete(Future<Long> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                Long ttl = future.getNow();

                // lock acquired
                if (ttl == null) {
                    if (!result.trySuccess(null)) {
                        unlockAsync(currentThreadId);
                    }
                    return;
                }

                final RFuture<RedissonLockEntry> subscribeFuture = subscribe(currentThreadId);
                subscribeFuture.addListener(new FutureListener<RedissonLockEntry>() {
                    @Override
                    public void operationComplete(Future<RedissonLockEntry> future) throws Exception {
                        if (!future.isSuccess()) {
                            result.tryFailure(future.cause());
                            return;
                        }

                        lockAsync(leaseTime, unit, subscribeFuture, result, currentThreadId);
                    }

                });
            }
        });

        return result;
    }

    private void lockAsync(final long leaseTime, final TimeUnit unit,
            final RFuture<RedissonLockEntry> subscribeFuture, final RPromise<Void> result, final long currentThreadId) {
        RFuture<Long> ttlFuture = tryAcquireAsync(leaseTime, unit, currentThreadId);
        ttlFuture.addListener(new FutureListener<Long>() {
            @Override
            public void operationComplete(Future<Long> future) throws Exception {
                if (!future.isSuccess()) {
                    unsubscribe(subscribeFuture, currentThreadId);
                    result.tryFailure(future.cause());
                    return;
                }

                Long ttl = future.getNow();
                // lock acquired
                if (ttl == null) {
                    unsubscribe(subscribeFuture, currentThreadId);
                    if (!result.trySuccess(null)) {
                        unlockAsync(currentThreadId);
                    }
                    return;
                }

                final RedissonLockEntry entry = getEntry(currentThreadId);
                if (entry.getLatch().tryAcquire()) {
                    lockAsync(leaseTime, unit, subscribeFuture, result, currentThreadId);
                } else {
                    // waiting for message
                    final AtomicReference<Timeout> futureRef = new AtomicReference<Timeout>();
                    final Runnable listener = new Runnable() {
                        @Override
                        public void run() {
                            if (futureRef.get() != null) {
                                futureRef.get().cancel();
                            }
                            lockAsync(leaseTime, unit, subscribeFuture, result, currentThreadId);
                        }
                    };

                    entry.addListener(listener);

                    if (ttl >= 0) {
                        Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                if (entry.removeListener(listener)) {
                                    lockAsync(leaseTime, unit, subscribeFuture, result, currentThreadId);
                                }
                            }
                        }, ttl, TimeUnit.MILLISECONDS);
                        futureRef.set(scheduledFuture);
                    }
                }
            }
        });
    }

    @Override
    public RFuture<Boolean> tryLockAsync() {
        return tryLockAsync(Thread.currentThread().getId());
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long threadId) {
        return tryAcquireOnceAsync(-1, null, threadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, TimeUnit unit) {
        return tryLockAsync(waitTime, -1, unit);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit) {
        long currentThreadId = Thread.currentThread().getId();
        return tryLockAsync(waitTime, leaseTime, unit, currentThreadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(final long waitTime, final long leaseTime, final TimeUnit unit,
            final long currentThreadId) {
        final RPromise<Boolean> result = new RedissonPromise<Boolean>();

        final AtomicLong time = new AtomicLong(unit.toMillis(waitTime));
        final long currentTime = System.currentTimeMillis();
        RFuture<Long> ttlFuture = tryAcquireAsync(leaseTime, unit, currentThreadId);
        ttlFuture.addListener(new FutureListener<Long>() {
            @Override
            public void operationComplete(Future<Long> future) throws Exception {
                if (!future.isSuccess()) {
                    result.tryFailure(future.cause());
                    return;
                }

                Long ttl = future.getNow();

                // lock acquired
                if (ttl == null) {
                    if (!result.trySuccess(true)) {
                        unlockAsync(currentThreadId);
                    }
                    return;
                }

                long elapsed = System.currentTimeMillis() - currentTime;
                time.addAndGet(-elapsed);
                
                if (time.get() <= 0) {
                    trySuccessFalse(currentThreadId, result);
                    return;
                }
                
                final long current = System.currentTimeMillis();
                final AtomicReference<Timeout> futureRef = new AtomicReference<Timeout>();
                final RFuture<RedissonLockEntry> subscribeFuture = subscribe(currentThreadId);
                subscribeFuture.addListener(new FutureListener<RedissonLockEntry>() {
                    @Override
                    public void operationComplete(Future<RedissonLockEntry> future) throws Exception {
                        if (!future.isSuccess()) {
                            result.tryFailure(future.cause());
                            return;
                        }

                        if (futureRef.get() != null) {
                            futureRef.get().cancel();
                        }

                        long elapsed = System.currentTimeMillis() - current;
                        time.addAndGet(-elapsed);
                        
                        tryLockAsync(time, leaseTime, unit, subscribeFuture, result, currentThreadId);
                    }
                });
                if (!subscribeFuture.isDone()) {
                    Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
                        @Override
                        public void run(Timeout timeout) throws Exception {
                            if (!subscribeFuture.isDone()) {
                                subscribeFuture.cancel(false);
                                trySuccessFalse(currentThreadId, result);
                            }
                        }
                    }, time.get(), TimeUnit.MILLISECONDS);
                    futureRef.set(scheduledFuture);
                }
            }

        });


        return result;
    }

    private void trySuccessFalse(final long currentThreadId, final RPromise<Boolean> result) {
        acquireFailedAsync(currentThreadId).addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (future.isSuccess()) {
                    result.trySuccess(false);
                } else {
                    result.tryFailure(future.cause());
                }
            }
        });
    }

    private void tryLockAsync(final AtomicLong time, final long leaseTime, final TimeUnit unit,
            final RFuture<RedissonLockEntry> subscribeFuture, final RPromise<Boolean> result, final long currentThreadId) {
        if (result.isDone()) {
            unsubscribe(subscribeFuture, currentThreadId);
            return;
        }
        
        if (time.get() <= 0) {
            unsubscribe(subscribeFuture, currentThreadId);
            trySuccessFalse(currentThreadId, result);
            return;
        }
        
        final long current = System.currentTimeMillis();
        RFuture<Long> ttlFuture = tryAcquireAsync(leaseTime, unit, currentThreadId);
        ttlFuture.addListener(new FutureListener<Long>() {
            @Override
            public void operationComplete(Future<Long> future) throws Exception {
                if (!future.isSuccess()) {
                    unsubscribe(subscribeFuture, currentThreadId);
                    result.tryFailure(future.cause());
                    return;
                }

                Long ttl = future.getNow();
                // lock acquired
                if (ttl == null) {
                    unsubscribe(subscribeFuture, currentThreadId);
                    if (!result.trySuccess(true)) {
                        unlockAsync(currentThreadId);
                    }
                    return;
                }
                
                long elapsed = System.currentTimeMillis() - current;
                time.addAndGet(-elapsed);
                
                if (time.get() <= 0) {
                    unsubscribe(subscribeFuture, currentThreadId);
                    trySuccessFalse(currentThreadId, result);
                    return;
                }

                // waiting for message
                final long current = System.currentTimeMillis();
                final RedissonLockEntry entry = getEntry(currentThreadId);
                if (entry.getLatch().tryAcquire()) {
                    tryLockAsync(time, leaseTime, unit, subscribeFuture, result, currentThreadId);
                } else {
                    final AtomicBoolean executed = new AtomicBoolean();
                    final AtomicReference<Timeout> futureRef = new AtomicReference<Timeout>();
                    final Runnable listener = new Runnable() {
                        @Override
                        public void run() {
                            executed.set(true);
                            if (futureRef.get() != null) {
                                futureRef.get().cancel();
                            }

                            long elapsed = System.currentTimeMillis() - current;
                            time.addAndGet(-elapsed);
                            
                            tryLockAsync(time, leaseTime, unit, subscribeFuture, result, currentThreadId);
                        }
                    };
                    entry.addListener(listener);

                    long t = time.get();
                    if (ttl >= 0 && ttl < time.get()) {
                        t = ttl;
                    }
                    if (!executed.get()) {
                        Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                if (entry.removeListener(listener)) {
                                    long elapsed = System.currentTimeMillis() - current;
                                    time.addAndGet(-elapsed);
                                    
                                    tryLockAsync(time, leaseTime, unit, subscribeFuture, result, currentThreadId);
                                }
                            }
                        }, t, TimeUnit.MILLISECONDS);
                        futureRef.set(scheduledFuture);
                    }
                }
            }
        });
    }


}
;