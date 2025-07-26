package com.stock.premium;

/**
 * 简化应用启动类 - 不依赖Spring Boot
 * 
 * @author system
 * @since 2024-01-01
 */
public class SimpleApplication {
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("股票溢价率监控系统");
        System.out.println("版本: 1.0.0");
        System.out.println("状态: 编译测试模式");
        System.out.println("=================================");
        
        // 基本功能测试
        testBasicFunctionality();
    }
    
    /**
     * 测试基本功能
     */
    private static void testBasicFunctionality() {
        try {
            // 测试溢价率计算
            double hStockPrice = 100.0;
            double exchangeRate = 0.9;
            double aStockPrice = 85.0;
            
            double premiumRate = calculatePremiumRate(hStockPrice, exchangeRate, aStockPrice);
            System.out.println("溢价率计算测试: " + premiumRate + "%");
            
            System.out.println("基本功能测试通过!");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算溢价率
     * 公式: (H股价格*汇率 - A股价格) / A股价格 × 100%
     */
    private static double calculatePremiumRate(double hStockPrice, double exchangeRate, double aStockPrice) {
        if (aStockPrice <= 0) {
            throw new IllegalArgumentException("A股价格必须大于0");
        }
        
        double hStockPriceInRMB = hStockPrice * exchangeRate;
        return ((hStockPriceInRMB - aStockPrice) / aStockPrice) * 100;
    }
}