# JVM info
> 机器: 系统是22核393GB的机器, 在上面部署各种container 

```shell
## 使用 `ps -fea|grep -i java`命令
> 1. jobmanager: 
> 堆内存1G, 然后定义了撇脂文件和job的名字, 还有spring的环境`--spring.profiles.active=UAT`
> 2. taskmanager:
> 使用G1GC, 堆内存:2.7G.
jobmanager
1001260+     84      1  1 03:22 ?        00:03:26 java -Xms1024m -Xmx1024m -classpath /opt/flink/flink-1.9.2/lib/a-a-flink-1.9.2-setup-patch.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-api-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-core-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-slf4j-impl-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-flink-1.9.2-patch.jar:/opt/flink/flink-1.9.2/lib/a-flinkmanager-rest-patch.jar:/opt/flink/flink-1.9.2/lib/commons-lang-2.6.jar:/opt/flink/flink-1.9.2/lib/flink-metrics-prometheus-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-queryable-state-runtime_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-table-blink_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-table_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/json-smart-1.1.1.jar:/opt/flink/flink-1.9.2/lib/jsonevent-layout-1.7.jar:/opt/flink/flink-1.9.2/lib/log4j-1.2.17.jar:/opt/flink/flink-1.9.2/lib/slf4j-log4j12-1.7.15.jar:/opt/flink/flink-1.9.2/lib/z-flink-application.jar:/opt/flink/flink-1.9.2/lib/z-jackson-annotations-2.11.1.jar:/opt/flink/flink-1.9.2/lib/z-jackson-core-2.11.1.jar:/opt/flink/flink-1.9.2/lib/z-jackson-databind-2.11.1.jar:/opt/flink/flink-1.9.2/lib/flink-dist_2.12-1.9.2.jar::: org.apache.flink.container.entrypoint.StandaloneJobClusterEntryPoint --configDir /opt/flink/flink-1.9.2/conf --job-classname com.gspdata.subscription.trade.TradeApplication --spring.profiles.active=UAT -env uat
taskmanager
1001260+     86      1 16 03:22 ?        01:05:01 java -XX:+UseG1GC -Xms2765M -Xmx2765M -XX:MaxDirectMemorySize=8388607T -classpath /opt/flink/flink-1.9.2/lib/a-a-flink-1.9.2-setup-patch.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-api-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-core-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-a-log4j-slf4j-impl-2.11.1.jar:/opt/flink/flink-1.9.2/lib/a-flink-1.9.2-patch.jar:/opt/flink/flink-1.9.2/lib/a-flinkmanager-rest-patch.jar:/opt/flink/flink-1.9.2/lib/commons-lang-2.6.jar:/opt/flink/flink-1.9.2/lib/flink-metrics-prometheus-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-queryable-state-runtime_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-table-blink_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/flink-table_2.12-1.9.2.jar:/opt/flink/flink-1.9.2/lib/json-smart-1.1.1.jar:/opt/flink/flink-1.9.2/lib/jsonevent-layout-1.7.jar:/opt/flink/flink-1.9.2/lib/log4j-1.2.17.jar:/opt/flink/flink-1.9.2/lib/slf4j-log4j12-1.7.15.jar:/opt/flink/flink-1.9.2/lib/z-flink-application.jar:/opt/flink/flink-1.9.2/lib/z-jackson-annotations-2.11.1.jar:/opt/flink/flink-1.9.2/lib/z-jackson-core-2.11.1.jar:/opt/flink/flink-1.9.2/lib/z-jackson-databind-2.11.1.jar:/opt/flink/flink-1.9.2/lib/flink-dist_2.12-1.9.2.jar::: org.apache.flink.runtime.taskexecutor.TaskManagerRunner --configDir /opt/flink/flink-1.9.2/conf

# =====================================================================================
## 使用 `./jcmd 84 VM.flags` 命令
> MinHeapDeltaBytes(最小的GC收集数量):192kb, 使用class的压缩指针和对象的压缩指针. 
> 1. jobmanager
> InitialHeapSize(堆内存), MaxHeapSize(最大堆内存): 1G
> NewSize, MaxNewSize(新生代): 341MB, OldSize(老年代): 682.6MB, 
> 2. taskmanager:
> 堆总共1.8GB, 新生代614.6MB, 老年代1.2G, 
# jobManager: JVM infor
-XX:CICompilerCount=2 -XX:InitialHeapSize=1073741824 -XX:MaxHeapSize=1073741824 -XX:MaxNewSize=357892096 -XX:MinHeapDeltaBytes=196608 
-XX:NewSize=357892096 -XX:OldSize=715849728 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps 
# taskManager: 
-XX:CICompilerCount=2 -XX:InitialHeapSize=1933574144 -XX:MaxDirectMemorySize=9223370937343148032 -XX:MaxHeapSize=1933574144 -XX:MaxNewSize=644481024 
-XX:MinHeapDeltaBytes=196608 -XX:NewSize=644481024 -XX:OldSize=1289093120 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps 
# =====================================================================================

## 使用 `./jcmd 84 VM.system_properties` 查看JVM信息
84:
#Tue Aug 18 03:55:53 UTC 2020
zookeeper.sasl.client=true
java.runtime.name=Java(TM) SE Runtime Environment
sun.boot.library.path=/opt/jre/1.8.0_211l64/lib/amd64
java.vm.version=25.211-b12
java.vm.vendor=Oracle Corporation
java.vendor.url=http\://java.oracle.com/
path.separator=\:
java.vm.name=Java HotSpot(TM) 64-Bit Server VM
file.encoding.pkg=sun.io
user.country=US
sun.java.launcher=SUN_STANDARD
sun.os.patch.level=unknown
java.vm.specification.name=Java Virtual Machine Specification
user.dir=/opt/flink/flink-1.9.2/lib
PID=84
java.runtime.version=1.8.0_211-b12
java.awt.graphicsenv=sun.awt.X11GraphicsEnvironment
java.endorsed.dirs=/opt/jre/1.8.0_211l64/lib/endorsed
os.arch=amd64
java.io.tmpdir=/tmp
line.separator=\n
java.vm.specification.vendor=Oracle Corporation
os.name=Linux
sun.jnu.encoding=ANSI_X3.4-1968
java.library.path=/usr/java/packages/lib/amd64\:/usr/lib64\:/lib64\:/lib\:/usr/lib
spring.beaninfo.ignore=true
sun.nio.ch.bugLevel=
java.specification.name=Java Platform API Specification
java.class.version=52.0
sun.management.compiler=HotSpot 64-Bit Tiered Compilers
os.version=3.10.0-1062.18.1.el7.x86_64
user.home=?
user.timezone=UTC
java.awt.printerjob=sun.print.PSPrinterJob
file.encoding=ANSI_X3.4-1968
java.specification.version=1.8
java.class.path=/opt/flink/flink-1.9.2/lib/a-a-flink-1.9.2-setup-patch.jar\:/opt/flink/flink-1.9.2/lib/a-a-log4j-api-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/a-a-log4j-core-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/a-a-log4j-slf4j-impl-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/a-flink-1.9.2-patch.jar\:/opt/flink/flink-1.9.2/lib/a-flinkmanager-rest-patch.jar\:/opt/flink/flink-1.9.2/lib/commons-lang-2.6.jar\:/opt/flink/flink-1.9.2/lib/flink-metrics-prometheus-1.9.2.jar\:/opt/flink/flink-1.9.2/lib/flink-queryable-state-runtime_2.12-1.9.2.jar\:/opt/flink/flink-1.9.2/lib/flink-table-blink_2.12-1.9.2.jar\:/opt/flink/flink-1.9.2/lib/flink-table_2.12-1.9.2.jar\:/opt/flink/flink-1.9.2/lib/json-smart-1.1.1.jar\:/opt/flink/flink-1.9.2/lib/jsonevent-layout-1.7.jar\:/opt/flink/flink-1.9.2/lib/log4j-1.2.17.jar\:/opt/flink/flink-1.9.2/lib/slf4j-log4j12-1.7.15.jar\:/opt/flink/flink-1.9.2/lib/z-flink-application.jar\:/opt/flink/flink-1.9.2/lib/z-jackson-annotations-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/z-jackson-core-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/z-jackson-databind-2.11.1.jar\:/opt/flink/flink-1.9.2/lib/flink-dist_2.12-1.9.2.jar\:\:\:
user.name=?
java.vm.specification.version=1.8
sun.java.command=org.apache.flink.container.entrypoint.StandaloneJobClusterEntryPoint --configDir /opt/flink/flink-1.9.2/conf --job-classname com.gspdata.subscription.trade.TradeApplication --spring.profiles.active\=UAT -env uat
java.home=/opt/jre/1.8.0_211l64
sun.arch.data.model=64
user.language=en
java.specification.vendor=Oracle Corporation
awt.toolkit=sun.awt.X11.XToolkit
java.vm.info=mixed mode
java.version=1.8.0_211
java.ext.dirs=/opt/jre/1.8.0_211l64/lib/ext\:/usr/java/packages/lib/ext
sun.boot.class.path=/opt/jre/1.8.0_211l64/lib/resources.jar\:/opt/jre/1.8.0_211l64/lib/rt.jar\:/opt/jre/1.8.0_211l64/lib/sunrsasign.jar\:/opt/jre/1.8.0_211l64/lib/jsse.jar\:/opt/jre/1.8.0_211l64/lib/jce.jar\:/opt/jre/1.8.0_211l64/lib/charsets.jar\:/opt/jre/1.8.0_211l64/lib/jfr.jar\:/opt/jre/1.8.0_211l64/classes
java.awt.headless=true
java.vendor=Oracle Corporation
java.security.auth.login.config=/tmp/jaas-3811447641443138264.conf
file.separator=/
java.vendor.url.bug=http\://bugreport.sun.com/bugreport/
sun.io.unicode.encoding=UnicodeLittle
sun.cpu.endian=little
sun.cpu.isalist=
```





