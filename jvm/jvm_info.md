# JVM info
> 系统是22核393GB的机器, 在上面部署各种container, 






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











