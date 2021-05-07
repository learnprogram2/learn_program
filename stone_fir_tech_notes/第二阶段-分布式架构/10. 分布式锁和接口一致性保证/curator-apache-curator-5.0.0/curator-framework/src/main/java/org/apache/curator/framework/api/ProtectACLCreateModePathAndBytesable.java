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
package org.apache.curator.framework.api;

public interface ProtectACLCreateModePathAndBytesable<T> extends
    ACLBackgroundPathAndBytesable<T>,
    CreateModable<ACLBackgroundPathAndBytesable<T>>
{
    /**
     * <p>
     *     Hat-tip to https://github.com/sbridges for pointing this out
     * </p>
     *
     * <p>
     *     It turns out there is an edge case that exists when creating sequential-ephemeral
     *     nodes. The creation can succeed on the server, but the server can crash before
     *     the created node name is returned to the client. However, the ZK session is still
     *     valid so the ephemeral node is not deleted. Thus, there is no way for the client to
     *     determine what node was created for them.
     *
     *     有一种极端情况: zkserver收到client的创建node的请求, 创建好了, 返回的时候server炸了, client不明白有没有创建成功.
     * </p>
     *
     * <p>
     *     Even without sequential-ephemeral, however, the create can succeed on the sever
     *     but the client (for various reasons) will not know it.
     * </p>
     *
     * <p>
     *     Putting the create builder into protection mode works around this.
     *     The name of the node that is created is prefixed with a GUID. If node creation fails
     *     the normal retry mechanism will occur. On the retry, the parent path is first searched
     *     for a node that has the GUID in it. If that node is found, it is assumed to be the lost
     *     node that was successfully created on the first try and is returned to the caller.
     *
     *     大概的意思是: 如果节点创建失败, 会使用重试机制去检查有没有节点, 如果创建成功了就成功, 没有就失败. (节点会有一个GUID的前缀)
     * </p>
     *
     * @return this
     */
    public ACLCreateModeBackgroundPathAndBytesable<String>    withProtection();
}
