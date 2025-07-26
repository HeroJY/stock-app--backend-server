package com.stock.premium;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单测试类
 */
public class SimpleTest {

    @Test
    public void testBasic() {
        String result = "Hello World";
        assertNotNull(result);
        assertEquals("Hello World", result);
    }
}