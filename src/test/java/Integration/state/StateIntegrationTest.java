package Integration.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.*;

class StrategyIntegrationTest {

    @Test
    void testStrategiesFullCoverage() {
        DiscountContext ctx = new DiscountContext();

        ctx.setStrategy(new NoDiscount());
        assertEquals(10000, ctx.apply(10000), 0.1);

        ctx.setStrategy(new PercentageDiscount(15));
        assertEquals(8500, ctx.apply(10000), 0.1);

        ctx.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, ctx.apply(100000), 0.1);
    }
}