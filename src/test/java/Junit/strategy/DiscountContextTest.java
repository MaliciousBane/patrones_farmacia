package Junit.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.model.*;
import patrones_farmacia.strategy.controller.*;

class DiscountContextTest {

    @Test
    void testPercentageDiscount() {
        DiscountContext context = new DiscountContext();
        context.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, context.apply(10000), 0.1);
    }

    @Test
    void testVipDiscountAppliesWhenThresholdMet() {
        DiscountContext context = new DiscountContext();
        context.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, context.apply(100000), 0.1);
    }

    @Test
    void testNoDiscountStrategy() {
        DiscountContext context = new DiscountContext();
        context.setStrategy(new NoDiscount());
        assertEquals(10000, context.apply(10000), 0.1);
    }
}
