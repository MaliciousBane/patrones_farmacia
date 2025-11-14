package e2e.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.*;

class DiscountStrategyE2ETest {

    @Test
    void shouldApplyDifferentDiscountStrategiesCorrectly() {
        DiscountContext context = new DiscountContext();

        context.setStrategy(new NoDiscount());
        assertEquals(10000, context.apply(10000), 0.1, 
                     "Sin descuento debe retornar el precio original");

        context.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, context.apply(10000), 0.1, 
                     "10% de descuento debe reducir el precio a 9000");

        context.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, context.apply(100000), 0.1, 
                     "20% de descuento VIP debe reducir 100000 a 80000");
    }
}