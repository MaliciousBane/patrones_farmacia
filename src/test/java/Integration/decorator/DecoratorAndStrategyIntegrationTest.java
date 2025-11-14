package Integration.decorator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;
import patrones_farmacia.decorator.model.BaseProduct;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.PercentageDiscount;

class DecoratorAndStrategyIntegrationTest {

    private DecoratorController controller;
    private DiscountContext context;

    @BeforeEach
    void init() {
        controller = new DecoratorController();
        context = new DiscountContext();
    }

    @Test
    void baseProductCreatesCorrectlyAndDescribeWorks() {
        Product p = controller.createBaseProduct("Jarabe", 5000);
        String desc = controller.describeProduct(p);
        assertTrue(desc.contains("Jarabe"));
        assertTrue(desc.contains("5000.00"));
        assertEquals(5000.0, controller.calculateFinalPrice(p), 0.001);
    }

    @Test
    void applyTaxAddsCorrectAmount() {
        Product p = controller.createBaseProduct("Crema", 2000);
        Product taxed = controller.applyTax(p, 10);
        assertTrue(taxed.getDescription().contains("IVA 10"));
        assertEquals(2200.0, taxed.getPrice(), 0.001);
    }

    @Test
    void applyDiscountReducesCorrectAmount() {
        Product p = controller.createBaseProduct("Ungüento", 4000);
        Product disc = controller.applyDiscount(p, 25);
        assertTrue(disc.getDescription().contains("Descuento 25"));
        assertEquals(3000.0, disc.getPrice(), 0.001);
    }

    @Test
    void discountThenTaxAndTaxThenDiscountProduceExpectedMultiplicativeResult() {
        Product base = controller.createBaseProduct("Jarabe Infantil", 10000);
        Product taxThenDisc = controller.applyDiscount(controller.applyTax(base, 19), 10);
        Product discThenTax = controller.applyDiscountThenTax(base, 10, 19);
        double expected = 10000.0 * 1.19 * 0.90;
        assertEquals(expected, taxThenDisc.getPrice(), 0.001);
        assertEquals(expected, discThenTax.getPrice(), 0.001);
    }

    @Test
    void zeroPercentDiscountAndTaxKeepPrice() {
        Product base = controller.createBaseProduct("Seda", 7500);
        Product noDisc = controller.applyDiscount(base, 0);
        Product noTax = controller.applyTax(base, 0);
        assertEquals(7500.0, noDisc.getPrice(), 0.001);
        assertEquals(7500.0, noTax.getPrice(), 0.001);
    }

    @Test
    void fullDiscountYieldsZeroEvenWithTax() {
        Product base = controller.createBaseProduct("Muestra", 1000);
        Product free = controller.applyDiscount(base, 100);
        Product taxed = controller.applyTax(free, 50);
        assertEquals(0.0, free.getPrice(), 0.001);
        assertEquals(0.0, taxed.getPrice(), 0.001);
    }

    @Test
    void multipleDecoratorLayersAccumulateCorrectly() {
        Product base = controller.createBaseProduct("Pack", 1000);
        Product d1 = controller.applyDiscount(base, 10);
        Product t1 = controller.applyTax(d1, 5);
        Product d2 = controller.applyDiscount(t1, 20);
        double expected = 1000.0 * 0.90 * 1.05 * 0.80;
        assertEquals(expected, d2.getPrice(), 0.001);
    }

    @Test
    void strategyPercentageDiscountAppliesToGivenPrice() {
        context.setStrategy(new PercentageDiscount(15));
        double result = context.apply(2000.0);
        assertEquals(1700.0, result, 0.001);
    }

    @Test
    void strategyAndDecoratorTogetherProduceConsistentTotal() {
        Product product = controller.createBaseProduct("Jarabe Infantil", 10000);
        product = controller.applyTax(product, 19);
        context.setStrategy(new PercentageDiscount(10));
        double total = context.apply(product.getPrice());
        assertEquals(10710.0, total, 0.1);
    }

    @Test
    void calculateFinalPriceThrowsWhenNegativeDetected() {
        Product negative = new BaseProduct("Buggy", -5) {
            @Override
            public double getPrice() { return -10; }
        };
        assertThrows(IllegalStateException.class, () -> controller.calculateFinalPrice(negative));
    }

    @Test
    void createBaseProductRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> controller.createBaseProduct("Bad", -1));
    }

    @Test
    void applyDiscountRejectsNullProductAndNoopOnNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> controller.applyDiscount(null, 10));
        Product p = controller.createBaseProduct("Ok", 100);
        Product same = controller.applyDiscount(p, 0);
        assertSame(p.getClass(), same.getClass());
        assertEquals(100.0, same.getPrice(), 0.001);
    }

    @Test
    void applyTaxRejectsNullProductAndNoopOnNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> controller.applyTax(null, 5));
        Product p = controller.createBaseProduct("Ok", 100);
        Product same = controller.applyTax(p, 0);
        assertSame(p.getClass(), same.getClass());
        assertEquals(100.0, same.getPrice(), 0.001);
    }

    @Test
    void describeProductHandlesNullGracefully() {
        assertEquals("Producto no definido", controller.describeProduct(null));
    }
}