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
     * 每30秒执行一次数据采集
     */
    @Scheduled(fixedRateString = "${schedule.stock-data-collect.fixed-rate:30000}")
    public void collectStockData() {
        try {
            log.debug("开始执行定时数据采集任务");
            dataCollectionService.collectDataIfTradingTime();
        } catch (Exception e) {
            log.error("定时数据采集任务执行失败", e);
        }
    }
}