package com.stock.premium;

import com.stock.premium.scheduled.SimpleDataCollectionTask;
import com.stock.premium.service.SimpleDataCollectionService;

/**
 * 简化版股票溢价率监控应用
 * 不依赖Spring框架，可以独立运行
 * 
 * @author system
 * @since 2024-01-01
 */
public class SimpleStockApplication {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("股票溢价率监控系统 - 简化版启动");
        System.out.println("=================================");
        
        try {
            // 创建数据采集服务
            SimpleDataCollectionService dataService = new SimpleDataCollectionService();
            
            // 创建定时任务
            SimpleDataCollectionTask scheduledTask = new SimpleDataCollectionTask(null);
            
            // 启动定时任务
            scheduledTask.startScheduledTask();
            
            // 手动执行一次数据采集测试
            System.out.println("\n执行初始数据采集测试:");
            dataService.collectDataIfTradingTime();
            
            System.out.println("\n系统运行中... 按 Ctrl+C 退出");
            
            // 保持程序运行
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n正在关闭系统...");
                scheduledTask.stopScheduledTask();
                System.out.println("系统已关闭");
            }));
            
            // 主线程等待
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}