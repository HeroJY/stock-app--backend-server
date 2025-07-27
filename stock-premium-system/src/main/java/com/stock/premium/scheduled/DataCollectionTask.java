package com.stock.premium.scheduled;

import com.stock.premium.service.DataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据采集定时任务
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "schedule.stock-data-collect.enabled", havingValue = "true", matchIfMissing = true)
public class DataCollectionTask {

    @Autowired
    private DataCollectionService dataCollectionService;

    /**
     * 上午交易时间数据采集：9:30-12:00，每30秒执行一次
     * 工作日上午9:30:00, 9:30:30, 9:31:00... 直到12:00:00
     */
    @Scheduled(cron = "0/30 30-59 9 * * MON-FRI")
    public void collectStockDataMorning1() {
        executeDataCollection();
    }
    
    @Scheduled(cron = "0/30 * 10-11 * * MON-FRI")
    public void collectStockDataMorning2() {
        executeDataCollection();
    }
    
    @Scheduled(cron = "0/30 0 12 * * MON-FRI")
    public void collectStockDataMorning3() {
        executeDataCollection();
    }

    /**
     * 下午交易时间数据采集：13:00-16:30，每30秒执行一次
     * 工作日下午1:00:00, 1:00:30, 1:01:00... 直到4:30:00
     */
    @Scheduled(cron = "0/30 * 13-15 * * MON-FRI")
    public void collectStockDataAfternoon1() {
        executeDataCollection();
    }
    
    @Scheduled(cron = "0/30 0-30 16 * * MON-FRI")
    public void collectStockDataAfternoon2() {
        executeDataCollection();
    }

    /**
     * 执行数据采集的具体逻辑
     */
    private void executeDataCollection() {
        try {
            log.debug("开始执行定时数据采集任务");
            dataCollectionService.collectAllStockData();
        } catch (Exception e) {
            log.error("定时数据采集任务执行失败", e);
        }
    }
}