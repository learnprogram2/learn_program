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

import java.util.concurrent.locks.Lock;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.command.CommandAsyncExecutor;

/**
 * readWriteLock维护一对关联的不公平lock
 * 1. readLock: 可以被多个read操作拿着.
 * 2. writeLock: 只能被一个拿着.
 *
 * A {@code ReadWriteLock} maintains a pair of associated {@link
 * Lock locks}, one for read-only operations and one for writing.
 * The {@link #readLock read lock} may be held simultaneously by
 * multiple reader threads, so long as there are no writers.  The
 * {@link #writeLock write lock} is exclusive.
 *
 * Works in non-fair mode. Therefore order of read and write
 * locking is unspecified.
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReadWriteLock extends RedissonExpirable implements RReadWriteLock {

    public RedissonReadWriteLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
    }

    @Override
    public RLock readLock() {
        return new RedissonReadLock(commandExecutor, getName());
    }

    @Override
    public RLock writeLock() {
        return new RedissonWriteLock(commandExecutor, getName());
    }


    // 下面是read-write lock的lua脚本:
    /*
========== write锁  √
keys: [keyName]
values: [leaseTime, lockName(UUID:{threadId}) / (lockObjId:{threadId}) / (connectionMgrId:{threadId})]

// 先拿到keyName对应的Hash对象里的mode字段
local mode = redis.call('hget', KEYS[1], 'mode');
if (mode == false) then
    // 如果没有锁/锁mode, 就设置上锁对象, 加上过期时间(30s), 类似可重入锁 {<lockName:1>, <mode:write>}, 多了一个mode
    redis.call('hset', KEYS[1], 'mode', 'write');
    redis.call('hset', KEYS[1], ARGV[2], 1);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end;

// 如果自己已经拿到了write锁
if (mode == 'write') then
    // 如果这个锁是自己的, 就锁自增, 并且续约30S.
    if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then
        redis.call('hincrby', KEYS[1], ARGV[2], 1);
        local currentExpire = redis.call('pttl', KEYS[1]);
        redis.call('pexpire', KEYS[1], currentExpire + ARGV[1]);
        return nil;
    end;
end;
// 别人占了锁对象(读锁或者是写锁), 返回锁的有效期.
    return redis.call('pttl', KEYS[1]);


----------拿读锁  √
key: [keyName, readWriteTimeoutNamePrefix({keyName}:{lockName}:rwlock_timeout)]
value: [leaseTime, lockName, writeLockName(lockName:write)]

// 拿到当前锁的mode
local mode = redis.call('hget', KEYS[1], 'mode');
if (mode == false) then
    // 如果没有锁, 就设置可重入锁, 添加一个<mode:read>的键.
    redis.call('hset', KEYS[1], 'mode', 'read');
    redis.call('hset', KEYS[1], ARGV[2], 1);
    // 设置<readWriteTimeoutNamePrefix({keyName}:lockName:rwlock_timeout):1, 1>,
    redis.call('set', KEYS[2] .. ':1', 1);
    // 设置readLock和读锁的过期时间
    redis.call('pexpire', KEYS[2] .. ':1', ARGV[1]);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil; // 这里会开启看门狗.
end;
// = 读锁之间并行
// 如果有读锁, 或者 (写锁对象里是自己的writeLockName)
if (mode == 'read') or (mode == 'write' and redis.call('hexists', KEYS[1], ARGV[3]) == 1) then
    // 给自己的锁对象里的lockName重入次数+1
    local ind = redis.call('hincrby', KEYS[1], ARGV[2], 1);
    // 设置一个读锁对象: <readWriteTimeoutNamePrefix({keyName}:lockName:rwlock_timeout):重入次数, 1>
    local key = KEYS[2] .. ':' .. ind;
    redis.call('set', key, 1);
    // 把读锁对象和锁对象都续约.
    redis.call('pexpire', key, ARGV[1]);
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return nil;
end;
// 如果不是读锁, 也不是自己的写锁, 就返回当前锁对象的ttl.
return redis.call('pttl', KEYS[1]);


-------续约读锁  √
key:[keyName, {keyName}]
args:[leaseTime, lockName(uuid:threadId)]

// 拿到当前lock的重入次数
local counter = redis.call('hget', KEYS[1], ARGV[2]);
// 如果有重入次数/有读锁
if (counter ~= false) then
    // 给锁对象续期
    redis.call('pexpire', KEYS[1], ARGV[1]);
    // 如果锁对象里的读锁持有人
    if (redis.call('hlen', KEYS[1]) > 1) then
        // 找到keyName的hash里面所有key
        local keys = redis.call('hkeys', KEYS[1]);
        for n, key in ipairs(keys) do
            // 拿到键值对的value
            counter = tonumber(redis.call('hget', KEYS[1], key));
            if type(counter) == 'number' then
                // 如果value是数字的话: 也就是lockName的key. 运行counter次, 就是把这个lockName的重入次数的读锁都续约一下.
                for i=counter, 1, -1 do
                    redis.call('pexpire', KEYS[2] .. ':' .. key .. ':rwlock_timeout:' .. i, ARGV[1]);
                end;
            end;
        end;
    end;
    return 1;
end;
// 续约失败返回0
return 0;


-------------解读锁: !!!如果java里拿到读写锁对象, 用写锁加一个锁, 用读锁可以解掉.
keys: [keyName, channelName(redisson_rwlock:{keyName}), readWriteTimeoutNamePrefix({keyName}:{lockName}:rwlock_timeout), {keyName}]
args: [unlockMessage. lockName(lockId:threadId)]
// 拿到当前lock对象里的mode字段
local mode = redis.call('hget', KEYS[1], 'mode');
if (mode == false) then
    // 没有mode就是没有读写锁, 直接再channel里发条消息, 返回1
    redis.call('publish', KEYS[2], ARGV[1]);
    return 1;
end;
// 检查当前lockName没有在锁对象里面
local lockExists = redis.call('hexists', KEYS[1], ARGV[2]);
if (lockExists == 0) then
    // 如果没有就说明当前线程没有任何锁, 返回nil
    return nil;
end;

// 把锁对象里的自己的lockName可重入次数-1.
local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1);
if (counter == 0) then
    // 如果可重入次数为0了, 就把锁对象里的当前lockName键值对干掉.
    // 如果是写锁加的读锁, 这个怎么办? 锁对象的lockName被干掉了? 不会的, 那样可重入次数就不会是0
    redis.call('hdel', KEYS[1], ARGV[2]);
end;
// 删除了读锁, 就把读锁对象干掉.
redis.call('del', KEYS[3] .. ':' .. (counter+1));

// 如果还有别的人占有读锁/写锁, 单纯续约一下
if (redis.call('hlen', KEYS[1]) > 1) then
    local maxRemainTime = -3;
    local keys = redis.call('hkeys', KEYS[1]);
    // 拿到别的读锁的可重入次数, 统计一下所有其他读锁的所有读锁对象的最大ttl
    for n, key in ipairs(keys) do
        counter = tonumber(redis.call('hget', KEYS[1], key));
        if type(counter) == 'number' then
            for i=counter, 1, -1 do
                local remainTime = redis.call('pttl', KEYS[4] .. ':' .. key .. ':rwlock_timeout:' .. i);
                maxRemainTime = math.max(remainTime, maxRemainTime);
            end;
        end;
    end;
    // 使用最长ttl给锁对象续约, 然后返回0
    if maxRemainTime > 0 then
        redis.call('pexpire', KEYS[1], maxRemainTime);
        return 0;
    end;
    // 如果是自己的写锁, 就返回0: 这里可以走进来么??? 别人有写锁, 自己还有读锁?
    if mode == 'write' then
        return 0;
    end;
end;

// 如果没有别的人拿锁, 把锁对象干掉, 发布一条消息, 返回1.
redis.call('del', KEYS[1]);
redis.call('publish', KEYS[2], ARGV[1]);
return 1;





-------解写锁
keys: [keyName, channelName(redisson_rwlock:{keyName})]
args: [message, leaseTime, lockName]
local mode = redis.call('hget', KEYS[1], 'mode');
// 如果没有写锁, 就发布一条消息, 然后返回1
if (mode == false) then
    redis.call('publish', KEYS[2], ARGV[1]);
    return 1;
end;
// 有一个写锁
if (mode == 'write') then
    // 如果锁对象里不是自己拿的, 就返回nil
    local lockExists = redis.call('hexists', KEYS[1], ARGV[3]);
    if (lockExists == 0) then
        return nil;
    else
        // 自己的写锁对象: 锁可重入次数-1
        local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1);
        // 如果还有, 就续约, 返回 0
        if (counter > 0) then
            redis.call('pexpire', KEYS[1], ARGV[2]);
            return 0;
        else
            // 干掉锁对象里的自己的lockName
            redis.call('hdel', KEYS[1], ARGV[3]);
            // 看看有没有别的锁持有, 就直接干掉, 然后publish一条消息
            if (redis.call('hlen', KEYS[1]) == 1) then
                redis.call('del', KEYS[1]);
                redis.call('publish', KEYS[2], ARGV[1]);
            else
                // 还有自己的read Lock: 把模式改成read 这个不可能走到啊, 因为在里面不区分自己的读写锁.
                redis.call('hset', KEYS[1], 'mode', 'read');
            end;
            return 1;
        end;
    end;
end;
return nil;

     */
}
