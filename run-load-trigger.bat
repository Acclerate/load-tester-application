@echo off

setlocal enabledelayedexpansion

echo 负载测试应用启动工具
set /p "choice=请选择操作 (1-编译, 2-运行, 3-CPU负载, 4-内存负载, 5-磁盘负载, 6-停止负载): "

if "!choice!"=="1" (
    echo 开始编译应用...
    call mvn clean package -DskipTests
    if %ERRORLEVEL% equ 0 (
        echo 编译成功！
    ) else (
        echo 编译失败！
        pause
        exit /b 1
    )
) else if "!choice!"=="2" (
    echo 开始运行应用...
    start "负载测试应用" java -jar target/load-tester-1.0-SNAPSHOT.jar
    echo 应用已启动，端口号：8976
    echo 请等待应用完全启动后再执行其他操作
    timeout /t 5 /nobreak
) else if "!choice!"=="3" (
    set /p "cpu_percent=请输入CPU使用率 (1-20): "
    set /p "cpu_seconds=请输入持续时间 (秒): "
    echo 启动CPU负载...
    curl -X POST "http://localhost:8976/start/cpu?percent=!cpu_percent!&seconds=!cpu_seconds!"
    echo.
) else if "!choice!"=="4" (
    set /p "mem_percent=请输入内存使用率 (1-20): "
    set /p "mem_seconds=请输入持续时间 (秒): "
    echo 启动内存负载...
    curl -X POST "http://localhost:8976/start/memory?percent=!mem_percent!&seconds=!mem_seconds!"
    echo.
) else if "!choice!"=="5" (
    set /p "disk_percent=请输入磁盘使用率 (1-20): "
    set /p "disk_seconds=请输入持续时间 (秒): "
    echo 启动磁盘负载...
    curl -X POST "http://localhost:8976/start/disk?percent=!disk_percent!&seconds=!disk_seconds!"
    echo.
) else if "!choice!"=="6" (
    echo 停止所有负载...
    curl -X POST "http://localhost:8976/stop/cpu"
    echo.
    curl -X POST "http://localhost:8976/stop/memory"
    echo.
    curl -X POST "http://localhost:8976/stop/disk"
    echo.
) else (
    echo 无效的选择！
    pause
    exit /b 1
)

echo 操作完成！
pause
