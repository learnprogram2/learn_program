https://docs.gradle.org/current/userguide/declaring_dependencies.html

Gradle: implement - compileOnly - testImplementation

2.x的版本scope

1. compile						- 被 implementation 和 api替代

2. provided						- 被 compile only 替代

3. apk

4. test compile

5. debug compile

6. release compile



3.x的版本scope					对应			2.x

1. implementation							compile(但只在当前module生效)

2. api										compile(参与编译和打包)

3. compile only								provided(只编译, 不打包)

4. runtime only 							apk(不编译, 只生成apk时候打包)

5. unit test implementation					

6. test implementation						testCompile(只在单元测试时候管用)

7. debug implementation

8. release implementation


maven: 常用: compile - provided - test
1. compile:									默认, 编译和打包
2. test:									测试相关的依赖, 不编译不打包
3. runtime:									只跳过不编译, 测试和run时候都用, 对于一些编译只需要接口就好了, JDBC的实现就是.
4. provided:								不打包的compile, web container会提供
5. system:									和provided相同, 但是会从本地文件系统找依赖.




