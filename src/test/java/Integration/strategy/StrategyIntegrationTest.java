package Integration.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.*;

class StrategyIntegrationTest {

    @Test
    void testDifferentDiscountStrategiesIntegration() {
        DiscountContext context = new DiscountContext();

        context.setStrategy(new NoDiscount());
        assertEquals(10000, context.apply(10000), 0.1);

        context.setStrategy(new PercentageDiscount(15));
        assertEquals(8500, context.apply(10000), 0.1);

        context.setStrategy(new VIPClientDiscount(50000, 25));
        assertEquals(75000, context.apply(100000), 0.1);
    }
}