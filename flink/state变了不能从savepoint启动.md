1. 如果save point的东西多了, 启动不了: 下面, 就是我们减少了一个operator, 结果起不来.
Failed to rollback to checkpoint/savepoint file:/nfsdata/ecs/flink-savepoints/gspdata-subscription-smc-uat/namicg39011u/20201117080205/savepoint-000000-09f5beb4951c. Cannot map checkpoint/savepoint state for operator 0c3ff90a81d0e73f69ce845e5decbea8 to the new program, because the operator is not available in the new program. If you want to allow to skip this, you can set the --allowNonRestoredState option on the CLI.

2. 僧伽一个operator: 有待测试.

