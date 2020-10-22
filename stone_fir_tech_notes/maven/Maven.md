1. maven介绍
2. maven依赖管理
3. maven生命周期
4. plugin执行

### 2. 无Maven时代

工作流程:

1. 写功能模块代码, 写单元测试, 弄好单元测试覆盖率报告.

   手动 单元测试, 测试环境, 集成测试

2. 第三方jar包要拷贝到工程的lib目录下.

3. 每次开发后, 要自己编译打包, 测试部署.

   部署: 要把tomcat停掉, 放入war包...

4. 缺点: 

   - 需要管理众多依赖包:

     对于ERP, OA, 银行, 电信系统... 很难.

### 3. Maven: 让依赖和构建自动化

1. maven命令进行单元测试自动运行

2. maven集成多个工程
3. 管理依赖, 自动下载.
4. 自动编译, 打包, 发布.

- maven前身: 
  - make: 最原始的构建工具, 不跨平台
  - ant: maven之前, 手动配置语法复杂, 依赖管理借助ivy.
  - maven: 自动化
  - gradle: google发布, 基于DSL语言, 构建管理, 语法功能强大. Android主要用.



### 4. Win上安装maven

1. 安装jdk
2. 安装maven包, 配置m2_home
3. 环境变量: maven_opts: 设置maven启动的jvm参数

4. 配置`%m2_home%/setting.xml`配置文件
5. `maven help:system `命令会自动构建一些基本的配置



### 5. maven快速创建工程

1. `mvn archetype:generate`命令可以创建project, 甚至可以用骨架.

2. maven目录约定: src/main/java是源码, 测试代码文件夹, pom核心配置文件.

3. pom.xml配置文件:

   <url>: 项目文档下载的url: 用于生成文档.

4. maven打包: 自动下载依赖, 自动单元测试



### 6. maven 体系结构

1. maven解析settings.xml配置文件
2. 解析pom.xml文件
3. 根据pom文件查找依赖: 本地仓库, 远程仓库.
4. 在maven/lib里面会有一个maven-build-.jar包的pom.xml里面配置了最基础的maven仓库.

5. 执行构建: 清理上次构建, 重新编译, 打包.

TODO: maven_体系结构图.



### 7. m2eclipse插件应用eclipse

### 8. eclipse 使用maven开发项目



### 9. 案例: 办公自动化OA系统





### 10. maven依赖管理: 坐标机制

1. 坐标: groupId, artifact, version, packaging, classifer
   - groupId: 公司域名倒叙+项目名
   - artifactId: 项目里的某个模块名, 微服务的服务名
   - classifier: 工程的附属项目: 比方说源码: xxx-source.jar





### 11. 开发: 组织机构模块, 规范的企业坐标

1. 设置好企业坐标

2. 配置文件放resources文件下:

   spring的xml, 引入其它的jdbc之类的配置文件.

3. 编写代码
4. 编写单元测试



### 12. maven的依赖管理机制



1. 所结合的框架之列的, 可以在官网查找依赖.

2. scope: 依赖范围: maven有三套classpath: 编译代码, 单元测试, 项目运行. 

   compile: 对三个class path都有用. test: 只在测试classpath. provided: 编译和测试时候有效. 不会打到包里面. 比如servlet依赖. runtime: 测试和运行.不会编译:打包. 

3. 依赖传递性: 

   依赖父子链条.

4. 可选依赖: <optional>true</optional>

   不会向上传递了, 不继承依赖的依赖了.

5. 依赖调解:

   如果项目有同一个版本的两个不同版本: 就近原则, 然后是第一声明原则.



### 13. 依赖冲突问题和解决

1. 依赖冲突的产生:

   项目里面多个依赖, 一调解之后, 版本给变了.  就会出现实现不同的代码错用.

2. 解决: 用最新的版本......

3. `mvn dependency:tree` 命令分析依赖路径树

   排除掉老版本的.



### 14. maven多重仓库

1. 仓库概念: 

   maven用仓库存储所有依赖

2. 仓库布局:

   - 本地仓库: 在m2文件下的repository文件夹
   - 远程仓库: 私服, 其他组织仓库, 镜像仓库, 中央仓库.

3. 私服: 局域网

   私服是本地仓库和中央仓库的中介. 不允许本地仓库直接去中央仓库.

4. 其他公司/组织的仓库: google, jboss, ...
5. 镜像仓库: 中央仓库的国内/区域代理



### 15. nexus私服仓库安装



### 16. nexus 私服 操作

1. **nexus的仓库类型:**
   1. group: 仓库组: 把各种宿主仓库/代理仓库 组成虚拟仓库组.
   2. hosted: 宿主仓库: 公司内部发布包放在仓库里
   3. proxy: 代理仓库: 代理了其他公司/组织的仓库, 镜像/中央仓库.

