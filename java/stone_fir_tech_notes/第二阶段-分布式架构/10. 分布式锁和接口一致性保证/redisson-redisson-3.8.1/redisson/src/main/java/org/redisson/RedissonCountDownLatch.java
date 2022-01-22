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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RFuture;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.pubsub.CountDownLatchPubSub;

/**
 * Distributed alternative to the {@link java.util.concurrent.CountDownLatch}
 *
 * It has a advantage over {@link java.util.concurrent.CountDownLatch} --
 * count can be reset via {@link #trySetCount}.
 *
 * @author Nikita Koksharov

------------ 设置总门槛
key: [keyName, channelName(redisson_countdownlatch__channel__{keyName})]
args: [newCountMessage(1), count(总门槛)]
// 检查有没有keyName
if redis.call('exists', KEYS[1]) == 0 then
// 先设置keyName的锁对象, 就是一个int的总门槛, 发布一条消息1
redis.call('set', KEYS[1], ARGV[2]);
redis.call('publish', KEYS[2], ARGV[1]);
return 1
else
// 之前设置过就返回0false.
return 0
end;

------------ 满足一个门槛
key:[keyName, channelName(redisson_countdownlatch__channel__{keyName})]
args:[zeroCountMessage(0)]
// 1. 把总门槛-1.
local v = redis.call('decr', KEYS[1]);
// 2. 门槛变成0了就干掉keyName
if v <= 0 then redis.call('del', KEYS[1]) end;
// 3. 如果门槛都满足了, 就发布一条0的消息
if v == 0 then redis.call('publish', KEYS[2], ARGV[1]) end;

 */
public class RedissonCountDownLatch extends RedissonObject implements RCountDownLatch {

    public static final Long zeroCountMessage = 0L;
    public static final Long newCountMessage = 1L;

    private static final CountDownLatchPubSub PUBSUB = new CountDownLatchPubSub();

    private final UUID id;

    protected RedissonCountDownLatch(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.id = commandExecutor.getConnectionManager().getId();
    }

    // TODO: H.3: 等待门槛满足: 监听channel里的消息, 被唤醒后拿门槛, 如果=0了就返回.
    public void await() throws InterruptedException {
        RFuture<RedissonCountDownLatchEntry> future = subscribe();
        try {
            commandExecutor.syncSubscription(future);

            while (getCount() > 0) {
                // waiting for open state
                RedissonCountDownLatchEntry entry = getEntry();
                if (entry != null) {
                    entry.getLatch().await();
                }
            }
        } finally {
            unsubscribe(future);
        }
    }

    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        long remainTime = unit.toMillis(time);
        long current = System.currentTimeMillis();
        RFuture<RedissonCountDownLatchEntry> promise = subscribe();
        if (!await(promise, time, unit)) {
            return false;
        }

        try {
            remainTime -= (System.currentTimeMillis() - current);
            if (remainTime <= 0) {
                return false;
            }

            while (getCount() > 0) {
                if (remainTime <= 0) {
                    return false;
                }
                current = System.currentTimeMillis();
                // waiting for open state
                RedissonCountDownLatchEntry entry = getEntry();
                if (entry != null) {
                    entry.getLatch().await(remainTime, TimeUnit.MILLISECONDS);
                }

                remainTime -= (System.currentTimeMillis() - current);
            }

            return true;
        } finally {
            unsubscribe(promise);
        }
    }

    private RedissonCountDownLatchEntry getEntry() {
        return PUBSUB.getEntry(getEntryName());
    }

    private RFuture<RedissonCountDownLatchEntry> subscribe() {
        return PUBSUB.subscribe(getEntryName(), getChannelName(), commandExecutor.getConnectionManager().getSubscribeService());
    }

    private void unsubscribe(RFuture<RedissonCountDownLatchEntry> future) {
        PUBSUB.unsubscribe(future.getNow(), getEntryName(), getChannelName(), commandExecutor.getConnectionManager().getSubscribeService());
    }

    // TODO: H.2: 满足一个门槛
    @Override
    public void countDown() {
        get(countDownAsync());
    }

    @Override
    public RFuture<Void> countDownAsync() {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                        "local v = redis.call('decr', KEYS[1]);" +
                        "if v <= 0 then redis.call('del', KEYS[1]) end;" +
                        "if v == 0 then redis.call('publish', KEYS[2], ARGV[1]) end;",
                    Arrays.<Object>asList(getName(), getChannelName()), zeroCountMessage);
    }

    private String getEntryName() {
        return id + getName();
    }

    private String getChannelName() {
        return "redisson_countdownlatch__channel__{" + getName() + "}";
    }

    @Override
    public long getCount() {
        return get(getCountAsync());
    }

    @Override
    public RFuture<Long> getCountAsync() {
        return commandExecutor.writeAsync(getName(), LongCodec.INSTANCE, RedisCommands.GET_LONG, getName());
    }

    // TODO: H.1: 这里是设置总门槛
    @Override
    public boolean trySetCount(long count) {
        return get(trySetCountAsync(count));
    }

    @Override
    public RFuture<Boolean> trySetCountAsync(long count) {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('exists', KEYS[1]) == 0 then "
                    + "redis.call('set', KEYS[1], ARGV[2]); "
                    + "redis.call('publish', KEYS[2], ARGV[1]); "
                    + "return 1 "
                + "else "
                    + "return 0 "
                + "end",
                Arrays.<Object>asList(getName(), getChannelName()), newCountMessage, count);
    }

    @Override
    public RFuture<Boolean> deleteAsync() {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('del', KEYS[1]) == 1 then "
                    + "redis.call('publish', KEYS[2], ARGV[1]); "
                    + "return 1 "
                + "else "
                    + "return 0 "
                + "end",
                Arrays.<Object>asList(getName(), getChannelName()), newCountMessage);
    }

}
