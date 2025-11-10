package e2e.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.*;

class DiscountStrategyE2ETest {

    @Test
    void testDifferentDiscountStrategies() {
        DiscountContext context = new DiscountContext();

        context.setStrategy(new NoDiscount());
        assertEquals(10000, context.apply(10000), 0.1);

        context.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, context.apply(10000), 0.1);

        context.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, context.apply(100000), 0.1);
    }
}