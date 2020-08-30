### 什么是 ElasticSearch?

分布式, 高性能, HA, scalable 的搜索和分析系统

**什么是搜索:**

1. 互联网站, 各种app里面的搜索.

2. IT系统的搜索, OA软件, 办公软件, 日程管理, 后台系统之类的搜索. 

**使用数据库搜索:**

1. 会遍历全部记录, 然后抓出来匹配. 

2. 文字匹配规则简单(like %xx%), 搜索不出同义的结果.

**什么是全文检索和Lucene:**

1. **倒排索引:**

   ```java
   1. 把每个记录拆成关键词list
   2. 把关键词拿出来, 组建索引, 索引的item指向包含关键词记录, 组成倒排索引. 搜索关键词就可以找到句子了, 不用去搜句子解析句子了.
   ```

2. **全文检索:**

   搜索关键词, 关键词是之前分析记录的倒排索引, 就是全文检索. 

3. **[Lucene](https://lucene.apache.org/):**

   是组建倒排索引和搜索的依赖框架. apache的

**EalsticSearch是什么:**

如果单台服务器不能管理的太多数据, ES就是一个封装了多台机器Lucene存储的框架. 保证了数据分发和副本.

1. 自动维护数据的分布和多个节点的索引, 还有请求的分发.
2. 维护数据副本
3. 提供了一些高级搜索的功能(聚合分析, 地理位置检索...)



### 03. ElasticSearch 介绍

**功能:**

1. 分布式: 将数据分散到Node上存储和检索. **近实时搜索.**
2. 搜索: 全文检索, 结构化搜索(比如商品分类搜索)
3. 数据分析: (例:分析每个商品分类下有多少相关商品)

**使用场景:**

1. 百科, GitHub, 代码搜索
2. 新闻网站, 论坛, 电商
3. 日志数据分析, ELK, 价格监控, BI商业分析系统. Kibana做数据可视化.

**ES特点:**

1. scalable: 可以大型分布式, 也可以单机
2. 技术整合: 圈粉检索, 数据分析, 分布式.
3. 开箱即用: 
4. 相对数据库, 搜索功能更多. 对于不需要事物(数据库强项)的, ES也挺好.



### 04. ElasticSearch 核心概念

**Lucene和ElasticSearch的前世今生:**

Lucene是现金功能强大的搜索库, 但API复杂开发难. 

ES基于lucene, 提供简单易用的API. 

**ES的核心概念:**

1. near realtime(NRT): 近实时, 有秒级的延迟. 

2. cluster: 集群, 多个节点

3. Node: 集群中一个节点, 随机分配名字. 直接启动一堆节点, 会组成一个elasticsearch集群, 新启动的节点也会去加入elasticsearch的集群.

4. document: 文档, es中最小的数据单元. 可以是一条任何的数据, 通常json格式. 每个index下的type可以存储多个ducoment.

5. index: 索引, 包含一堆相似结构的document, 比如: 有一个客户索引, 商品分类索引, 订单索引...

   索引有一个名字, 然后包含很多document. 

6. Type: 类型, 是索引的逻辑数据分类, 每个索引里可以有一个/n个type. 一个type下的documents, 有相同的field. ES8消失了.

7. shard: ES把一个索引中的数据切分成多个shard. 更容易横向扩展和更多的数据, 可以分发查询.

   有**primaryShard和replicaShard**, 分别做主从副本.  

**和数据库对比:**

document	行

type	表

index	库



### 05. 安装启动ES

![步骤](kernel_knowledge_1.assets/image-20200811225037518.png)



### 06. 商品管理(1)

1. **document数据格式:** 

   面向document的搜索分析引擎. 

   ```java
   1. 应用系统的数据结构都是面向对象的;
   2. 对象结构存储在DB种, 拆分成扁平的多张表, 取出来放在对象里需要涉及多层嵌套. 
   3. ES面向document, document存储的json数据结构, 于面向对象的数据结构是一致的;
   ```

2. **商品管理案例背景:** 

   

3. **集群管理:** 

   ```java
   1. 快速检查集群健康, 通过catAPI.
       `/_cat/health?v`
   2. 状态颜色:
   	green: 每个索引的primaryShard和replicaShard都是active状态.
       yellow: 每个索引的primaryShard是active的, replicaShard不是active的.
       red: 不是所有的primaryShard都是active的. 部分索引有数据丢失. 
   3. 单台计算机的shard是yellow的:
   	一个ES进程是一个node, 默认的kibana内置的index存储, 分配5个primaryShard和5个replicaShard, 对应的shard不能放在同一个机器上. 所以只有一个primaryShard, 没有第二台放replicaShard. 只要启动第二个es进程添加一个node, 那个replicaShard就会存储到里面.
   
   4. 查询集群的索引: `/_cat/indexs?v`
   ```

   

4. **CRUD操作:** 

   **新增:** ES自动建立index和type, 默认对document每个field建里倒排索引.

   ```json
   # PUT /index/type/id
   {
       "name": "aaa",
       "desc": "a",
       "price": 100,
       "producer": "xxxx",
       "tags": ["a", "aa"]
   }    
   ```

   **查询:**

   ```json
   # GET /Index/type/id
   ```

   **修改:**

   ```json
   # PUT /index/type/id 替换, 需要把所有的field都带上.
   # POST /index/type/id/_update 更新文档, 只需要修改的field
   
   ```

   

### 07. 商品管理: 多种搜索方式

1. Query String Search:

   ```txt
   // 搜索全部商品: 
   GET /index/type/_search?field1=name:xxx&sort=yyy:desc
   Response:
   {
   	"took": 耗时,
   	timed_out: 是否超时.
   	_shards: 搜索请求, 会发送到所有的Shard(primary或者replicaShard)
   	hits.total: 命中的documents.
   	hits.max_score: 命中的document分数最高的一个. 越相关, 分数越高.
   	hits.hits: 匹配到的documents的详细数据. 
   }
   ```

   

2. query DSL: (DSL: Domain Specified Language 特定领域的语言)

   ```txt
   把查询的条件构成json, 放在http body里面. 
   1. 查询所有的商品:
   GET /index/type/_search
   {
   	"query":{
   		"match_all":{}
   	}
   }
   2. 查询名称包含xxx的商品按照yyy字段倒序:
   GET /index/type/_search
   {
   	"query":{
   		"match":{
   			"name":"xxx"
   		}
   	},
   	"sort": {
   		{"yyy": "desc"}
   	}
   }
   3. 分页查询
   GET /index/type/_search
   {
   	"query": {"match_all":{}},
   	"from": 1,
   	"size": 1
   }
   4. 指定查询的字段, 只查询xxx和yyy字段
   {
   	"query":{
   		"match_all":{}
   	},
   	"_source":["xxx", "yyy"]
   }
   ```

   

3. query filter: 过滤数据, bool字段封装多个查询条件

   ```txt
   1. 查询, name是xxx的, 价格字段大于25的document
   GET /index/type/_search
   {
   	"query":{
   		"bool":{
   			"must":{
   				"match":{
   					"name":"xxx"
   				}
   			},
   			"filter":{
   				"range": {
   					"price":{"gt", 25}
   				}
   			}
   		}
   	}
   }
   ```

   

4. full-text search: 全文检索.

   ```txt
   1. 全文搜索:
   name字段应该会被拆解成词, 然后建立倒排索引. 
   GET /index/type/_search
   {
   	"query": {
   		"match":{
   			"name": "xxx yyy"
   		}
   	}
   }
   ```

   

5. phrase search:短语搜索

   ```txt
   GET /index/type/_search
   {
   	"query": {
   		"match_phrase":{
   			"name": "xxx yyy" # 查询结果必须包含"xxx yyy"短语.
   		}
   	}
   }
   ```

   

6. highlight search:

   ```txt
   GET /index/type/_search
   {
   	"query": {
   		"match":{
   			"name": "xxx yyy"
   		}
   	},
   	"highlight": {
   		"fields": {
   			"name":{} # 把name字段都加亮
   		}
   	}
   }
   ```

   

### 08. 商品管理: _group by_ +  _avg_ + _sort_ 等聚合分析

```txt
// 1. 计算每个tag下面的商品数量.
GET /index/type/_search
{
	"aggs": {
		# 自己启一个agg的名字
		"group_by_tags":{
			"terms":{
				"field": "tags"
			}
		}
	}
}

// 2. 对搜索出来的商品聚合分析
GET /index/type/_search
{
	"query": {
		"match":{
			"name": "xxx"
		}
	},
	"aggs": {
		# 自己启一个agg的名字
		"group_by_tags":{
			"terms":{
				"field": "tags"
			}
		}
	}
}

// 3. 先分组, 再算每组的平均值, 计算每个tag下的商品平均线价格
GET /index/type/_search
{
	"size": 0,
	"aggs" {
		"group_by_tags": {
			"terms": {"field": "tags"},
			"aggs": {
				# 上面agg里面的agg.叫"avg_price", 使用"avg"方法
    			"avg_price": {
					"avg": {"field":"price"}
				}
			}
		}
	}
}

// 4. 在3基础上, 再加上按照平均价格进行倒序排序.
GET /index/type/_search
{
	"aggs":{
		"group_by_tags":{
			"terms":{
				"feild": "tags",
				"order": {"acg_price":"desc"}
			}, 
			"aggs": {
				# 这个agg叫平均价格, 做avg
				"acg_price": {
					"avg": {"field": "price"}
				}
			}
		}
	}
}

// 5. 按照指定的价格区间进行分组, 然后在组内按照tag进行分组, 最后计算每组的平均价格.
GET /index/type/_search
{
	"aggs"{
		"group_by_price" {
			"range": {
				"field": "price",
				"ranges": [
					{"from":0, "to": 20},
					{"from":20, "to": 40},
					{"from":40, "to": 60}
				]
			},
			"aggs": {
				"group_by_tags": {
					"terms":{"field": "tags"},
					"aggs":{
						"avg_price": {
							"feild":"price"
						}
					}
				}
			}
		}
	}
}
```



### 09. Elasticsearch 的基础分布式架构

1. ES 对复杂分布式机制的透明隐藏特性

   分布式机制: 

   1. 分片机制
   2. cluster discovery
   3. shard负载均衡
   4. shard副本
   5. 请求路由
   6. 集群扩容
   7. shard重分配

2. ES的垂直和水平扩容

   垂直扩容: 每台服务器扩容.

   水平扩容: 加服务器.

3. 节点调整时候的rebalance.

   尽量每次节点调整时候让shard平均分布.

4. master节点:

   管理es集群的元数据:索引创建和删除,...... 不成姐所有请求. 

5. 节点平等的分布式架构:

   1. 每个节点都可以接受请求
   2. 接受请求的节点, 把请求转发到存有对应shard的节点
   3. 接受请求的节点, 负责把最终的操作结果, 返回给客户端.

   

### 10. shard&replica机制树立, 单个node里面如何创建index

**Shard和Replica机制:**

1. Index有多个shard组成

   一个index, 包含3T的数据, 每个服务器可以放1T数据, Index可以分成3个Shard, 分别盛放1T.

2. Shard是最小工作单元:

   承接部分数据, **每个shard就是一个Licene实例, 完整的建立索引和处理请求的能力.**

3. **shard节点自动在Nodes里负载均衡**: 尽量平均分配到nodes里面

4. **PrimaryShard和replicaShard是一对**, 每个document只在index里面的一对shard里面

   replica做副本容错.

5. **PrimaryShard数量在创建index时候就固定(默认5)**, reolicashard的数量可以修改(默认1). 所以默认由10个shard, 其中primary和replica五五分.

6. **primary不能和replica放在同一节点**(否则节点宕机). replica可以和其它的primary放在一起.



**图解单Node环境下Index创建:**

1. 创建index, 包含三个PrimaryShard和3个ReplicaShard:

   ```json
   PUT /index_name
   {
       "settings": {
           "number_of_shards": 3,
           "number_of_replicas": 1
       }
   }
   ```

2. Node不足: 集群状态: yellow. 集群正常工作, 但是无法创建replica, 没有副本容错保证.



### 11.**图解2个Node环境下的ReplicaShard分配:**

1. 多了一个node之后, 副本就可以建立在新的node里面了, 没有一个node里分配primaryShard个数的限制
2. primaryShard里面的数据就被同步到replicaShard
3. **Primary/Replica 都可以处理读请求**



### 12. 横向扩容, 如何提升容错

1. 尽量保持shard的平均分配, 但也不能primary和replica放在同一个node里.

   shard占据的系统资源更多, 性能更好.

2. 扩容的极限: 就是shard数量(primary+replica)

3. 超出了扩容的极限, 就可以增加replica数量, 应对读请求. 



提升容错性: 提高replica, 每个primary+n个replcia, down到最后剩下一个就行.



### 13. ES 容错机制: master选举, replica容错, 数据恢复

有masterNode被干掉集群状态就变成red, 需要容错, 进行master选举. 

**容错:** 

1. Master选举: 自动选举出另一个node做为新的masterNode. 承担起masterNode的协调功能
2. 新的masterNode: 将down掉的primaryShard换成它的replicaShard. 集群变成yellow(因为缺失了replica)
3. 重启故障Node: 把死掉的master起来做一个node, 将缺失的replicaShard放在这个node上. 使用之前已有的shard数据, 然后和其他的shard同步一下宕机之内的修改.



### 14. Document 主要元数据

每一个document里面都会有三个字段的元数据: index, type, id.

1. **_index 元数据:**

   1. 表示document存放的index.
   2. 每个index包含类似的document, 比如商品类的index, 鞋类的index
   3. index的名字必须小写, 不能下划线开头

2. **_type元数据**

   代表document属于index的type, 同type的field相同. 

   type名字可以大小写, 不能下划线开头

3. **_id元数据**

   document在index的type里的唯一标识. (index, type, id)是document的坐标.

   可手动指定, 否则自动生成.

   **手动指定:**

   比如从DB中的库表数据导出到ES里面, 比较合适手动指定ES里的id为数据库里的.

   **自动生成:**

   如果是log之类的, id不重要, 也不知道之前的ID是什么.

   长度20字符, URL安全的, base64编码, GUID生成(分布式不冲突)

   ```json
   POST /index/type
   {
       "field1": "value1"
   }
   response{
       "_index":"index",
       "_type": "type",
       "_id": "cdasjflekwlkjqr",
       "_version": 1,
       "result": "created",
       "_shards": {
           "total":2,
           "successful": 1,
           "failed": 0
       },
       "created": true
   }
   ```

4. **_source元数据:**

   **document的所有field.** 也可以指定返回field返回,

   ```json
   GET /index/type/id?_source=field2,field3
   {
       "_index":"",
       "_type":"",
       "_id":"",
       "_version":"",
       "found":true,
       "_source":{
           "field2": "value2",
           "field3": "value3"
       }
   }
   ```

   

### 17. document的全量替换, 强制创建及删除操作

1. **全量替换:** 和创建的语法一样, **指定index/type/id 进行PUT**

   老的document被**标记成deleted, 创建一个新的**. deleted数据只有在ES空间不足的时候进行清除. 

2. **document强制创建:** 

   创建document, 默认如果之前有了就全量替换, 但version会增加. 可以强制创建一个.

   `PUT /index/type/id?op_type=create`或者`PUT /index/type/id/_create` 

   如果是“全量替换”操作，那么就失败。

3. **删除**

   `Delete /index/type/id` 默认标记成delete状态, 只有在内存不足时侯后台物理删掉



### 18. ES并发冲突问题

并发修改时候, 拿着老数据去CAS, 肯定失败了. 不cas又会造成数据不准确.

**乐观锁悲观锁**

悲观锁并发低, 乐观锁需要自己逻辑处理, 可以带版本号避免ABA

**ES内部基于_version进行乐观锁并发控制**

document里面的_version元数据, 用于CAS.

ES后台多线程, 可能version版本大的修改先到, 版本小的反倒后到, 这个时候会修改大版本的, 不会让小版本的修改覆盖大版本的.?? 确实版本不一样覆盖不了, 但是不会有比自己版本号还大的.~~



### 21. 使用_version进行乐观锁修改

先创建一个document

```json
PUT /index/type/id
{
    "field1": "value1"
}
```

更新时候带上版本号

```json
PUT /index/type/id?version=1
{
	"field1": "value1_----"
}
# 修改成功
```

**此时, 如果再用version=1去修改, 就改不成功了.**

需要重新拿到版本号, 然后去修改.



### 22. 基于_externalVersion_进行乐观锁并发控制

1. esternal version是什么?

   可以不用_version, 用自己维护的external version进行并发控制.

2. external version 修改规则:

   _version必须是相同才能修改(因为递增, ES里只有最大的)

   **external version需要version比es里面的_version大才可以成功.** 因为ES要用传进来的version做document的新version.

   ```json
   PUT /index1/_doc/external_version_update?version=1&version_type=external
   {
     "field1": "value2--------"
   }
   # response:
   {
           "type" : "version_conflict_engine_exception",
           "reason" : "[external_version_update]: version conflict, current version [4] is higher or equal to the one provided [4]",
    }
   ```

   

### 23. partial update原理

1. **what is partial update:**

   之前是`PUT /index/type/id`, 创建和修改document. **是全量数据的新增和替换**

   partial update(局部更新) 使用的是 `POST /index/type/id/_update` post的`doc`字段放所想要更新的fields

   ```json
   POST /index/type/id/_update
   {
       "doc":{
           "fieldx": "valuexxx"
       }
   }
   ```

2. **内部原理**

   和之前先要查一遍, 修改字段后全量更新一样, 都是会把原来的document标记为delete, 然后新建一个document, version增加.

   优点就是修改的三个步骤"查询,修改,写回"都发生在shard内部, 减少了冲突, 更方便了

3. 基于Groovy脚本实现_partial update_

   `POST /index/type/id/_update`

   ```json
   {
       "script": {
           "lang": "groovy",
           "file": "test-file", # script名字, 放在es的conf/script目录下.
           "params": {
               
           }
       }
   }
   ```



### 25. Partial update的乐观锁并发控制原理

partial update内部会自动执行version为版本号的乐观锁并发控制. 也可以自己加一个version过去.

**Retry策略:** 可以在update后面添加参数`?retry_on_conflict=5`, 这样还会5次retry.



### 26. mget 批量查询API

批量查询:

```json
# GET /_mget
{
  "docs":[
    {
      "_index":"index1",
      "_type":"type1",
      "_id": 1
    }, 
    {
      "_index":"index1",
      "_type":"type1",
      "_id":2
    }
    ]
}
# response:
{
  "docs" : [
    {
      "_index" : "index1",
      "_type" : "type1",
      "_id" : "1",
      "_version" : 2,
      "_seq_no" : 1,
      "_primary_term" : 1,
      "found" : true,
      "_source" : {
        "field2" : "value2"
      }
    },
    {
      "_index" : "index1",
      "_type" : "type1",
      "_id" : "2",
      "found" : false
    }
  ]
}
# 如果查询index下面的还可以
# GET /index1/_mget
{
    "docs":[
        { "_type":"type1","_id":1 },
        { "_type":"type1","_id":2 }
            ]
}
# type也一样.
```

**mget重要性:** 多条数据一定要用batchAPI, 减少网络开销.

查询所有的, [上面讲的](#多种搜索方式)

### 27. bulk 批量增删改

1. bulk对json语法要求: 每个操作的JSON不能换行, 只能一行.

   ```txt
   1. delete删除操作
   2. create创建操作
   3. index创建/全量替换操作
   4. update partialUpdate操作
   每个操作除了delete, 要两个json:
   1. {"{{action}}" : {k-v}}
   2. {param-value}
   ```

   

2. 任意一个操作的失败不会影响其他操作的执行.

3. 也可以像上面mget里面, 在URL里面指定操作的index和type.

4. bulk size: bulk request会加载到内存里, 太大性能会降低, 要多尝试.



### 28. 阶段总结 和 _distributed document store_

1~8: 快速入门和安装

9~13: ES分布式基本原理

14~27: document的操作.

**什么是_distributed document store_**

上面两个讲解时结合扫了ES在运行时候的最核心功能: 分布式的文档数据存储系统.

ES可以存储和操作JSON文档类型数据, 起到了NoSQL的存储系统. 



**适用应用(也算是NoSQL数据库):**

1. 数据量大, ES可以扩容, 承载大量数据.
2. 数据格式多变, 数据结构复杂.
3. 数据操作简单, CRUD.

适用于数据量大, 功能少的系统.



