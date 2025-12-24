package com.loadtester.controller;

import com.loadtester.service.LoadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoadController {

    private static final Logger logger = LoggerFactory.getLogger(LoadController.class);

    @Autowired
    private LoadManager loadManager;

    @PostMapping("/start/cpu")
    public ResponseEntity<String> startCpuLoad(
            @RequestParam int percent,
            @RequestParam int seconds) {
        try {
            logger.info("接收到CPU负载请求: {}%, {}秒", percent, seconds);
            loadManager.startCpuLoad(percent, seconds);
            return ResponseEntity.ok("CPU负载已启动: " + percent + "% 持续" + seconds + "秒");
        } catch (Exception e) {
            logger.error("启动CPU负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("启动CPU负载失败: " + e.getMessage());
        }
    }

    @PostMapping("/stop/cpu")
    public ResponseEntity<String> stopCpuLoad() {
        try {
            logger.info("接收到停止CPU负载请求");
            loadManager.stopCpuLoad();
            return ResponseEntity.ok("CPU负载已停止");
        } catch (Exception e) {
            logger.error("停止CPU负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("停止CPU负载失败: " + e.getMessage());
        }
    }

    @PostMapping("/start/memory")
    public ResponseEntity<String> startMemoryLoad(
            @RequestParam int percent,
            @RequestParam int seconds) {
        try {
            logger.info("接收到内存负载请求: {}%, {}秒", percent, seconds);
            loadManager.startMemoryLoad(percent, seconds);
            return ResponseEntity.ok("内存负载已启动: " + percent + "% 持续" + seconds + "秒");
        } catch (Exception e) {
            logger.error("启动内存负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("启动内存负载失败: " + e.getMessage());
        }
    }

    @PostMapping("/stop/memory")
    public ResponseEntity<String> stopMemoryLoad() {
        try {
            logger.info("接收到停止内存负载请求");
            loadManager.stopMemoryLoad();
            return ResponseEntity.ok("内存负载已停止");
        } catch (Exception e) {
            logger.error("停止内存负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("停止内存负载失败: " + e.getMessage());
        }
    }

    @PostMapping("/start/disk")
    public ResponseEntity<String> startDiskLoad(
            @RequestParam int percent,
            @RequestParam int seconds) {
        try {
            logger.info("接收到磁盘负载请求: {}%, {}秒", percent, seconds);
            loadManager.startDiskLoad(percent, seconds);
            return ResponseEntity.ok("磁盘负载已启动: " + percent + "% 持续" + seconds + "秒");
        } catch (Exception e) {
            logger.error("启动磁盘负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("启动磁盘负载失败: " + e.getMessage());
        }
    }

    @PostMapping("/stop/disk")
    public ResponseEntity<String> stopDiskLoad() {
        try {
            logger.info("接收到停止磁盘负载请求");
            loadManager.stopDiskLoad();
            return ResponseEntity.ok("磁盘负载已停止");
        } catch (Exception e) {
            logger.error("停止磁盘负载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("停止磁盘负载失败: " + e.getMessage());
        }
    }

    @RequestMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            logger.info("接收到系统状态请求");
            Map<String, Object> status = loadManager.getSystemStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取系统状态失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
