```
ioutil.ReadFile(dir)
```

dir如果是相对路径, 他的base是相对于main方法的.

因为: generate文件夹下的main, 如果不是goland这种path规定的, 取不到"conf/local.yml"文件.