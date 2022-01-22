/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.curator.framework.recipes.locks;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class StandardLockInternalsDriver implements LockInternalsDriver
{
    static private final Logger log = LoggerFactory.getLogger(StandardLockInternalsDriver.class);

    // 检查当前node的index有没有在可重入次数之内, 如果没有说明没拿到(监听前一个锁地址), 如果在说明拿到了.
    @Override
    public PredicateResults getsTheLock(CuratorFramework client, List<String> children, String sequenceNodeName, int maxLeases) throws Exception
    {
        // 找到当前的lockName在所有的顺序lock里面的index.
        int             ourIndex = children.indexOf(sequenceNodeName);
        validateOurIndex(sequenceNodeName, ourIndex); // 如果没有找到就报错.

        // 0 < 1. 1 < 2. 2 < 3. 如果 位置的index < 可重入次数, 就说明拿到了锁.
        boolean         getsTheLock = ourIndex < maxLeases;
        // 拿到了就null, 没有监听path. 没有拿到, 就监听上一个node
        String          pathToWatch = getsTheLock ? null : children.get(ourIndex - maxLeases);

        // 小小简单包装.
        return new PredicateResults(pathToWatch, getsTheLock);
    }

    // 这里几乎一定能加成功, 因为临时顺序节点嘛, 往下顺延就好了
    @Override
    public String createsTheLock(CuratorFramework client, String path, byte[] lockNodeBytes) throws Exception
    {
        String ourPath;
        if ( lockNodeBytes != null )
        {
            ourPath = client.create().creatingParentContainersIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, lockNodeBytes);
        }
        else // lockNodeBytes==null, lock的node里面没有数据.
        {
            ourPath = client.create().creatingParentContainersIfNeeded()
                    .withProtection() // 使用重试机制保证创建了就是创建了, 没创建就是没创建.
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL) // 临时顺序节点, 创建path: "/xxxx/xxxx/locks/lock_01/lock-"
                    .forPath(path);     // "bathPath/_c_5e588727-5df4-42df-bc27-4f6f19f420be-lock-0000000002"
                                        // "bathPath/_c_6e127065-c3fb-47a2-b363-226d885cf9b3-lock-0000000003"
                                        // "bathPath/_c_99ae0401-406c-4c66-8212-82ea4d0f52a0-lock-0000000004"
                                        // "bathPath/{{GUID}}lock-{{sequence}}"
        }
        return ourPath;
    }


    @Override
    public String fixForSorting(String str, String lockName)
    {
        return standardFixForSorting(str, lockName);
    }

    public static String standardFixForSorting(String str, String lockName)
    {
        int index = str.lastIndexOf(lockName);
        if ( index >= 0 )
        {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

    static void validateOurIndex(String sequenceNodeName, int ourIndex) throws KeeperException
    {
        if ( ourIndex < 0 )
        {
            throw new KeeperException.NoNodeException("Sequential path not found: " + sequenceNodeName);
        }
    }
}
