package Integration.decorator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.PercentageDiscount;

class DecoratorAndStrategyIntegrationTest {

    @Test
    void testDiscountAndDecoratorTogether() {
        DecoratorController decoController = new DecoratorController();
        Product product = decoController.createBaseProduct("Jarabe Infantil", 10000);
        product = decoController.applyTax(product, 19); // IVA

        DiscountContext strategy = new DiscountContext();
        strategy.setStrategy(new PercentageDiscount(10)); // 10% descuento

        double total = strategy.apply(product.getPrice());
        assertEquals(10710, total, 0.1);
    }
}