
> *低级语言*分**机器语言**(二进制语言)和**汇编语言**(符号语言). 
      特定的汇编语言和特定的机器语言指令集是一一对应, 不同平台之间不可以直接移植. 操作的对象不是具体的数据,而是寄存器或者存储器, 面向处理器编程.
  *高级语言*是相对于汇编语言而言的, 它是脱离了机器的硬件系统, 高度封装了的, 比如java,c,c++,C#,pascal,python,lisp.
  所以shell语言 也是高级语言.  

脚本是命令集合, 这些命令由解释器执行.

### shell脚本实例:
```shell script
#!/bin/sh
cd ~
mkdir shell_tut
cd shell_tut

for ((i=0; i<10; i++)); do
	touch test_$i.txt
done
```
解释: 第一行的`#!/bin/sh`是指定脚本解释器, `/bin/sh`是一个解释器.

### shell 和 shell 脚本概念:
shell是一个应用程序, 它提供了用户和系统交互界面. Ken Thompson的sh是第一种UnixShell, WindowsExplorer是一个典型的图形界面Shell.
shell脚本是为shell编写的命令集合.

#### shell编程:
shell只定义了一个非常简单的编程语言, 它是解释型的, 一边解释一边执行. 功能不多.

#### 环境: 脚本解释器
有文本编辑器和一个能解释执行的脚本解释器就可以了.
解释器: 
1. sh(Bourne shell): 路径经常放在`/bin/sh`, 
2. bash: sh的替代品, 路径是`/bin/bash`, CentOS里, `/bin/sh`是指向`/bin/bash`的符号链接.

各个系统默认的:
Linux: Linux默认安装就带了shell解释器.
Mac OS: Mac OS不仅带了sh, bash这两个最基础的解释器, 还内置了ksh, csh, zsh等不常用的解释器.
Windows上的模拟器: windows出厂时没有内置shell解释器, 需要自行安装, 为了同时能用grep, awk, curl等工具, 最好装一个cygwin或者mingw来模拟linux环境.



### 高级编程语言
理论上讲, 只要一门语言提供了解释器(而不仅是编译器), 这门语言就可以胜任脚本编程. 
1. 解释型语言都是可以用作脚本编程. 如:Perl, Tcl, Python, PHP, Ruby.
2. 编译型语言, 只要有解释器, 也可以用作脚本编程. 内置的CShell, Java的jShell










