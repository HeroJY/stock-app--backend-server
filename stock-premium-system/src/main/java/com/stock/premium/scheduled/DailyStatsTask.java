package com.stock.premium.scheduled;

import com.stock.premium.service.DailyStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 日统计数据生成定时任务
 * 
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "schedule.daily-stats.enabled", havingValue = "true", matchIfMissing = true)
public class DailyStatsTask {

    @Autowired
    private DailyStatsService dailyStatsService;

    /**
     * 港股收盘后生成日统计数据
     * 每个工作日下午4:30执行
     */
    @Scheduled(cron = "${schedule.daily-stats.cron:0 30 16 * * MON-FRI}")
    public void generateDailyStats() {
        try {
            LocalDate today = LocalDate.now();
            log.info("开始执行日统计数据生成任务: {}", today);
            
            dailyStatsService.generateAllStocksDailyStats(today);
            
            log.info("日统计数据生成任务完成: {}", today);
        } catch (Exception e) {
            log.error("日统计数据生成任务执行失败", e);
        }
    }

    /**
     * 手动触发昨日数据统计（用于补数据）
     * 每天早上9点执行，处理昨日数据
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void generateYesterdayStats() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // 检查昨日是否为工作日
            int dayOfWeek = yesterday.getDayOfWeek().getValue();
            if (dayOfWeek > 5) {
                log.debug("昨日为周末，跳过统计数据生成: {}", yesterday);
                return;
            }
            
            log.info("开始补充昨日统计数据: {}", yesterday);
            dailyStatsService.generateAllStocksDailyStats(yesterday);
            log.info("昨日统计数据补充完成: {}", yesterday);
        } catch (Exception e) {
            log.error("昨日统计数据补充失败", e);
        }
    }
}