#!/bin/bash

# 编译
javac -d target/classes -cp "lib/*" src/main/java/com/example/sales/**/*.java

# 运行
java -cp "target/classes:lib/*" com.example.sales.SalesApplication
