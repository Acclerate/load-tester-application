#!/bin/bash

echo "负载测试应用启动工具"
echo "1. 编译应用"
echo "2. 运行应用"
echo "3. 启动CPU负载"
echo "4. 启动内存负载"
echo "5. 启动磁盘负载"
echo "6. 停止所有负载"

read -p "请选择操作 (1-6): " choice

case $choice in
    1)
        echo "开始编译应用..."
        mvn clean package -DskipTests
        if [ $? -eq 0 ]; then
            echo "编译成功！"
        else
            echo "编译失败！"
            exit 1
        fi
        ;;
    2)
        echo "开始运行应用..."
        java -jar target/load-tester-1.0-SNAPSHOT.jar &
        APP_PID=$!
        echo "应用已启动，进程ID: $APP_PID，端口号：8976"
        echo "请等待应用完全启动后再执行其他操作"
        sleep 5
        ;;
    3)
        read -p "请输入CPU使用率 (1-20): " cpu_percent
        read -p "请输入持续时间 (秒): " cpu_seconds
        echo "启动CPU负载..."
        curl -X POST "http://localhost:8976/start/cpu?percent=$cpu_percent&seconds=$cpu_seconds"
        echo
        ;;
    4)
        read -p "请输入内存使用率 (1-20): " mem_percent
        read -p "请输入持续时间 (秒): " mem_seconds
        echo "启动内存负载..."
        curl -X POST "http://localhost:8976/start/memory?percent=$mem_percent&seconds=$mem_seconds"
        echo
        ;;
    5)
        read -p "请输入磁盘使用率 (1-20): " disk_percent
        read -p "请输入持续时间 (秒): " disk_seconds
        echo "启动磁盘负载..."
        curl -X POST "http://localhost:8976/start/disk?percent=$disk_percent&seconds=$disk_seconds"
        echo
        ;;
    6)
        echo "停止所有负载..."
        curl -X POST "http://localhost:8976/stop/cpu"
        echo
        curl -X POST "http://localhost:8976/stop/memory"
        echo
        curl -X POST "http://localhost:8976/stop/disk"
        echo
        ;;
    *)
        echo "无效的选择！"
        exit 1
        ;;
esac

echo "操作完成！"