2. 仓库格式/布局: maven2.
3. 仓库路径

4. **nexus仓库怎么使用:**

   TODO: 依赖使用流程图



### 17. 实战: 基于nexus搭建一套仓库架构



创建一个总的仓库组

创建release和snapshot宿主仓库, 盛放公司开发的

创建proxy代理仓库, 代理中央仓库.



### 18. 实战: 结合镜像机制强制公司项目配置从nexus私服下载

1. 在settings.xml里面添加profile, 添加私服仓库: 有包的仓库和插件仓库.

2. 使用settings.xml里面的mirror标签, 可以把所有仓库的请求镜像到私服.





### 19. 实战: nexus的权限管理机制

**nexus权限是典型RBAC权限控制. 创建角色和权限, 就可以有读写发布包的权限.**



### 20. 实战: 私服中发布包

1. 创建权限账号

2. 在项目的pom中定义`distributionManagement`标签

   ```xml
   <distributionManagement>
       //  1. 正式版本发布的仓库
   	<repository>
   		<id> nexus-releases</id>
   		<name> Nexus Release Repository</name><url>http://localhost:8081/nexus/content/repositories/releases/</url>
   	</repository>
       // 2. 非正式版本
   	<snapshotRepository>
   		<id> nexus-snapshots</id>
   		<name> Nexus Snapshot Repository</name>
   		<url>http://localhost:8081/nexus/content/repositories/snapshots/</url>
   	</snapshotRepository>
   </distributionManagement>
   
   ```

3. settings.xml中配置两个repository的server标签, 里面包装着仓库名和对应的用户名和密码.

   如果没有, maven就会用匿名帐号去部署.

4. mvn deploy命令

5. 也可以用mvn deploy命令加参数手动把一个jar上传到仓库里.



### 08. 实战: nexus配置自动化管理任务

可以添加一些定时task.





### 22. maven的生命周期以及plugin执行原理

maven生命周期: 就是去解释mvn各种命令背后的原理, 是对传统软件项目构建工作的抽象.

1. 三个独立的生命周期:
2. 每个生命周期执行, 多个phase依次执行
3. 每个phase可以绑定到plugin上, plugin可以有多个goal, 也是依次执行.
4. 默认maven把一些plugin 的goal绑定到phase

![04_maven生命周期原理](Maven.assets/04_maven%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F%E5%8E%9F%E7%90%86.png)

**mvn命令和生命周期关系:**

1. mvn + phase, 可能多个phase: mvn clean package: 运行clean和package两个phase:

2. clean是clean生命周期中的clean phase, 会先执行clean phase之前的所有phase

   package是default生命周期中的package phase, 会执行default生命周期的package phase之前的所有phase.

3. 执行所有的phase, 只有phase绑定了plugin的goal, 这个phase才会干事情.

   plugin.goal是真的干事情的.

4. 例子: clean:clean，clean plugin的clean goal

1. mvn plugin:goal 例如:  dependency:tree

   意思是: 直接执行指定的插件的一个goal, 不执行其他的东西.



### 23. plugin的配置

maven扩展功能时候, 就配置一个插件, 把插件绑定到phase里, 在maven执行命令时候, 就会按照顺序执行phase, 绑定的plugin的goal

1. 每个插件有多个goal, 每个goal是单个的功能实现.
2. plugin标签下, resource标签是resource:resource这个goal的资源配置.

3. 把plugin绑定到phase:

   ```xml
   <build>
   	<plugins>
   		<plugin>
   			<groupId>org.apache.maven.plugins</groupId>
   			<artifactId>maven-source-plugin</artifactId>
   			<version>2.1.1</version>
   			<executions>
   				<execution>
   					<id>attach-sources</id>
   					<phase>verify</phase>
   					<goals>
   						<goal>jar-no-fork</goal>
   					</goals>
   				</execution>
   			</executions>
               <configuration>这里放插件的配置</configuration>
   		</plugin>
   	</plugins>
   </build>
   ```

   

### 24. 完善案例的业务模型：开发权限管理模块

### 25. 完善案例的业务模型：开发流程审批模块



### 26. 实战: 基于聚合功能实现多模块统一构建

例子: 有三个模块, 组织机构这个模块，修改了代码；权限管理和流程审批都依赖了组织机构. 那么组织机构模块修改后, 子模块都需要构建打包单元测试

**聚合功能:**

1. 多个模块聚合成大模块, 给它一个父工程; 父模块运行构建命令, maven会自动对父模块所有子模块都运行相应的构建命令.

2. 聚合代码:

   ```xml
   <modules>
   	<module>oa-organ</module>
   	<module>oa-auth</module>
   	<module>oa-flow</module>
   </modules>
   ```

   





























