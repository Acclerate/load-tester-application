package com.loadtester.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class LoadManager {

    private static final Logger logger = LoggerFactory.getLogger(LoadManager.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private List<Future<?>> cpuTasks = new CopyOnWriteArrayList<>();
    private List<byte[]> memoryArrays = new CopyOnWriteArrayList<>();
    private File diskFile = null;

    @PostConstruct
    public void init() {
        logger.info("LoadManager initialized");
    }

    public void startCpuLoad(int percent, int seconds) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        double targetPerCore = (double) percent / 100 / availableProcessors;
        percent = Math.min(percent, 20); // 限制最大CPU利用率为20%
        
        for (int i = 0; i < availableProcessors; i++) {
            Future<?> future = executorService.submit(() -> {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + seconds * 1000;
                while (System.currentTimeMillis() < endTime) {
                    long busyTime = (long) (100 * targetPerCore);
                    long idleTime = 100 - busyTime;
                    // 忙等待
                    long busyStart = System.nanoTime();
                    while (System.nanoTime() - busyStart < busyTime * 1000000) {}
                    // 空闲时间
                    try { Thread.sleep(idleTime); } catch (InterruptedException e) { break; }
                }
            });
            cpuTasks.add(future);
        }
        scheduler.schedule(this::stopCpuLoad, seconds, TimeUnit.SECONDS);
    }

    public void stopCpuLoad() {
        for (Future<?> task : cpuTasks) {
            task.cancel(true);
        }
        cpuTasks.clear();
        logger.info("CPU负载已停止");
    }

    public void startMemoryLoad(int percent, int seconds) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = 0;
        try {
            Method method = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
            totalMemory = (long) method.invoke(osBean);
        } catch (Exception e) {
            logger.error("获取系统内存信息失败: {}", e.getMessage());
            return;
        }

        long targetMemory = (long) (totalMemory * percent / 100);
        targetMemory = Math.min(targetMemory, totalMemory / 5); // 限制最大内存使用为系统的20%

        try {
            // 分配内存并触摸每个页面
            byte[] memoryArray = new byte[(int) targetMemory];
            for (int i = 0; i < memoryArray.length; i += 4096) {
                memoryArray[i] = 1;
            }
            memoryArrays.add(memoryArray);
            logger.info("内存负载已启动: 分配了 {} MB 内存", targetMemory / 1024 / 1024);
            scheduler.schedule(this::stopMemoryLoad, seconds, TimeUnit.SECONDS);
        } catch (OutOfMemoryError e) {
            logger.error("内存分配失败: {}", e.getMessage());
            throw e;
        }
    }

    public void stopMemoryLoad() {
        memoryArrays.clear();
        System.gc();
        logger.info("内存负载已停止");
    }

    public void startDiskLoad(int percent, int seconds) {
        try {
            Path tempPath = Files.createTempDirectory("load-test");
            File tempDir = tempPath.toFile();
            
            // 获取磁盘信息
            long totalSpace = tempDir.getTotalSpace();
            long availableSpace = tempDir.getUsableSpace();
            long targetSpace = (long) (totalSpace * percent / 100);
            targetSpace = Math.min(targetSpace, availableSpace / 5); // 限制最大磁盘使用为可用空间的20%
            
            diskFile = new File(tempDir, "disk-load-test.bin");
            logger.info("磁盘负载已启动: 将创建 {} MB 文件", targetSpace / 1024 / 1024);
            
            // 创建大文件
            try (RandomAccessFile raf = new RandomAccessFile(diskFile, "rw")) {
                raf.setLength(targetSpace);
                // 写入一些数据以确保文件被实际创建
                byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
                int chunks = (int) (targetSpace / buffer.length);
                for (int i = 0; i < chunks; i++) {
                    raf.write(buffer);
                }
            }
            
            scheduler.schedule(this::stopDiskLoad, seconds, TimeUnit.SECONDS);
        } catch (IOException e) {
            logger.error("磁盘负载创建失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void stopDiskLoad() {
        if (diskFile != null) {
            File parentDir = diskFile.getParentFile();
            if (diskFile.delete()) {
                logger.info("磁盘负载文件已删除");
            }
            if (parentDir != null && parentDir.delete()) {
                logger.info("临时目录已删除");
            }
            diskFile = null;
        }
        logger.info("磁盘负载已停止");
    }

    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        // CPU 使用率
        try {
            Method method = osBean.getClass().getMethod("getSystemCpuLoad");
            double cpuLoad = (double) method.invoke(osBean);
            status.put("cpuLoad", Math.round(cpuLoad * 100));
        } catch (Exception e) {
            status.put("cpuLoad", "N/A");
        }
        
        // 内存信息
        try {
            Method totalMemMethod = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
            Method freeMemMethod = osBean.getClass().getMethod("getFreePhysicalMemorySize");
            
            long totalMemory = (long) totalMemMethod.invoke(osBean);
            long freeMemory = (long) freeMemMethod.invoke(osBean);
            long usedMemory = totalMemory - freeMemory;
            int memoryUsage = (int) (usedMemory * 100 / totalMemory);
            
            status.put("totalMemory", totalMemory / 1024 / 1024);
            status.put("usedMemory", usedMemory / 1024 / 1024);
            status.put("memoryUsage", memoryUsage);
        } catch (Exception e) {
            status.put("memory", "N/A");
        }
        
        // 磁盘信息
        try {
            File tempDir = Files.createTempDirectory("status-check").toFile();
            long totalSpace = tempDir.getTotalSpace();
            long availableSpace = tempDir.getUsableSpace();
            long usedSpace = totalSpace - availableSpace;
            int diskUsage = (int) (usedSpace * 100 / totalSpace);
            
            status.put("totalDisk", totalSpace / 1024 / 1024);
            status.put("usedDisk", usedSpace / 1024 / 1024);
            status.put("diskUsage", diskUsage);
            
            tempDir.delete();
        } catch (Exception e) {
            status.put("disk", "N/A");
        }
        
        return status;
    }
}