## CPU Info
> 22核的2GHz的cpu. [get the info](https://alvinalexander.com/linux-unix/linux-processor-cpu-memory-information-commands/)
```shell
bash-4.2$ cat /proc/cpuinfo
# 每个processor有一个ID
processor       : 0
vendor_id       : GenuineIntel
# processor的类型, 如果是intel-based系统, 就是86前的值. 由李云检测老系统的体系架构, 并且有助于确认哪个RPM包更适合.
cpu family      : 6
model           : 85
# cpu名字
model name      : Intel(R) Xeon(R) Gold 6138 CPU @ 2.00GHz
stepping        : 4
microcode       : 0x200005e
cpu MHz         : 1995.312
cache size      : 28160 KB
physical id     : 0
siblings        : 1
core id         : 0
cpu cores       : 1
apicid          : 0
initial apicid  : 0
fpu             : yes
fpu_exception   : yes
cpuid level     : 13
wp              : yes
flags           : fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts mmx fxsr sse sse2 ss syscall nx pdpe1gb rdtscp lm constant_tsc arch_perfmon pebs bts nopl xtopology tsc_reliable nonstop_tsc eagerfpu pni pclmulqdq ssse3 fma cx16 pcid sse4_1 sse4_2 x2apic movbe popcnt aes xsave avx f16c rdrand hypervisor lahf_lm 3dnowprefetch ssbd ibrs ibpb stibp fsgsbase smep arat md_clear spec_ctrl intel_stibp flush_l1d arch_capabilities
bogomips        : 3990.62
clflush size    : 64
cache_alignment : 64
address sizes   : 40 bits physical, 48 bits virtual
power management:
# 22个processor, 都是一样的, 就是ID不一样.
```

## Memory Info
> 393.48GB的
```shell
bash-4.2$ cat /proc/meminfo 
MemTotal:       412594324 kB
MemFree:        17225876 kB
MemAvailable:   379521396 kB
Buffers:            7504 kB
Cached:         53007684 kB
SwapCached:            0 kB
Active:         51479800 kB
Inactive:       23566720 kB
Active(anon):   22064748 kB
Inactive(anon):     3548 kB
Active(file):   29415052 kB
Inactive(file): 23563172 kB
Unevictable:      156336 kB
Mlocked:          156364 kB
SwapTotal:             0 kB
SwapFree:              0 kB
Dirty:              1148 kB
Writeback:             0 kB
AnonPages:      22189300 kB
Mapped:          1448600 kB
Shmem:             15880 kB
Slab:           316715552 kB
SReclaimable:   311203892 kB
SUnreclaim:      5511660 kB
KernelStack:       68336 kB
PageTables:       117656 kB
NFS_Unstable:          0 kB
Bounce:                0 kB
WritebackTmp:          0 kB
CommitLimit:    206297160 kB
Committed_AS:   49889644 kB
VmallocTotal:   34359738367 kB
VmallocUsed:     1203016 kB
VmallocChunk:   34358310908 kB
HardwareCorrupted:     0 kB
AnonHugePages:   6002688 kB
CmaTotal:              0 kB
CmaFree:               0 kB
HugePages_Total:       0
HugePages_Free:        0
HugePages_Rsvd:        0
HugePages_Surp:        0
Hugepagesize:       2048 kB
DirectMap4k:      331584 kB
DirectMap2M:    15396864 kB
DirectMap1G:    405798912 kB
```











