# 负载测试应用使用说明

## 安全警告

**⚠️ 警告：请勿在生产环境使用此工具！**

## 环境要求

- Java 8 或更高版本
- Linux 操作系统（推荐）
- Maven 3.6+（用于编译）

## 项目说明

这是一个基于 Spring Boot 的负载测试应用，可以用于生成 CPU、内存和磁盘负载。

### API 端点

#### CPU 负载
- **POST** `/start/cpu`
  - 参数：
    - `percent`: CPU 使用率目标百分比（1-20）
    - `seconds`: 负载持续时间（秒）
  - 示例：`curl -X POST "http://localhost:8976/start/cpu?percent=20&seconds=3600"`

- **POST** `/stop/cpu`
  - 示例：`curl -X POST "http://localhost:8976/stop/cpu"`

#### 内存负载
- **POST** `/start/memory`
  - 参数：
    - `percent`: 内存使用率目标百分比（1-20）
    - `seconds`: 负载持续时间（秒）
  - 示例：`curl -X POST "http://localhost:8976/start/memory?percent=20&seconds=3600"`

- **POST** `/stop/memory`
  - 示例：`curl -X POST "http://localhost:8976/stop/memory"`

#### 磁盘负载
- **POST** `/start/disk`
  - 参数：
    - `percent`: 磁盘使用率目标百分比（1-20）
    - `seconds`: 负载持续时间（秒）
  - 示例：`curl -X POST "http://localhost:8976/start/disk?percent=20&seconds=3600"`

- **POST** `/stop/disk`
  - 示例：`curl -X POST "http://localhost:8976/stop/disk"`

#### 系统状态
- **GET** `/status`
  - 示例：`curl "http://localhost:8976/status"`

## 负载实现说明

### CPU 负载

CPU 负载通过在每个 CPU 核心上创建一个线程，控制线程的忙等待和空闲时间比例来实现：

1. 计算每个核心需要达到的目标使用率（例如总目标 20%，4 核心 CPU 则每个核心需要 5%）
2. 在一个循环中，线程执行忙等待（消耗 CPU）一段时间，然后睡眠（空闲）一段时间
3. 忙等待时间和空闲时间的比例根据目标使用率调整
4. 例如：20% 负载意味着每个 100ms 周期内，线程忙等待 20ms，睡眠 80ms

### 内存负载

内存负载通过分配指定大小的字节数组并触摸每个页面来实现：

1. 计算系统总内存和可用内存
2. 根据目标百分比计算需要分配的内存大小
3. 分配一个大字节数组，大小为计算出的内存大小
4. 遍历数组并触摸每个页面，确保内存实际被分配

### 磁盘负载

磁盘负载通过在临时目录创建大文件来实现：

1. 检查临时目录所在的文件系统的总大小和可用空间
2. 根据目标百分比计算需要使用的磁盘空间
3. 在临时目录创建一个文件，并写入随机数据直到达到目标大小
4. 负载完成后，删除创建的临时文件

## 使用脚本

### Windows (run-load-trigger.bat)

1. 编译应用：
   ```
   run-load-trigger.bat compile
   ```

2. 运行应用：
   ```
   run-load-trigger.bat run
   ```

3. 触发 CPU 负载：
   ```
   run-load-trigger.bat cpu
   ```

4. 触发内存负载：
   ```
   run-load-trigger.bat memory
   ```

5. 触发磁盘负载：
   ```
   run-load-trigger.bat disk
   ```

6. 停止所有负载：
   ```
   run-load-trigger.bat stop
   ```

### Linux/Mac (run-load-trigger.sh)

1. 编译应用：
   ```
   ./run-load-trigger.sh compile
   ```

2. 运行应用：
   ```
   ./run-load-trigger.sh run
   ```

3. 触发 CPU 负载：
   ```
   ./run-load-trigger.sh cpu
   ```

4. 触发内存负载：
   ```
   ./run-load-trigger.sh memory
   ```

5. 触发磁盘负载：
   ```
   ./run-load-trigger.sh disk
   ```

6. 停止所有负载：
   ```
   ./run-load-trigger.sh stop
   ```
