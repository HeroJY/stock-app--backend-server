package com.stock.premium.scheduled;

import com.stock.premium.service.DataCollectionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 简化版数据采集定时任务
 * 不依赖Spring注解，使用Java原生定时器
 * 
 * @author system
 * @since 2024-01-01
 */
public class SimpleDataCollectionTask {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private DataCollectionService dataCollectionService;
    
    public SimpleDataCollectionTask(DataCollectionService dataCollectionService) {
        this.dataCollectionService = dataCollectionService;
    }
    
    /**
     * 启动定时任务，每30秒执行一次数据采集
     */
    public void startScheduledTask() {
        scheduler.scheduleAtFixedRate(this::collectStockData, 0, 30, TimeUnit.SECONDS);
        System.out.println("数据采集定时任务已启动，每30秒执行一次");
    }
    
    /**
     * 停止定时任务
     */
    public void stopScheduledTask() {
        scheduler.shutdown();
        System.out.println("数据采集定时任务已停止");
    }
    
    /**
     * 执行数据采集
     */
    private void collectStockData() {
        try {
            System.out.println("开始执行定时数据采集任务");
            if (dataCollectionService != null) {
                dataCollectionService.collectDataIfTradingTime();
            } else {
                System.out.println("模拟数据采集：获取股票价格和汇率数据");
            }
        } catch (Exception e) {
            System.err.println("定时数据采集任务执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}